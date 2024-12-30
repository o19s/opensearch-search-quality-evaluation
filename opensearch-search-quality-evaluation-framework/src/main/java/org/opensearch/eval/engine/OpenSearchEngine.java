/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.engine;

import com.google.gson.Gson;
import org.apache.hc.core5.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.mapping.IntegerNumberProperty;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.opensearch.eval.model.ClickthroughRate;
import org.opensearch.eval.model.Judgment;
import org.opensearch.eval.model.ubi.query.UbiQuery;
import org.opensearch.eval.utils.TimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel.INDEX_QUERY_DOC_CTR;
import static org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel.INDEX_RANK_AGGREGATED_CTR;

/**
 * Functionality for interacting with OpenSearch.
 */
public class OpenSearchEngine extends SearchEngine {

    private static final Logger LOGGER = LogManager.getLogger(OpenSearchEngine.class.getName());

    private final OpenSearchClient client;
    private final Gson gson = new Gson();

    // Used to cache the query ID->user_query to avoid unnecessary lookups to OpenSearch.
    private static final Map<String, String> userQueryCache = new HashMap<>();

    public OpenSearchEngine() {

        final HttpHost[] hosts = new HttpHost[] {
                new HttpHost("http", "localhost", 9200)
        };

        final OpenSearchTransport transport = ApacheHttpClient5TransportBuilder
                .builder(hosts)
                .setMapper(new JacksonJsonpMapper())
                .build();

        this.client = new OpenSearchClient(transport);

    }

    @Override
    public boolean doesIndexExist(final String index) throws IOException {

        return client.indices().exists(ExistsRequest.of(s -> s.index(index))).value();

    }

    @Override
    public boolean createIndex(String index, Map<String, Object> mapping) throws IOException {

        // TODO: Build the mapping.
        final TypeMapping mapping2 = new TypeMapping.Builder()
                .properties("age", new Property.Builder().integer(new IntegerNumberProperty.Builder().build()).build())
                .build();

        final CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(index).mappings(mapping2).build();

        return Boolean.TRUE.equals(client.indices().create(createIndexRequest).acknowledged());

    }

    @Override
    public boolean deleteIndex(String index) throws IOException {

        return client.indices().delete(s -> s.index(index)).acknowledged();

    }

    @Override
    public String indexJudgment(String index, String id, Judgment judgment) throws IOException {

        if(id == null) {
            id = UUID.randomUUID().toString();
        }

        final IndexRequest<Judgment> indexRequest = new IndexRequest.Builder<Judgment>().index(index).id(id).document(judgment).build();
        return client.index(indexRequest).id();

    }

    @Override
    public boolean bulkIndex(String index, Map<String, Object> documents) throws IOException {

        final ArrayList<BulkOperation> bulkOperations = new ArrayList<>();

        for(final String id : documents.keySet()) {
            final Object document = documents.get(id);
            bulkOperations.add(new BulkOperation.Builder().index(IndexOperation.of(io -> io.index(index).id(id).document(document))).build());
        }

        final BulkRequest.Builder bulkReq = new BulkRequest.Builder()
                .index(index)
                .operations(bulkOperations)
                .refresh(Refresh.WaitFor);

        final BulkResponse bulkResponse = client.bulk(bulkReq.build());

        return !bulkResponse.errors();

    }

    /**
     * Gets the user query for a given query ID.
     * @param queryId The query ID.
     * @return The user query.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    @Override
    public String getUserQuery(final String queryId) throws Exception {

        // If it's in the cache just get it and return it.
        if(userQueryCache.containsKey(queryId)) {
            return userQueryCache.get(queryId);
        }

        // Cache it and return it.
        final UbiQuery ubiQuery = getQueryFromQueryId(queryId);

        // ubiQuery will be null if the query does not exist.
        if(ubiQuery != null) {

            userQueryCache.put(queryId, ubiQuery.getUserQuery());
            return ubiQuery.getUserQuery();

        } else {

            return null;

        }

    }

    /**
     * Gets the query object for a given query ID.
     * @param queryId The query ID.
     * @return A {@link UbiQuery} object for the given query ID.
     * @throws Exception Thrown if the query cannot be retrieved.
     */
    @Override
    public UbiQuery getQueryFromQueryId(final String queryId) throws Exception {

        LOGGER.debug("Getting query from query ID {}", queryId);

        final String query = "{\"match\": {\"query_id\": \"" + queryId + "\" }}";
        final WrapperQueryBuilder qb = QueryBuilders.wrapperQuery(query);

        // The query_id should be unique anyway, but we are limiting it to a single result anyway.
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(qb);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);

        final String[] indexes = {Constants.UBI_QUERIES_INDEX_NAME};

        final SearchRequest searchRequest = new SearchRequest(indexes, searchSourceBuilder);
        final SearchResponse response = client.search(searchRequest).get();

