/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.opensearch;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.Client;
import org.opensearch.core.action.ActionListener;
import org.opensearch.eval.judgments.model.ClickthroughRate;
import org.opensearch.eval.judgments.model.Judgment;
import org.opensearch.eval.judgments.model.ubi.query.UbiQuery;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.WrapperQueryBuilder;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.opensearch.eval.SearchQualityEvaluationPlugin.JUDGMENTS_INDEX_NAME;
import static org.opensearch.eval.SearchQualityEvaluationPlugin.UBI_EVENTS_INDEX_NAME;
import static org.opensearch.eval.SearchQualityEvaluationPlugin.UBI_QUERIES_INDEX_NAME;
import static org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel.INDEX_QUERY_DOC_CTR;
import static org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel.INDEX_RANK_AGGREGATED_CTR;

/**
 * Functionality for interacting with OpenSearch.
 * TODO: Move these functions out of this class.
 */
public class OpenSearchHelper {

    private static final Logger LOGGER = LogManager.getLogger(OpenSearchHelper.class.getName());

    private final Client client;
    private final Gson gson = new Gson();

    // Used to cache the query ID->user_query to avoid unnecessary lookups to OpenSearch.
    private static final Map<String, String> userQueryCache = new HashMap<>();

    public OpenSearchHelper(final Client client) {
        this.client = client;
    }

    /**
     * Gets the user query for a given query ID.
     * @param queryId The query ID.
     * @return The user query.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    public String getUserQuery(final String queryId) throws Exception {

        // If it's in the cache just get it and return it.
        if(userQueryCache.containsKey(queryId)) {
            return userQueryCache.get(queryId);
        }

        // Cache it and return it.
        final UbiQuery ubiQuery = getQueryFromQueryId(queryId);
        userQueryCache.put(queryId, ubiQuery.getUserQuery());

        return ubiQuery.getUserQuery();

    }

    /**
     * Gets the query object for a given query ID.
     * @param queryId The query ID.
     * @return A {@link UbiQuery} object for the given query ID.
     * @throws Exception Thrown if the query cannot be retrieved.
     */
    public UbiQuery getQueryFromQueryId(final String queryId) throws Exception {

        //LOGGER.info("Getting query from query ID {}", queryId);

        final String query = "{\"match\": {\"query_id\": \"" + queryId + "\" }}";
        final WrapperQueryBuilder qb = QueryBuilders.wrapperQuery(query);

        // The query_id should be unique anyway, but we are limiting it to a single result anyway.
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(qb);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);

        final String[] indexes = {UBI_QUERIES_INDEX_NAME};

        final SearchRequest searchRequest = new SearchRequest(indexes, searchSourceBuilder);
        final SearchResponse response = client.search(searchRequest).get();

        // Will only be a single result.
        final SearchHit hit = response.getHits().getHits()[0];

        //LOGGER.info("Retrieved query from query ID {}", queryId);

