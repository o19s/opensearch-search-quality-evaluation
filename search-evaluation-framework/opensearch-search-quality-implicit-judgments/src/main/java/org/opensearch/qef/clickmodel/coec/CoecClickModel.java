package org.opensearch.qef.clickmodel.coec;

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
import org.opensearch.index.query.WrapperQueryBuilder;
import org.opensearch.qef.clickmodel.ClickModel;
import org.opensearch.qef.engine.opensearch.OpenSearchHelper;
import org.opensearch.qef.model.ClickthroughRate;
import org.opensearch.qef.model.Judgment;
import org.opensearch.qef.model.ubi.event.UbiEvent;
import org.opensearch.qef.util.MathUtils;
import org.opensearch.qef.util.UserQueryHash;
import org.opensearch.search.Scroll;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.*;

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

    public CoecClickModel(final CoecClickModelParameters parameters, final OpenSearchHelper openSearchHelper) {

        this.parameters = parameters;
        this.openSearchHelper = openSearchHelper;
        this.client = parameters.getRestHighLevelClient();

    }

    @Override
    public Collection<Judgment> calculateJudgments() throws IOException {

        final int maxRank = parameters.getMaxRank();

        // Calculate and index the rank-aggregated click-through.
        final Map<Integer, Double> rankAggregatedClickThrough = getRankAggregatedClickThrough(maxRank);
        LOGGER.info("Rank-aggregated clickthrough positions: {}", rankAggregatedClickThrough.size());
        showRankAggregatedClickThrough(rankAggregatedClickThrough);

//        // Calculate and index the click-through rate for query/doc pairs.
//        final Map<String, Set<ClickthroughRate>> clickthroughRates = getClickthroughRate(maxRank);
//        LOGGER.info("Clickthrough rates for number of queries: {}", clickthroughRates.size());
//        showClickthroughRates(clickthroughRates);
//
//        // Generate and index the implicit judgments.
//        final Collection<Judgment> judgments = calculateCoec(rankAggregatedClickThrough, clickthroughRates);
//        LOGGER.info("Number of judgments: {}", judgments.size());

       // return judgments;

        return null;

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

        // TODO: Only consider events that are VIEW or CLICK.

        final String query = "{\"match_all\":{}}";
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

                final Gson gson = new Gson();
                final UbiEvent ubiEvent = gson.fromJson(hit.getSourceAsString(), UbiEvent.class);

                if(ubiEvent.getEventAttributes() != null && ubiEvent.getEventAttributes().getPosition() != null) {

                    if (ubiEvent.getEventAttributes().getPosition().getIndex() <= maxRank) {

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

                }

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
     * @param maxRank The maximum rank position to consider.
     * @return A map of positions to clickthrough rates.
     * @throws IOException Thrown when a problem accessing OpenSearch.
     */
    public Map<Integer, Double> getRankAggregatedClickThrough(final int maxRank) throws IOException {

        final Map<Integer, Double> rankAggregatedClickThrough = new HashMap<>();

        // TODO: Change this to a query over just clicks and views (configurable)
        // over some past time period, and allow for selecting which application.

        // Aggregation query to get event counts per position.
        /**
         * GET ubi_events/_search
         * {
         *   "size": 0,
         *   "aggs": {
         *     "By_Action": {
         *       "terms": {
         *         "field": "action_name"
         *       },
         *       "aggs": {
         *         "By_Position": {
         *           "terms": {
         *             "field": "event_attributes.position.index"
         *           }
         *         }
         *       }
         *     }
         *   }
         * }
         */


        TermsAggregationBuilder positionsAggregator = AggregationBuilders.terms("By_Position").field("event_attributes.position.index");
        TermsAggregationBuilder actionNameAggregation = AggregationBuilders.terms("By_Action").field("action_name").subAggregation(positionsAggregator);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //searchSourceBuilder.query(QueryBuilders.queryStringQuery(myQueryString));
        searchSourceBuilder.aggregation(actionNameAggregation);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(0);

        SearchRequest searchRequest = new SearchRequest(INDEX_UBI_EVENTS);
        searchRequest.source(searchSourceBuilder);

        final SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

//        for(final Aggregation aggregation : searchResponse.getAggregations().asList()) {
//            System.out.println("aggregation name: " + aggregation.getName());
//            for(final String s : aggregation.getMetadata().keySet()) {
//                System.out.println("\ts = " + aggregation.getMetadata().get(s));
//            }
//        }

        final Map<Integer, Long> clickCounts = new HashMap<>();
        final Map<Integer, Long> viewCounts = new HashMap<>();

        final Terms actionTerms = searchResponse.getAggregations().get("By_Action");
        final Collection<? extends Terms.Bucket> actionBuckets = actionTerms.getBuckets();
        for(final Terms.Bucket actionBucket : actionBuckets) {

            //System.out.println("Key: " + actionBucket.getKey() + " Count: " + actionBucket.getDocCount());

            if(StringUtils.equalsIgnoreCase(actionBucket.getKey().toString(), EVENT_CLICK)) {
                //clicks = actionBucket.getDocCount();

                final Terms positionTerms = actionBucket.getAggregations().get("By_Position");
                final Collection<? extends Terms.Bucket> positionBuckets = positionTerms.getBuckets();

                for(final Terms.Bucket positionBucket : positionBuckets) {
                    //System.out.println("\tKey: " + positionBucket.getKey() + " Count: " + positionBucket.getDocCount());
                    clickCounts.put(Integer.valueOf(positionBucket.getKey().toString()), positionBucket.getDocCount());
                }

            }

            if(StringUtils.equalsIgnoreCase(actionBucket.getKey().toString(), EVENT_VIEW)) {
                //views = actionBucket.getDocCount();

                final Terms positionTerms = actionBucket.getAggregations().get("By_Position");
                final Collection<? extends Terms.Bucket> positionBuckets = positionTerms.getBuckets();

                for(final Terms.Bucket positionBucket : positionBuckets) {
                    //System.out.println("\tKey: " + positionBucket.getKey() + " Count: " + positionBucket.getDocCount());
                    viewCounts.put(Integer.valueOf(positionBucket.getKey().toString()), positionBucket.getDocCount());
                }

            }

        }

        for(final Integer i : clickCounts.keySet()) {
            System.out.println("Position = " + i + ", Click Count = " + clickCounts.get(i));
        }

        System.out.println("==================");

        for(final Integer i : viewCounts.keySet()) {
            System.out.println("Position = " + i + ", View Count = " + viewCounts.get(i));
        }

        System.out.println("==================");

        for(int x = 0; x < clickCounts.size(); x++) {
            if(clickCounts.get(x) != null) {
                clickCounts.put(x, clickCounts.get(x) / (clickCounts.get(x) + viewCounts.get(x)));
            }
        }

        System.out.println("==================");

        for(final Integer i : clickCounts.keySet()) {
            System.out.println("Position = " + i + ", CTR = " + clickCounts.get(i));
        }

//        System.out.println("---------------------");
//
//        Terms terms2 = searchResponse.getAggregations().get("By_Position");
//        Collection<? extends Terms.Bucket> buckets2 = terms2.getBuckets();
//        for (Terms.Bucket x : buckets2) {
//            System.out.println("Key: " + x.getKey() + " Count: " + x.getDocCount());
//        }

        //String scrollId = searchResponse.getScrollId();
//        SearchHit[] searchHits = searchResponse.getHits().getHits();
//
//        long totalEvents = 0;
//
//        while (searchHits != null && searchHits.length > 0) {
//
//            for (final SearchHit searchHit : searchHits) {
//
//                final UbiEvent ubiEvent = gson.fromJson(searchHit.getSourceAsString(), UbiEvent.class);
//
//                if(ubiEvent.getEventAttributes() != null && ubiEvent.getEventAttributes().getPosition() != null) {
//
//                    if (ubiEvent.getEventAttributes().getPosition().getIndex() <= maxRank) {
//
//                        // Increment the number of clicks for the position.
//                        if (StringUtils.equalsIgnoreCase(ubiEvent.getActionName(), EVENT_CLICK)) {
//                            rankAggregatedClickThrough.merge(ubiEvent.getEventAttributes().getPosition().getIndex(), 1.0, Double::sum);
//                        }
//
//                    }
//
//                }
//
//            }

            // Sum up the total number of events.
         //   totalEvents += searchHits.length;

//            final SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
//            scrollRequest.scroll(scroll);
//
//            searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
//            scrollId = searchResponse.getScrollId();

           // searchHits = searchResponse.getHits().getHits();

   //     }

//        // TODO: Change the denominator to be from totalEvents to
//        // impressions at rank r (to avoid double counting).
//        // Map from rank to number of impressions.
//
//        // Now for each position, divide its value by the total number of events.
//        // This is the click-through rate.
//        for(final Integer i : rankAggregatedClickThrough.keySet()) {
//            rankAggregatedClickThrough.put(i, rankAggregatedClickThrough.get(i) / totalEvents);
//        }
//
//        final ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
//        clearScrollRequest.addScrollId(scrollId);
//        client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
//
//        LOGGER.info("Rank-aggregated click through: {}", rankAggregatedClickThrough);
//        LOGGER.info("Number of total events: {}", totalEvents);
//
//        if(parameters.isPersist()) {
//            openSearchHelper.indexRankAggregatedClickthrough(rankAggregatedClickThrough);
//        }

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
