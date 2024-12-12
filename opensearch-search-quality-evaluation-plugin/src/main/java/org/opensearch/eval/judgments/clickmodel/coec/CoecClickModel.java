/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.clickmodel.coec;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchScrollRequest;
import org.opensearch.client.Client;
import org.opensearch.client.Requests;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.eval.SearchQualityEvaluationPlugin;
import org.opensearch.eval.judgments.clickmodel.ClickModel;
import org.opensearch.eval.judgments.model.ClickthroughRate;
import org.opensearch.eval.judgments.model.Judgment;
import org.opensearch.eval.judgments.model.ubi.event.UbiEvent;
import org.opensearch.eval.judgments.opensearch.OpenSearchHelper;
import org.opensearch.eval.judgments.queryhash.IncrementalUserQueryHash;
import org.opensearch.eval.utils.MathUtils;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.WrapperQueryBuilder;
import org.opensearch.search.Scroll;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.BucketOrder;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class CoecClickModel extends ClickModel {

    public static final String CLICK_MODEL_NAME = "coec";

    // OpenSearch indexes for COEC data.
    public static final String INDEX_RANK_AGGREGATED_CTR = "rank_aggregated_ctr";
    public static final String INDEX_QUERY_DOC_CTR = "click_through_rates";

    // UBI event names.
    public static final String EVENT_CLICK = "click";
    public static final String EVENT_IMPRESSION = "impression";

    private final CoecClickModelParameters parameters;

    private final OpenSearchHelper openSearchHelper;

    private final IncrementalUserQueryHash incrementalUserQueryHash = new IncrementalUserQueryHash();
    private final Gson gson = new Gson();
    private final Client client;

    private static final Logger LOGGER = LogManager.getLogger(CoecClickModel.class.getName());

    public CoecClickModel(final Client client, final CoecClickModelParameters parameters) {

        this.parameters = parameters;
        this.openSearchHelper = new OpenSearchHelper(client);
        this.client = client;

    }

    @Override
    public String calculateJudgments() throws Exception {

        final int maxRank = parameters.getMaxRank();

        // Calculate and index the rank-aggregated click-through.
        LOGGER.info("Beginning calculation of rank-aggregated click-through.");
        final Map<Integer, Double> rankAggregatedClickThrough = getRankAggregatedClickThrough();
        LOGGER.info("Rank-aggregated clickthrough positions: {}", rankAggregatedClickThrough.size());
        showRankAggregatedClickThrough(rankAggregatedClickThrough);

        // Calculate and index the click-through rate for query/doc pairs.
        LOGGER.info("Beginning calculation of clickthrough rates.");
        final Map<String, Set<ClickthroughRate>> clickthroughRates = getClickthroughRate(maxRank);
        LOGGER.info("Clickthrough rates for number of queries: {}", clickthroughRates.size());
        showClickthroughRates(clickthroughRates);

        // Generate and index the implicit judgments.
        LOGGER.info("Beginning calculation of implicit judgments.");
        return calculateCoec(rankAggregatedClickThrough, clickthroughRates);

    }

    public String calculateCoec(final Map<Integer, Double> rankAggregatedClickThrough,
                                final Map<String, Set<ClickthroughRate>> clickthroughRates) throws Exception {

        // Calculate the COEC.
        // Numerator is the total number of clicks received by a query/result pair.
        // Denominator is the expected clicks (EC) that an average result would receive after being impressed i times at rank r,
        // and CTR is the average CTR for each position in the results page (up to R) computed over all queries and results.

        // Format: query_id, query, document, judgment
        final Collection<Judgment> judgments = new LinkedList<>();

        LOGGER.info("Count of queries: {}", clickthroughRates.size());

        for(final String userQuery : clickthroughRates.keySet()) {

            // The clickthrough rates for this one query.
            // A ClickthroughRate is a document with counts of impressions and clicks.
            final Collection<ClickthroughRate> ctrs = clickthroughRates.get(userQuery);

            // Go through each clickthrough rate for this query.
            for(final ClickthroughRate ctr : ctrs) {

                double denominatorSum = 0;

                for(int rank = 0; rank < parameters.getMaxRank(); rank++) {

                    // The document's mean CTR at the rank.
                    final double meanCtrAtRank = rankAggregatedClickThrough.getOrDefault(rank, 0.0);

                    // The number of times this document was shown as this rank.
                    final long countOfTimesShownAtRank = openSearchHelper.getCountOfQueriesForUserQueryHavingResultInRankR(userQuery, ctr.getObjectId(), rank);

                    denominatorSum += (meanCtrAtRank * countOfTimesShownAtRank);

                }

                // Numerator is sum of clicks at all ranks up to the maxRank.
                final int totalNumberClicksForQueryResult = ctr.getClicks();

                // Divide the numerator by the denominator (value).
                final double judgmentValue;

                if(denominatorSum == 0) {
                    judgmentValue = 0.0;
                } else {
                    judgmentValue = totalNumberClicksForQueryResult / denominatorSum;
                }

                // Hash the user query to get a query ID.
                final int queryId = incrementalUserQueryHash.getHash(userQuery);

                // Add the judgment to the list.
                // TODO: What to do for query ID when the values are per user_query instead?
                final Judgment judgment = new Judgment(String.valueOf(queryId), userQuery, ctr.getObjectId(), judgmentValue);
                judgments.add(judgment);

            }

        }

        LOGGER.info("Count of user queries: {}", clickthroughRates.size());
        LOGGER.info("Count of judgments: {}", judgments.size());

        showJudgments(judgments);

        if(!(judgments.isEmpty())) {
            return openSearchHelper.indexJudgments(judgments);
        } else {
            return null;
        }

    }

    /**
     * Gets the clickthrough rates for each query and its results.
     * @param maxRank The maximum rank position to consider.
     * @return A map of user_query to the clickthrough rate for each query result.
     * @throws IOException Thrown when a problem accessing OpenSearch.
     */
    private Map<String, Set<ClickthroughRate>> getClickthroughRate(final int maxRank) throws Exception {

        // For each query:
        // - Get each document returned in that query (in the QueryResponse object).
        // - Calculate the click-through rate for the document. (clicks/impressions)

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
                "                        \"action_name\": \"impression\"\n" +
                "                      }\n" +
                "                    }\n" +
                "                  ],\n" +
                "                  \"must\": [\n" +
                "                    {\n" +
                "                      \"range\": {\n" +
                "                        \"event_attributes.position.ordinal\": {\n" +
                "                          \"lte\": " + maxRank + "\n" +
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
                .searchRequest(SearchQualityEvaluationPlugin.UBI_EVENTS_INDEX_NAME)
                .source(searchSourceBuilder)
                .scroll(scroll);

        // TODO Don't use .get()
        SearchResponse searchResponse = client.search(searchRequest).get();

        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        final Map<String, Set<ClickthroughRate>> queriesToClickthroughRates = new HashMap<>();

        while (searchHits != null && searchHits.length > 0) {

            for (final SearchHit hit : searchHits) {

                final UbiEvent ubiEvent = AccessController.doPrivileged((PrivilegedAction<UbiEvent>) () -> gson.fromJson(hit.getSourceAsString(), UbiEvent.class));

                // We need to the hash of the query_id because two users can both search
                // for "computer" and those searches will have different query IDs, but they are the same search.
                final String userQuery = openSearchHelper.getUserQuery(ubiEvent.getQueryId());

                // userQuery will be null if there is not a query for this event in ubi_queries.
                if(userQuery != null) {

                    // Get the clicks for this queryId from the map, or an empty list if this is a new query.
                    final Set<ClickthroughRate> clickthroughRates = queriesToClickthroughRates.getOrDefault(userQuery, new LinkedHashSet<>());

                    // Get the ClickthroughRate object for the object that was interacted with.
                    final ClickthroughRate clickthroughRate = clickthroughRates.stream().filter(p -> p.getObjectId().equals(ubiEvent.getEventAttributes().getObject().getObjectId())).findFirst().orElse(new ClickthroughRate(ubiEvent.getEventAttributes().getObject().getObjectId()));

                    if (EVENT_CLICK.equalsIgnoreCase(ubiEvent.getActionName())) {
                        //LOGGER.info("Logging a CLICK on " + ubiEvent.getEventAttributes().getObject().getObjectId());
                        clickthroughRate.logClick();
                    } else if (EVENT_IMPRESSION.equalsIgnoreCase(ubiEvent.getActionName())) {
                        //LOGGER.info("Logging an IMPRESSION on " + ubiEvent.getEventAttributes().getObject().getObjectId());
                        clickthroughRate.logImpression();
                    } else {
                        LOGGER.warn("Invalid event action name: {}", ubiEvent.getActionName());
                    }

                    clickthroughRates.add(clickthroughRate);
                    queriesToClickthroughRates.put(userQuery, clickthroughRates);
                    // LOGGER.debug("clickthroughRate = {}", queriesToClickthroughRates.size());

                }

            }

            final SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);

            //LOGGER.info("Doing scroll to next results");
            // TODO: Getting a warning in the log that "QueryGroup _id can't be null, It should be set before accessing it. This is abnormal behaviour"
            // I don't remember seeing this prior to 2.18.0 but it's possible I just didn't see it.
            // https://github.com/opensearch-project/OpenSearch/blob/f105e4eb2ede1556b5dd3c743bea1ab9686ebccf/server/src/main/java/org/opensearch/wlm/QueryGroupTask.java#L73
            searchResponse = client.searchScroll(scrollRequest).get();
            //LOGGER.info("Scroll complete.");

            scrollId = searchResponse.getScrollId();

            searchHits = searchResponse.getHits().getHits();

        }

        openSearchHelper.indexClickthroughRates(queriesToClickthroughRates);

        return queriesToClickthroughRates;

    }

    /**
     * Calculate the rank-aggregated click through from the UBI events.
     * @return A map of positions to clickthrough rates.
     * @throws IOException Thrown when a problem accessing OpenSearch.
     */
    public Map<Integer, Double> getRankAggregatedClickThrough() throws Exception {

        final Map<Integer, Double> rankAggregatedClickThrough = new HashMap<>();

        // TODO: Allow for a time period and for a specific application.

        final QueryBuilder findRangeNumber = QueryBuilders.rangeQuery("event_attributes.position.ordinal").lte(parameters.getMaxRank());
        final QueryBuilder queryBuilder = new BoolQueryBuilder().must(findRangeNumber);

        // Order the aggregations by key and not by value.
        final BucketOrder bucketOrder = BucketOrder.key(true);

        final TermsAggregationBuilder positionsAggregator = AggregationBuilders.terms("By_Position").field("event_attributes.position.ordinal").order(bucketOrder).size(parameters.getMaxRank());
        final TermsAggregationBuilder actionNameAggregation = AggregationBuilders.terms("By_Action").field("action_name").subAggregation(positionsAggregator).order(bucketOrder).size(parameters.getMaxRank());

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder)
                .aggregation(actionNameAggregation)
                .from(0)
                .size(0);

        final SearchRequest searchRequest = new SearchRequest(SearchQualityEvaluationPlugin.UBI_EVENTS_INDEX_NAME).source(searchSourceBuilder);
        final SearchResponse searchResponse = client.search(searchRequest).get();

        final Map<Integer, Double> clickCounts = new HashMap<>();
        final Map<Integer, Double> impressionCounts = new HashMap<>();

        final Terms actionTerms = searchResponse.getAggregations().get("By_Action");
        final Collection<? extends Terms.Bucket> actionBuckets = actionTerms.getBuckets();

        LOGGER.debug("Aggregation query: {}", searchSourceBuilder.toString());

        for(final Terms.Bucket actionBucket : actionBuckets) {

            // Handle the "impression" bucket.
            if(EVENT_IMPRESSION.equalsIgnoreCase(actionBucket.getKey().toString())) {

                final Terms positionTerms = actionBucket.getAggregations().get("By_Position");
                final Collection<? extends Terms.Bucket> positionBuckets = positionTerms.getBuckets();

                for(final Terms.Bucket positionBucket : positionBuckets) {
                    LOGGER.debug("Inserting impression event from position {} with click count {}", positionBucket.getKey(), (double) positionBucket.getDocCount());
                    impressionCounts.put(Integer.valueOf(positionBucket.getKey().toString()), (double) positionBucket.getDocCount());
                }

            }

            // Handle the "click" bucket.
            if(EVENT_CLICK.equalsIgnoreCase(actionBucket.getKey().toString())) {

                final Terms positionTerms = actionBucket.getAggregations().get("By_Position");
                final Collection<? extends Terms.Bucket> positionBuckets = positionTerms.getBuckets();

                for(final Terms.Bucket positionBucket : positionBuckets) {
                    LOGGER.debug("Inserting client event from position {} with click count {}", positionBucket.getKey(), (double) positionBucket.getDocCount());
                    clickCounts.put(Integer.valueOf(positionBucket.getKey().toString()), (double) positionBucket.getDocCount());
                }

            }

        }

        for(int rank = 0; rank < parameters.getMaxRank(); rank++) {

            if(impressionCounts.containsKey(rank)) {

                if(clickCounts.containsKey(rank)) {

                    // Calculate the CTR by dividing the number of clicks by the number of impressions.
                    LOGGER.info("Position = {}, Impression Counts = {}, Click Count = {}", rank, impressionCounts.get(rank), clickCounts.get(rank));
                    rankAggregatedClickThrough.put(rank, clickCounts.get(rank) / impressionCounts.get(rank));

                } else {

                    // This document has impressions but no clicks, so it's CTR is zero.
                    LOGGER.info("Position = {}, Impression Counts = {}, Impressions but no clicks so CTR is 0", rank, clickCounts.get(rank));
                    rankAggregatedClickThrough.put(rank, 0.0);

                }

            } else {

                // No impressions so the clickthrough rate is 0.
                LOGGER.info("No impressions for rank {}, so using CTR of 0", rank);
                rankAggregatedClickThrough.put(rank, (double) 0);

            }

        }

        openSearchHelper.indexRankAggregatedClickthrough(rankAggregatedClickThrough);

        return rankAggregatedClickThrough;

    }

    private void showJudgments(final Collection<Judgment> judgments) {

        for(final Judgment judgment : judgments) {
            LOGGER.info(judgment.toJudgmentString());
        }

    }

    private void showClickthroughRates(final Map<String, Set<ClickthroughRate>> clickthroughRates) {

        for(final String userQuery : clickthroughRates.keySet()) {

            LOGGER.debug("user_query: {}", userQuery);

            for(final ClickthroughRate clickthroughRate : clickthroughRates.get(userQuery)) {
                LOGGER.debug("\t - {}", clickthroughRate.toString());
            }

        }

    }

    private void showRankAggregatedClickThrough(final Map<Integer, Double> rankAggregatedClickThrough) {

        for(final int position : rankAggregatedClickThrough.keySet()) {
            LOGGER.info("Position: {}, # ctr: {}", position, MathUtils.round(rankAggregatedClickThrough.get(position), parameters.getRoundingDigits()));
        }

    }

}