        return AccessController.doPrivileged((PrivilegedAction<UbiQuery>) () -> gson.fromJson(hit.getSourceAsString(), UbiQuery.class));

    }

    private Collection<String> getQueryIdsHavingUserQuery(final String userQuery) throws Exception {

        final String query = "{\"match\": {\"user_query\": \"" + userQuery + "\" }}";
        final WrapperQueryBuilder qb = QueryBuilders.wrapperQuery(query);

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(qb);

        final String[] indexes = {UBI_QUERIES_INDEX_NAME};

        final SearchRequest searchRequest = new SearchRequest(indexes, searchSourceBuilder);
        final SearchResponse response = client.search(searchRequest).get();

        final Collection<String> queryIds = new ArrayList<>();

        for(final SearchHit hit : response.getHits().getHits()) {
            final String queryId = hit.getSourceAsMap().get("query_id").toString();
            queryIds.add(queryId);
        }

        return queryIds;

    }

    public long getCountOfQueriesForUserQueryHavingResultInRankR(final String userQuery, final String objectId, final int rank) throws Exception {

        long countOfTimesShownAtRank = 0;

        // Get all query IDs matching this user query.
        final Collection<String> queryIds = getQueryIdsHavingUserQuery(userQuery);

        // For each query ID, get the events with action_name = "view" having a match on objectId and rank (position).
        for(final String queryId : queryIds) {

            //LOGGER.info("userQuery = {}; queryId = {}; objectId = {}; rank = {}", userQuery, queryId, objectId, rank);

            final String query = "{\n" +
                    "    \"bool\": {\n" +
                    "      \"must\": [\n" +
                    "          {\n" +
                    "            \"term\": {\n" +
                    "              \"query_id\": \"" + queryId + "\"\n" +
                    "            }\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"term\": {\n" +
                    "              \"action_name\": \"view\"\n" +
                    "            }\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"term\": {\n" +
                    "              \"event_attributes.position.index\": \"" + rank + "\"\n" +
                    "            }\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"term\": {\n" +
                    "              \"event_attributes.object.object_id\": \"" + objectId + "\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    }";

            //LOGGER.info(query);
            //LOGGER.info("----------------------------------------------------");

            final WrapperQueryBuilder qb = QueryBuilders.wrapperQuery(query);

            final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(qb);
            searchSourceBuilder.trackTotalHits(true);
            searchSourceBuilder.size(0);

            final String[] indexes = {UBI_EVENTS_INDEX_NAME};

            final SearchRequest searchRequest = new SearchRequest(indexes, searchSourceBuilder);
            final SearchResponse response = client.search(searchRequest).get();

//            if(queryId.equals("a2151d8c-44b6-4af6-9993-39cd7798671b")) {
//                if(objectId.equals("B07R1J8TYC")) {
//                    if(rank == 4) {
//                        LOGGER.info("This is the one!");
//                        LOGGER.info("Hits = {}", response.getHits().getTotalHits().value);
//                        LOGGER.info(response.toString());
//                    }
//                }
//            }

            //LOGGER.info("Query ID: {} --- Count of {} having {} at rank {} = {}", queryId, userQuery, objectId, rank, response.getHits().getTotalHits().value);

            countOfTimesShownAtRank += response.getHits().getTotalHits().value;

        }

        //LOGGER.info("Count of {} having {} at rank {} = {}", userQuery, objectId, rank, countOfTimesShownAtRank);

        if(countOfTimesShownAtRank > 0) {
            //LOGGER.info("Count of {} having {} at rank {} = {}", userQuery, objectId, rank, countOfTimesShownAtRank);
        }

        return countOfTimesShownAtRank;

        /*

        // This commented block was used to get the value using the ubi_queries index.
        // We can now just use the ubi_events index.

        final String query = "{\"match\": {\"user_query\": \"" + userQuery + "\" }}";
        final WrapperQueryBuilder qb = QueryBuilders.wrapperQuery(query);

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(qb);

        final String[] indexes = {INDEX_UBI_QUERIES};

        final SearchRequest searchRequest = new SearchRequest(indexes, searchSourceBuilder);
        final SearchResponse response = client.search(searchRequest).get();

        for(final SearchHit searchHit : response.getHits().getHits()) {

            final List<String> queryResponseHidsIds = (List<String>) searchHit.getSourceAsMap().get("query_response_hit_ids");

            if(queryResponseHidsIds.get(rank).equals(objectId)) {
                countOfTimesShownAtRank++;
            }

        }

        */

    }

    /**
     * Index the rank-aggregated clickthrough values.
     * @param rankAggregatedClickThrough A map of position to clickthrough values.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    public void indexRankAggregatedClickthrough(final Map<Integer, Double> rankAggregatedClickThrough) throws Exception {

        if(!rankAggregatedClickThrough.isEmpty()) {

            // TODO: Split this into multiple bulk insert requests.

            final BulkRequest request = new BulkRequest();

            for (final int position : rankAggregatedClickThrough.keySet()) {

                final Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("position", position);
                jsonMap.put("ctr", rankAggregatedClickThrough.get(position));

                final IndexRequest indexRequest = new IndexRequest(INDEX_RANK_AGGREGATED_CTR).id(UUID.randomUUID().toString()).source(jsonMap);

                request.add(indexRequest);

            }

            client.bulk(request).get();

        }

    }

    /**
     * Index the clickthrough rates.
     * @param clickthroughRates A map of query IDs to a collection of {@link ClickthroughRate} objects.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    public void indexClickthroughRates(final Map<String, Set<ClickthroughRate>> clickthroughRates) throws Exception {

        if(!clickthroughRates.isEmpty()) {

            // TODO: Split this into multiple bulk insert requests.

            final BulkRequest request = new BulkRequest();

            for(final String queryId : clickthroughRates.keySet()) {

                for(final ClickthroughRate clickthroughRate : clickthroughRates.get(queryId)) {

                    final Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put("query_id", queryId);
                    jsonMap.put("clicks", clickthroughRate.getClicks());
                    jsonMap.put("events", clickthroughRate.getEvents());
                    jsonMap.put("ctr", clickthroughRate.getClickthroughRate());

                    final IndexRequest indexRequest = new IndexRequest(INDEX_QUERY_DOC_CTR).id(UUID.randomUUID().toString()).source(jsonMap);

                    request.add(indexRequest);

                }

            }

            client.bulk(request).get();

        }

    }

    /**
     * Index the judgments.
     * @param judgments A collection of {@link Judgment judgments}.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     * @return The ID of the indexed judgments.
     */
    public String indexJudgments(final Collection<Judgment> judgments) throws Exception {

        final String judgmentsId = UUID.randomUUID().toString();

        final BulkRequest request = new BulkRequest();

        for(final Judgment judgment : judgments) {

            final Map<String, Object> j = judgment.getJudgmentAsMap();
            j.put("judgments_id", judgmentsId);

            final IndexRequest indexRequest = new IndexRequest(JUDGMENTS_INDEX_NAME)
                    .id(UUID.randomUUID().toString())
                    .source(j);

            request.add(indexRequest);

        }

        client.bulk(request, new ActionListener<>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                LOGGER.info("Judgments indexed: {}", judgmentsId);
            }

            @Override
            public void onFailure(Exception ex) {
                throw new RuntimeException("Unable to insert judgments.", ex);
            }
        });

        return judgmentsId;

    }

}