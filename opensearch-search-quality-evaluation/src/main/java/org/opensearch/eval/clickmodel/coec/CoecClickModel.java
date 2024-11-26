package org.opensearch.eval.clickmodel.coec;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchScrollRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Requests;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.WrapperQueryBuilder;
import org.opensearch.eval.clickmodel.ClickModel;
import org.opensearch.eval.engine.opensearch.OpenSearchHelper;
import org.opensearch.eval.model.ClickthroughRate;
import org.opensearch.eval.model.Judgment;
import org.opensearch.eval.model.ubi.event.UbiEvent;
import org.opensearch.eval.util.MathUtils;
import org.opensearch.eval.util.UserQueryHash;
import org.opensearch.search.Scroll;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class CoecClickModel extends ClickModel<CoecClickModelParameters> {

    // OpenSearch indexes.
    public static final String INDEX_RANK_AGGREGATED_CTR = "rank_aggregated_ctr";
    public static final String INDEX_QUERY_DOC_CTR = "click_through_rates";
    public static final String INDEX_JUDGMENT = "judgments";

    // UBI event names.
    public static final String EVENT_CLICK = "click";
    public static final String EVENT_VIEW = "view";

    private final CoecClickModelParameters parameters;

    private final OpenSearchHelper openSearchHelper;

    private final UserQueryHash userQueryHash = new UserQueryHash();
    private final Gson gson = new Gson();
    private final RestHighLevelClient client;

    private static final Logger LOGGER = LogManager.getLogger(CoecClickModel.class.getName());

    public CoecClickModel(final CoecClickModelParameters parameters) {

        this.parameters = parameters;
        this.openSearchHelper = new OpenSearchHelper(parameters.getRestHighLevelClient());
        this.client = parameters.getRestHighLevelClient();

    }

    @Override
    public Collection<Judgment> calculateJudgments() throws IOException {

        final int maxRank = parameters.getMaxRank();

        // Calculate and index the rank-aggregated click-through.
        final Map<Integer, Double> rankAggregatedClickThrough = getRankAggregatedClickThrough();
        LOGGER.info("Rank-aggregated clickthrough positions: {}", rankAggregatedClickThrough.size());
        showRankAggregatedClickThrough(rankAggregatedClickThrough);

        // Calculate and index the click-through rate for query/doc pairs.
        final Map<String, Set<ClickthroughRate>> clickthroughRates = getClickthroughRate(maxRank);
        LOGGER.info("Clickthrough rates for number of queries: {}", clickthroughRates.size());
        showClickthroughRates(clickthroughRates);

        // Generate and index the implicit judgments.
        final Collection<Judgment> judgments = calculateCoec(rankAggregatedClickThrough, clickthroughRates);
        LOGGER.info("Number of judgments: {}", judgments.size());

        return judgments;

    }

    public Collection<Judgment> calculateCoec(final Map<Integer, Double> rankAggregatedClickThrough,
                                               final Map<String, Set<ClickthroughRate>> clickthroughRates) throws IOException {

        // Calculate the COEC.
        // Numerator is the total number of clicks received by a query/result pair.
        // Denominator is the expected clicks (EC) that an average result would receive after being impressed i times at rank r,
        // and CTR is the average CTR for each position in the results page (up to R) computed over all queries and results.

        // Format: query_id, query, document, judgment
        final Collection<Judgment> judgments = new LinkedList<>();

        // Up to Rank R
        final int maxRank = 20;

        for(final String userQuery : clickthroughRates.keySet()) {

            // The clickthrough rates for this query.
            final Collection<ClickthroughRate> ctrs = clickthroughRates.get(userQuery);

            for(final ClickthroughRate ctr : ctrs) {

                double denominatorSum = 0;

                for(int r = 0; r < maxRank; r++) {

                    final double meanCtrAtRank = rankAggregatedClickThrough.getOrDefault(r, 0.0);
                    final int countOfTimesShownAtRank = openSearchHelper.getCountOfQueriesForUserQueryHavingResultInRankR(userQuery, ctr.getObjectId(), r);

//                    System.out.println("rank = " + r);
//                    System.out.println("\tmeanCtrAtRank = " + meanCtrAtRank);
//                    System.out.println("\tcountOfTimesShownAtRank = " + countOfTimesShownAtRank);

                    denominatorSum += (meanCtrAtRank * countOfTimesShownAtRank);

                }

                // Numerator is sum of clicks at all ranks up to the maxRank.
                final int totalNumberClicksForQueryResult = ctr.getClicks();

//                System.out.println("numerator = " + totalNumberClicksForQueryResult);
//                System.out.println("denominator = " + denominatorSum);

                // Divide the numerator by the denominator (value).
                final double judgment = totalNumberClicksForQueryResult / denominatorSum;

                // Hash the user query to get a query ID.
                final int queryId = userQueryHash.getHash(userQuery);

                // Add the judgment to the list.
                // TODO: What to do for query ID when the values are per user_query instead?
                judgments.add(new Judgment(String.valueOf(queryId), userQuery, ctr.getObjectId(), judgment));

            }

        }

        if(parameters.isPersist()) {
            openSearchHelper.indexJudgments(judgments);
        }

        return judgments;

    }

    /**
     * Gets the clickthrough rates for each query and its results.
     * @param maxRank The maximum rank position to consider.
     * @return A map of user_query to the clickthrough rate for each query result.
     * @throws IOException Thrown when a problem accessing OpenSearch.
     */
    private Map<String, Set<ClickthroughRate>> getClickthroughRate(final int maxRank) throws IOException {

        // For each query:
        // - Get each document returned in that query (in the QueryResponse object).
        // - Calculate the click-through rate for the document. (clicks/impressions)

        // TODO: Use maxRank in place of the hardcoded 20.
        // TODO: Allow for a time period and for a specific application.

        final String query = "{\n" +
                "                \"bool\": {\n" +
                "                  \"should\": [\n" +
                "                    {\n" +
                "                      \"term\": {\n" +
                "                        \"action_name\": \"click\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"term\": {\n" +
                "                        \"action_name\": \"view\"\n" +
                "                      }\n" +
                "                    }\n" +
                "                  ],\n" +
                "                  \"must\": [\n" +
                "                    {\n" +
                "                      \"range\": {\n" +
                "                        \"event_attributes.position.index\": {\n" +
                "                          \"lte\": 20\n" +
                "                        }\n" +
                "                      }\n" +
                "                    }\n" +
                "                  ]\n" +
                "                }\n" +
                "              }";

        final BoolQueryBuilder queryBuilder = new BoolQueryBuilder().must(new WrapperQueryBuilder(query));
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(queryBuilder).size(1000);
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(10L));

        final SearchRequest searchRequest = Requests
                .searchRequest(INDEX_UBI_EVENTS)
                .source(searchSourceBuilder)
                .scroll(scroll);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        final Map<String, Set<ClickthroughRate>> queriesToClickthroughRates = new HashMap<>();

        while (searchHits != null && searchHits.length > 0) {

            for (final SearchHit hit : searchHits) {

                final UbiEvent ubiEvent = gson.fromJson(hit.getSourceAsString(), UbiEvent.class);

                // We need to the hash of the query_id because two users can both search
                // for "computer" and those searches will have different query IDs, but they are the same search.
                final String userQuery = openSearchHelper.getUserQuery(ubiEvent.getQueryId());
                // LOGGER.debug("user_query = {}", userQuery);

                // Get the clicks for this queryId from the map, or an empty list if this is a new query.
                final Set<ClickthroughRate> clickthroughRates = queriesToClickthroughRates.getOrDefault(userQuery, new LinkedHashSet<>());

                // Get the ClickthroughRate object for the object that was interacted with.
                final ClickthroughRate clickthroughRate = clickthroughRates.stream().filter(p -> p.getObjectId().equals(ubiEvent.getEventAttributes().getObject().getObjectId())).findFirst().orElse(new ClickthroughRate(ubiEvent.getEventAttributes().getObject().getObjectId()));

                if (StringUtils.equalsIgnoreCase(ubiEvent.getActionName(), EVENT_CLICK)) {
                    clickthroughRate.logClick();
                } else {
                    clickthroughRate.logEvent();
                }

                clickthroughRates.add(clickthroughRate);
                queriesToClickthroughRates.put(userQuery, clickthroughRates);
                // LOGGER.debug("clickthroughRate = {}", queriesToClickthroughRates.size());

            }

            final SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);

            searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();

            searchHits = searchResponse.getHits().getHits();

        }

        if(parameters.isPersist()) {
            openSearchHelper.indexClickthroughRates(queriesToClickthroughRates);
        }

        return queriesToClickthroughRates;

    }

    /**
     * Calculate the rank-aggregated click through from the UBI events.
     * @return A map of positions to clickthrough rates.
     * @throws IOException Thrown when a problem accessing OpenSearch.
     */
    public Map<Integer, Double> getRankAggregatedClickThrough() throws IOException {

        final Map<Integer, Double> rankAggregatedClickThrough = new HashMap<>();

        // TODO: Allow for a time period and for a specific application.

        final QueryBuilder findRangeNumber = QueryBuilders.rangeQuery("event_attributes.position.index").lte(parameters.getMaxRank());
        final QueryBuilder queryBuilder = new BoolQueryBuilder().must(findRangeNumber);

        final TermsAggregationBuilder positionsAggregator = AggregationBuilders.terms("By_Position").field("event_attributes.position.index").size(parameters.getMaxRank());
        final TermsAggregationBuilder actionNameAggregation = AggregationBuilders.terms("By_Action").field("action_name").subAggregation(positionsAggregator).size(parameters.getMaxRank());

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.aggregation(actionNameAggregation);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(100);

        final SearchRequest searchRequest = new SearchRequest(INDEX_UBI_EVENTS).source(searchSourceBuilder);
        final SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        final Map<Integer, Double> clickCounts = new HashMap<>();
        final Map<Integer, Double> viewCounts = new HashMap<>();

        final Terms actionTerms = searchResponse.getAggregations().get("By_Action");
        final Collection<? extends Terms.Bucket> actionBuckets = actionTerms.getBuckets();
        for(final Terms.Bucket actionBucket : actionBuckets) {

            // Handle the "click" bucket.
            if(StringUtils.equalsIgnoreCase(actionBucket.getKey().toString(), EVENT_CLICK)) {

                final Terms positionTerms = actionBucket.getAggregations().get("By_Position");
                final Collection<? extends Terms.Bucket> positionBuckets = positionTerms.getBuckets();

                for(final Terms.Bucket positionBucket : positionBuckets) {
                    clickCounts.put(Integer.valueOf(positionBucket.getKey().toString()), (double) positionBucket.getDocCount());
                }

            }

            // Handle the "view" bucket.
            if(StringUtils.equalsIgnoreCase(actionBucket.getKey().toString(), EVENT_VIEW)) {

                final Terms positionTerms = actionBucket.getAggregations().get("By_Position");
                final Collection<? extends Terms.Bucket> positionBuckets = positionTerms.getBuckets();

                for(final Terms.Bucket positionBucket : positionBuckets) {
                    viewCounts.put(Integer.valueOf(positionBucket.getKey().toString()), (double) positionBucket.getDocCount());
                }

            }

        }

        for(final Integer x : clickCounts.keySet()) {
            //System.out.println("Position = " + x + ", Click Count = " + clickCounts.get(x) + ", Event Count = " + viewCounts.get(x));
            rankAggregatedClickThrough.put(x, clickCounts.get(x) / viewCounts.get(x));
        }

        if(parameters.isPersist()) {
            openSearchHelper.indexRankAggregatedClickthrough(rankAggregatedClickThrough);
        }

        return rankAggregatedClickThrough;

    }

    private void showClickthroughRates(final Map<String, Set<ClickthroughRate>> clickthroughRates) {

        for(final String userQuery : clickthroughRates.keySet()) {

            LOGGER.info("user_query: {}", userQuery);

            for(final ClickthroughRate clickthroughRate : clickthroughRates.get(userQuery)) {

                LOGGER.info("\t - {}", clickthroughRate.toString());

            }

        }

    }

    private void showRankAggregatedClickThrough(final Map<Integer, Double> rankAggregatedClickThrough) {

        for(final int position : rankAggregatedClickThrough.keySet()) {

            LOGGER.info("Position: {}, # ctr: {}", position, MathUtils.round(rankAggregatedClickThrough.get(position), parameters.getRoundingDigits()));

        }

    }

}