        // If this does not return a query then we cannot calculate the judgments. Each even should have a query associated with it.
        if(response.getHits().getHits() != null & response.getHits().getHits().length > 0) {

            final SearchHit hit = response.getHits().getHits()[0];
            return gson.fromJson(hit.getSourceAsString(), UbiQuery.class);

        } else {

            LOGGER.warn("No query exists for query ID {} to calculate judgments.", queryId);
            return null;

        }

    }

    private Collection<String> getQueryIdsHavingUserQuery(final String userQuery) throws Exception {

        final String query = "{\"match\": {\"user_query\": \"" + userQuery + "\" }}";
        final WrapperQueryBuilder qb = QueryBuilders.wrapperQuery(query);

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(qb);

        final String[] indexes = {Constants.UBI_QUERIES_INDEX_NAME};

        final SearchRequest searchRequest = new SearchRequest(indexes, searchSourceBuilder);
        final SearchResponse response = client.search(searchRequest).get();

        final Collection<String> queryIds = new ArrayList<>();

        for(final SearchHit hit : response.getHits().getHits()) {
            final String queryId = hit.getSourceAsMap().get("query_id").toString();
            queryIds.add(queryId);
        }

        return queryIds;

    }

    @Override
    public long getCountOfQueriesForUserQueryHavingResultInRankR(final String userQuery, final String objectId, final int rank) throws Exception {

        long countOfTimesShownAtRank = 0;

        // Get all query IDs matching this user query.
        final Collection<String> queryIds = getQueryIdsHavingUserQuery(userQuery);

        // For each query ID, get the events with action_name = "impression" having a match on objectId and rank (position).
        for(final String queryId : queryIds) {

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
                    "              \"action_name\": \"impression\"\n" +
                    "            }\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"term\": {\n" +
                    "              \"event_attributes.position.ordinal\": \"" + rank + "\"\n" +
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

            final WrapperQueryBuilder qb = QueryBuilders.wrapperQuery(query);

            final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(qb);
            searchSourceBuilder.trackTotalHits(true);
            searchSourceBuilder.size(0);

            final String[] indexes = {Constants.UBI_EVENTS_INDEX_NAME};

            final SearchRequest searchRequest = new SearchRequest(indexes, searchSourceBuilder);
            final SearchResponse response = client.search(searchRequest).get();

            // Won't be null as long as trackTotalHits is true.
            if(response.getHits().getTotalHits() != null) {
                countOfTimesShownAtRank += response.getHits().getTotalHits().value;
            }

        }

        LOGGER.debug("Count of {} having {} at rank {} = {}", userQuery, objectId, rank, countOfTimesShownAtRank);

        if(countOfTimesShownAtRank > 0) {
            LOGGER.debug("Count of {} having {} at rank {} = {}", userQuery, objectId, rank, countOfTimesShownAtRank);
        }

        return countOfTimesShownAtRank;

    }

    /**
     * Index the rank-aggregated clickthrough values.
     * @param rankAggregatedClickThrough A map of position to clickthrough values.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    @Override
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
    @Override
    public void indexClickthroughRates(final Map<String, Set<ClickthroughRate>> clickthroughRates) throws Exception {

        if(!clickthroughRates.isEmpty()) {

            final BulkRequest request = new BulkRequest();

            for(final String userQuery : clickthroughRates.keySet()) {

                for(final ClickthroughRate clickthroughRate : clickthroughRates.get(userQuery)) {

                    final Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put("user_query", userQuery);
                    jsonMap.put("clicks", clickthroughRate.getClicks());
                    jsonMap.put("events", clickthroughRate.getImpressions());
                    jsonMap.put("ctr", clickthroughRate.getClickthroughRate());
                    jsonMap.put("object_id", clickthroughRate.getObjectId());

                    final IndexRequest indexRequest = new IndexRequest(INDEX_QUERY_DOC_CTR)
                            .id(UUID.randomUUID().toString())
                            .source(jsonMap);

                    request.add(indexRequest);

                }

            }

            client.bulk(request, new ActionListener<>() {

                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    if(bulkItemResponses.hasFailures()) {
                        LOGGER.error("Clickthrough rates were not all successfully indexed: {}", bulkItemResponses.buildFailureMessage());
                    } else {
                        LOGGER.debug("Clickthrough rates has been successfully indexed.");
                    }
                }

                @Override
                public void onFailure(Exception ex) {
                    LOGGER.error("Indexing the clickthrough rates failed.", ex);
                }

            });

        }

    }

    /**
     * Index the judgments.
     * @param judgments A collection of {@link Judgment judgments}.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     * @return The ID of the indexed judgments.
     */
    @Override
    public String indexJudgments(final Collection<Judgment> judgments) throws Exception {

        final String judgmentsId = UUID.randomUUID().toString();
        final String timestamp = TimeUtils.getTimestamp();

        final BulkRequest bulkRequest = new BulkRequest();

        for(final Judgment judgment : judgments) {

            final Map<String, Object> j = judgment.getJudgmentAsMap();
            j.put("judgments_id", judgmentsId);
            j.put("timestamp", timestamp);

            final IndexRequest indexRequest = new IndexRequest(Constants.JUDGMENTS_INDEX_NAME)
                    .id(UUID.randomUUID().toString())
                    .source(j);

            bulkRequest.add(indexRequest);

        }

        // TODO: Don't use .get()
        client.bulk(bulkRequest).get();

        return judgmentsId;

    }

}