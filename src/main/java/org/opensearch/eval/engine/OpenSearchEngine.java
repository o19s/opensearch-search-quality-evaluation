/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.engine;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.aggregations.LongTermsBucket;
import org.opensearch.client.opensearch._types.aggregations.StringTermsAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.ExistsQuery;
import org.opensearch.client.opensearch._types.query_dsl.FunctionScore;
import org.opensearch.client.opensearch._types.query_dsl.FunctionScoreQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RandomScoreFunction;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;
import org.opensearch.client.opensearch._types.query_dsl.WrapperQuery;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.ScrollRequest;
import org.opensearch.client.opensearch.core.ScrollResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.opensearch.client.opensearch.core.search.FieldCollapse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.TrackHits;
import org.opensearch.client.opensearch.generic.Bodies;
import org.opensearch.client.opensearch.generic.OpenSearchGenericClient;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.client.opensearch.generic.Response;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.opensearch.eval.Constants;
import org.opensearch.eval.metrics.SearchMetric;
import org.opensearch.eval.model.ClickthroughRate;
import org.opensearch.eval.model.TimeFilter;
import org.opensearch.eval.model.data.ClickThroughRate;
import org.opensearch.eval.model.data.Judgment;
import org.opensearch.eval.model.data.QueryResultMetric;
import org.opensearch.eval.model.data.QuerySet;
import org.opensearch.eval.model.data.RankAggregatedClickThrough;
import org.opensearch.eval.model.ubi.event.UbiEvent;
import org.opensearch.eval.model.ubi.query.UbiQuery;
import org.opensearch.eval.runners.QueryResult;
import org.opensearch.eval.runners.QuerySetRunResult;
import org.opensearch.eval.utils.TimeUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel.EVENT_CLICK;
import static org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel.EVENT_IMPRESSION;
import static org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel.INDEX_QUERY_DOC_CTR;
import static org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel.INDEX_RANK_AGGREGATED_CTR;
import static org.opensearch.eval.runners.OpenSearchQuerySetRunner.QUERY_PLACEHOLDER;

/**
 * Functionality for interacting with OpenSearch.
 */
public class OpenSearchEngine extends SearchEngine {

    private static final Logger LOGGER = LogManager.getLogger(OpenSearchEngine.class.getName());

    private static final String USER_QUERY_FIELD = "user_query";
    private static final String APPLICATION_FIELD = "application";

    private final OpenSearchClient client;

    // Used to cache the query ID->user_query to avoid unnecessary lookups to OpenSearch.
    private static final Map<String, String> userQueryCache = new HashMap<>();

    public OpenSearchEngine(final URI uri) {

        final HttpHost[] hosts = new HttpHost[]{
                HttpHost.create(uri)
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
    public boolean createIndex(final String index, final String mappingJson) throws IOException {

        final boolean doesIndexExist = doesIndexExist(index);

        if (!doesIndexExist) {

            final InputStream stream = new ByteArrayInputStream(mappingJson.getBytes(StandardCharsets.UTF_8));

            final CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                    .index(index)
                    .mappings(m -> m.withJson(stream))
                    .build();

            stream.close();

            return Boolean.TRUE.equals(client.indices().create(createIndexRequest).acknowledged());

        } else {

            // The index already exists.
            return true;

        }

    }

    @Override
    public String indexQuerySet(final QuerySet querySet) throws IOException {

        final String id = querySet.getId();

        final IndexRequest<QuerySet> indexRequest = new IndexRequest.Builder<QuerySet>().index(Constants.QUERY_SETS_INDEX_NAME).id(id).document(querySet).build();
        return client.index(indexRequest).id();

    }

    @Override
    public boolean doesQuerySetExist(final String querySetId) throws IOException {

        final Query query = Query.of(q -> q.term(m -> m.field("_id").value(FieldValue.of(querySetId))));

        final TrackHits trackHits = new TrackHits.Builder().enabled(true).build();
        final SearchResponse<QuerySet> searchResponse = client.search(s -> s.index(Constants.QUERY_SETS_INDEX_NAME).trackTotalHits(trackHits).query(query).size(1), QuerySet.class);

        if (searchResponse.hits().total().value() > 0) {
            return true;
        } else {
            return false;
        }

    }


    @Override
    public QuerySet getQuerySet(final String querySetId) throws IOException {

        final Query query = Query.of(q -> q.term(m -> m.field("_id").value(FieldValue.of(querySetId))));

        final SearchResponse<QuerySet> searchResponse = client.search(s -> s.index(Constants.QUERY_SETS_INDEX_NAME).query(query).size(1), QuerySet.class);

        return searchResponse.hits().hits().getFirst().source();

    }

    @Override
    public Double getJudgmentValue(final String judgmentsId, final String userQuery, final String documentId) throws Exception {

        var boolQuery = BoolQuery.of(bq -> bq
                .must(
                        List.of(
                                MatchQuery.of(mq -> mq.query(FieldValue.of(judgmentsId)).field("judgment_set_id")).toQuery(),
                                MatchQuery.of(mq -> mq.query(FieldValue.of(userQuery)).field("query")).toQuery(),
                                MatchQuery.of(mq -> mq.query(FieldValue.of(documentId)).field("document")).toQuery()
                        )
                )
        );

        final Query query = Query.of(q -> q.bool(boolQuery));

        // TODO: Make sure the query being run here is correct.
        //System.out.println(query.);

        final SearchResponse<Judgment> searchResponse = client.search(s -> s.index(Constants.JUDGMENTS_INDEX_NAME)
                        .query(query)
                        .from(0)
                        .size(1),
                Judgment.class);

        if (!searchResponse.hits().hits().isEmpty()) {
            System.out.println("Number of judgments: " + searchResponse.hits().hits().size());
        }

        if (searchResponse.hits().hits().isEmpty()) {
            return Double.NaN;
        } else {
            return searchResponse.hits().hits().getFirst().source().getJudgment();
        }

    }

    @Override
    public Map<String, Long> getRandomUbiQueries(final int n, final String application, final TimeFilter timeFilter) throws IOException {

        final long seed = System.currentTimeMillis();
        final RandomScoreFunction randomScoreFunction = new RandomScoreFunction.Builder().seed(String.valueOf(seed)).field(USER_QUERY_FIELD).build();
        final FunctionScore functionScore = new FunctionScore.Builder().randomScore(randomScoreFunction).build();

        final MatchAllQuery matchAllQuery = new MatchAllQuery.Builder().build();

        final FunctionScoreQuery functionScoreQuery = new FunctionScoreQuery.Builder()
                .query(matchAllQuery.toQuery())
                .functions(List.of(functionScore))
                .build();

        final List<Query> mustQueries = new ArrayList<>();

        mustQueries.add(new ExistsQuery.Builder().field(USER_QUERY_FIELD).build().toQuery());
        mustQueries.add(functionScoreQuery.toQuery());

        if(StringUtils.isNotEmpty(application)) {
            // Just a certain application.
            final TermQuery applicationQuery = TermQuery.of(tq -> tq.field("application").value(FieldValue.of(application)));
            mustQueries.add(applicationQuery.toQuery());
        }

        if(StringUtils.isNotEmpty(timeFilter.getStartTimestamp()) && StringUtils.isEmpty(timeFilter.getEndTimestamp())) {
            // Just a start timestamp.
            final RangeQuery timestampQuery = RangeQuery.of(q -> q.field("timestamp").gte(JsonData.of(timeFilter.getStartTimestamp())));
            mustQueries.add(timestampQuery.toQuery());
        }

        if(StringUtils.isEmpty(timeFilter.getStartTimestamp()) && StringUtils.isNotEmpty(timeFilter.getEndTimestamp())) {
            // Just an end timestamp.
            final RangeQuery timestampQuery = RangeQuery.of(q -> q.field("timestamp").lte(JsonData.of(timeFilter.getEndTimestamp())));
            mustQueries.add(timestampQuery.toQuery());
        }

        if(StringUtils.isNotEmpty(timeFilter.getStartTimestamp()) && StringUtils.isNotEmpty(timeFilter.getEndTimestamp())) {
            // Both start and end timestamps.
            final RangeQuery timestampQuery = RangeQuery.of(q -> q.field("timestamp").gte(JsonData.of(timeFilter.getStartTimestamp())).lte(JsonData.of(timeFilter.getEndTimestamp())));
            mustQueries.add(timestampQuery.toQuery());
        }

        final BoolQuery boolQuery = new BoolQuery.Builder()
                .must(mustQueries)
                .mustNot(q -> q.term(m -> m.field(USER_QUERY_FIELD).value(FieldValue.of(""))))
                .build();

        final SearchRequest searchRequest = new SearchRequest.Builder()
                .index(Constants.UBI_QUERIES_INDEX_NAME)
                .query(boolQuery.toQuery())
                .collapse(FieldCollapse.of(c -> c.field(USER_QUERY_FIELD)))
                .size(n)
                .build();

        final SearchResponse<UbiQuery> searchResponse = client.search(searchRequest, UbiQuery.class);

        final Map<String, Long> querySet = new HashMap<>();

        searchResponse.hits().hits().forEach(hit -> {
            final long count = getUserQueryCount(hit.source().getUserQuery());
            LOGGER.info("Adding user query to query set: {} with frequency {}", hit.source().getUserQuery(), count);
            querySet.put(hit.source().getUserQuery(), count);
        });

        return querySet;

    }

    @Override
    public Collection<UbiQuery> getUbiQueries(final String application, final TimeFilter timeFilter) throws IOException {

        final Collection<UbiQuery> ubiQueries = new ArrayList<>();

        final Time scrollTime = new Time.Builder().time("10m").build();

        final List<Query> mustQueries = new ArrayList<>();
        mustQueries.add(new MatchAllQuery.Builder().build().toQuery());

        if(StringUtils.isNotEmpty(application)) {
            mustQueries.add(new TermQuery.Builder().field("application").value(FieldValue.of(application)).build().toQuery());
        }

        // TODO: Add query for timeFilter.

        final BoolQuery boolQuery = new BoolQuery.Builder()
                .must(mustQueries)
                .mustNot(q -> q.term(m -> m.field(USER_QUERY_FIELD).value(FieldValue.of(""))))
                .build();

        final SearchResponse<UbiQuery> searchResponse = client.search(s -> s
                .index(Constants.UBI_QUERIES_INDEX_NAME)
                .query(boolQuery.toQuery())
                .size(1000)
                .scroll(scrollTime), UbiQuery.class);

        String scrollId = searchResponse.scrollId();
        List<Hit<UbiQuery>> searchHits = searchResponse.hits().hits();

        while (searchHits != null && !searchHits.isEmpty()) {

            for (int i = 0; i < searchResponse.hits().hits().size(); i++) {
                final UbiQuery ubiQuery = searchResponse.hits().hits().get(i).source();
                if (StringUtils.isNotEmpty(ubiQuery.getUserQuery())) {
                    ubiQueries.add(ubiQuery);
                }
            }

            if (scrollId != null) {
                final ScrollRequest scrollRequest = new ScrollRequest.Builder().scrollId(scrollId).build();
                final ScrollResponse<UbiQuery> scrollResponse = client.scroll(scrollRequest, UbiQuery.class);

                scrollId = scrollResponse.scrollId();
                searchHits = scrollResponse.hits().hits();
            } else {
                break;
            }

        }

        // TODO: Clear the scroll.
        // final ClearScrollRequest clearScrollRequest = new ClearScrollRequest.Builder().scrollId(scrollId).build();
        // client.clearScroll(clearScrollRequest);

        return ubiQueries;

    }

    @Override
    public Map<String, Long> getUbiQueries(final int n, final String application, final TimeFilter timeFilter) throws IOException {

        final Map<String, Long> querySet = new HashMap<>();

        final Aggregation userQueryAggregation = Aggregation.of(a -> a
                .terms(t -> t
                        .field(USER_QUERY_FIELD)
                        .size(n)
                )
        );

        final Map<String, Aggregation> aggregations = new HashMap<>();
        aggregations.put("By_User_Query", userQueryAggregation);

        final List<Query> mustQueries = new ArrayList<>();
        mustQueries.add(new ExistsQuery.Builder().field(USER_QUERY_FIELD).build().toQuery());

        if (StringUtils.isNotEmpty(application)) {
            mustQueries.add(new TermQuery.Builder().field(APPLICATION_FIELD).value(FieldValue.of(application)).build().toQuery());
        }

        // TODO: Add query for timeFilter.

        final BoolQuery boolQuery = new BoolQuery.Builder()
                .must(mustQueries)
                .mustNot(q -> q.term(m -> m.field(USER_QUERY_FIELD).value(FieldValue.of(""))))
                .build();

        final SearchRequest searchRequest = new SearchRequest.Builder()
                .index(Constants.UBI_QUERIES_INDEX_NAME)
                .query(boolQuery.toQuery())
                .aggregations(aggregations)
                .from(0)
                .size(0)
                .build();

        final SearchResponse<Void> searchResponse = client.search(searchRequest, Void.class);

        final Map<String, Aggregate> aggs = searchResponse.aggregations();
        final StringTermsAggregate byUserQuery = aggs.get("By_User_Query").sterms();
        final List<StringTermsBucket> byActionBuckets = byUserQuery.buckets().array();

        for (final StringTermsBucket bucket : byActionBuckets) {

            LOGGER.info("Adding user query to query set: {} with frequency {}", bucket.key(), bucket.docCount());
            querySet.put(bucket.key(), bucket.docCount());

        }

        return querySet;

    }

    @Override
    public long getUserQueryCount(final String userQuery) {

        try {

            final Query query = Query.of(q -> q.term(m -> m.field("user_query").value(FieldValue.of(userQuery))));

            final TrackHits trackHits = new TrackHits.Builder().enabled(true).build();
            final SearchResponse<UbiQuery> searchResponse = client.search(s -> s.index(Constants.UBI_QUERIES_INDEX_NAME).query(query).trackTotalHits(trackHits).size(0), UbiQuery.class);

            return searchResponse.hits().total().value();

        } catch (IOException ex) {

            LOGGER.error("Unable to determine count of user query: {}", userQuery);
            return -1;

        }

    }

    @Override
    public long getJudgmentsCount(final String judgmentsSetId) throws IOException {

        final Query query = Query.of(q -> q.term(m -> m.field("judgment_set_id").value(FieldValue.of(judgmentsSetId))));

        final TrackHits trackHits = new TrackHits.Builder().enabled(true).build();
        final SearchResponse<Judgment> searchResponse = client.search(s -> s.index(Constants.JUDGMENTS_INDEX_NAME).query(query).trackTotalHits(trackHits).size(0), Judgment.class);

        return searchResponse.hits().total().value();

    }

    @Override
    public Collection<Judgment> getJudgments() throws IOException {

        final Collection<Judgment> judgments = new ArrayList<>();

        final Time scrollTime = new Time.Builder().time("10m").build();

        final SearchResponse<Judgment> searchResponse = client.search(s -> s.index(Constants.JUDGMENTS_INDEX_NAME).size(1000).scroll(scrollTime), Judgment.class);

        String scrollId = searchResponse.scrollId();
        List<Hit<Judgment>> searchHits = searchResponse.hits().hits();

        while (searchHits != null && !searchHits.isEmpty()) {

            for (int i = 0; i < searchResponse.hits().hits().size(); i++) {
                judgments.add(searchResponse.hits().hits().get(i).source());
            }

            final ScrollRequest scrollRequest = new ScrollRequest.Builder().scrollId(scrollId).build();
            final ScrollResponse<Judgment> scrollResponse = client.scroll(scrollRequest, Judgment.class);

            scrollId = scrollResponse.scrollId();
            searchHits = scrollResponse.hits().hits();

        }

        return judgments;

    }

    @Override
    public boolean bulkIndex(String index, Map<String, Object> documents) throws IOException {

        final ArrayList<BulkOperation> bulkOperations = new ArrayList<>();

        for (final String id : documents.keySet()) {
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
     *
     * @param queryId The query ID.
     * @return The user query.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    @Override
    public String getUserQuery(final String queryId) throws Exception {

        // If it's in the cache just get it and return it.
        if (userQueryCache.containsKey(queryId)) {
            return userQueryCache.get(queryId);
        }

        // Cache it and return it.
        final UbiQuery ubiQuery = getQueryFromQueryId(queryId);

        // ubiQuery will be null if the query does not exist.
        if (ubiQuery != null) {

            userQueryCache.put(queryId, ubiQuery.getUserQuery());
            return ubiQuery.getUserQuery();

        } else {

            return null;

        }

    }

    /**
     * Gets the query object for a given query ID.
     *
     * @param queryId The query ID.
     * @return A {@link UbiQuery} object for the given query ID.
     * @throws Exception Thrown if the query cannot be retrieved.
     */
    @Override
    public UbiQuery getQueryFromQueryId(final String queryId) throws Exception {

        LOGGER.debug("Getting query from query ID {}", queryId);

        final SearchRequest searchRequest = new SearchRequest.Builder().query(q -> q.match(m -> m.field("query_id").query(FieldValue.of(queryId))))
                .index(Constants.UBI_QUERIES_INDEX_NAME)
                .from(0)
                .size(1)
                .build();

        final SearchResponse<UbiQuery> searchResponse = client.search(searchRequest, UbiQuery.class);

        // If this does not return a query then we cannot calculate the judgments. Each even should have a query associated with it.
        if (searchResponse.hits().hits() != null & !searchResponse.hits().hits().isEmpty()) {

            final UbiQuery ubiQuery = searchResponse.hits().hits().get(0).source();

            LOGGER.debug("Found query: {}", ubiQuery.getUserQuery().toString());

            return ubiQuery;

        } else {

            LOGGER.warn("No query exists for query ID {} to calculate judgments.", queryId);
            return null;

        }

    }

    @Override
    public List<String> runQuery(final String index, final String query, final int k, final String userQuery, final String idField, final String pipeline) throws IOException {

        // Replace the query placeholder with the user query.
        final String parsedQuery = query.replace(QUERY_PLACEHOLDER, userQuery);

        LOGGER.debug("Running query on index {}, k = {}, userQuery = {}, idField = {}, pipeline = {}, query = {}", index, k, userQuery, idField, pipeline, parsedQuery);

        // Use a generic client to get around https://github.com/opensearch-project/OpenSearch/issues/16829
        // Refer to https://code.dblock.org/2023/10/16/making-raw-json-rest-requests-to-opensearch.html
        final OpenSearchGenericClient genericClient = client.generic().withClientOptions(OpenSearchGenericClient.ClientOptions.throwOnHttpErrors());

        final Map<String, String> params = new HashMap<>();

        if (!pipeline.isEmpty()) {
            params.put("search_pipeline", pipeline);
        }

        // TODO: Need to consider k, or it needs to be the responsibility of the person to put k in the query.
        final Response searchResponse = genericClient.execute(
                Requests.builder()
                        .endpoint(index + "/_search")
                        .method("POST")
                        .query(params)
                        .json(parsedQuery)
                        .build());

        final JsonNode json = searchResponse.getBody()
                .map(b -> Bodies.json(b, JsonNode.class, client._transport().jsonpMapper()))
                .orElse(null);

        final List<String> orderedDocumentIds = new ArrayList<>();

        final JsonNode hits = json.get("hits").get("hits");
        // System.out.println("Number of hits for user query " + userQuery + ": " + hits.size());

        for (int i = 0; i < hits.size(); i++) {

            if (hits.get(i).get("_source").get(idField) != null) {
                orderedDocumentIds.add(hits.get(i).get("_source").get(idField).asText());
            } else {
                LOGGER.info("The requested idField {} does not exist.", idField);
            }

        }

        // The following commented code uses a wrapper query.
//        final String encodedQuery = Base64.getEncoder().encodeToString(parsedQuery.getBytes(StandardCharsets.UTF_8));

//        final WrapperQuery wrapperQuery = new WrapperQuery.Builder()
//                .query(encodedQuery)
//                .build();

        // TODO: Only return the idField since that's all we need.
        //       final SearchRequest searchRequest;

//        if(!pipeline.isEmpty()) {
//
//            searchRequest = new SearchRequest.Builder()
//                    .index(index)
//                    .query(q -> q.wrapper(wrapperQuery))
//                    .from(0)
//                    .size(k)
//                    .pipeline(pipeline)
//                    .build();
//
//        } else {
//
//            searchRequest = new SearchRequest.Builder()
//                    .index(index)
//                    .query(q -> q.wrapper(wrapperQuery))
//                    .from(0)
//                    .size(k)
//                    .build();
//
//        }

//        final SearchResponse<ObjectNode> searchResponse = client.search(searchRequest, ObjectNode.class);

//        final List<String> orderedDocumentIds = new ArrayList<>();
//
//        LOGGER.info("Encoded query: {}", encodedQuery);
//        LOGGER.info("Found hits: {}", searchResponse.hits().hits().size());
//
//        for (int i = 0; i < searchResponse.hits().hits().size(); i++) {
//
//            final String documentId;
//
//            if ("_id".equals(idField)) {
//                documentId = searchResponse.hits().hits().get(i).id();
//            } else {
//                // TODO: Need to check this field actually exists.
//                // TODO: Does this work?
//                final Hit<ObjectNode> hit = searchResponse.hits().hits().get(i);
//                documentId = hit.source().get(idField).toString();
//
//            }
//
//            orderedDocumentIds.add(documentId);
//
//        }

        return orderedDocumentIds;

    }

    @Override
    public Map<String, Set<ClickthroughRate>> getClickthroughRate(final int maxRank) throws Exception {

        final Map<String, Set<ClickthroughRate>> queriesToClickthroughRates = new HashMap<>();

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

        final String encodedQuery = Base64.getEncoder().encodeToString(query.getBytes(StandardCharsets.UTF_8));

        final WrapperQuery wrapperQuery = new WrapperQuery.Builder()
                .query(encodedQuery)
                .build();

        final Time scrollTime = new Time.Builder().time("10m").build();

        final SearchRequest searchRequest = new SearchRequest.Builder()
                .index(Constants.UBI_EVENTS_INDEX_NAME)
                .query(q -> q.wrapper(wrapperQuery))
                .from(0)
                .size(1000)
                .scroll(scrollTime)
                .build();

        // Use the generic client to send the raw json.
        // https://code.dblock.org/2023/10/16/making-raw-json-rest-requests-to-opensearch.html#:~:text=build()%3B,Here's%20a%20search%20example.&See%20the%20updated%20documentation%20and%20working%20demo%20for%20more%20information.

        final SearchResponse<UbiEvent> searchResponse = client.search(searchRequest, UbiEvent.class);

        String scrollId = searchResponse.scrollId();
        List<Hit<UbiEvent>> searchHits = searchResponse.hits().hits();

        while (searchHits != null && !searchHits.isEmpty()) {

            for (int i = 0; i < searchResponse.hits().hits().size(); i++) {

                final UbiEvent ubiEvent = searchResponse.hits().hits().get(i).source();

                // We need to the hash of the query_id because two users can both search
                // for "computer" and those searches will have different query IDs, but they are the same search.
                final String userQuery = getUserQuery(ubiEvent.getQueryId());

                // userQuery will be null if there is not a query for this event in ubi_queries.
                if (userQuery != null) {

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

                    // Safeguard to avoid having clicks without events.
                    // When the clicks is > 0 and impressions == 0, set the impressions to the number of clicks.
                    if (clickthroughRate.getClicks() > 0 && clickthroughRate.getImpressions() == 0) {
                        clickthroughRate.setImpressions(clickthroughRate.getClicks());
                    }

                    clickthroughRates.add(clickthroughRate);
                    queriesToClickthroughRates.put(userQuery, clickthroughRates);
                    // LOGGER.debug("clickthroughRate = {}", queriesToClickthroughRates.size());

                }

            }

            //LOGGER.info("Doing scroll to next results");
            // TODO: Getting a warning in the log that "QueryGroup _id can't be null, It should be set before accessing it. This is abnormal behaviour"
            // I don't remember seeing this prior to 2.18.0 but it's possible I just didn't see it.
            // https://github.com/opensearch-project/OpenSearch/blob/f105e4eb2ede1556b5dd3c743bea1ab9686ebccf/server/src/main/java/org/opensearch/wlm/QueryGroupTask.java#L73

            if (scrollId == null) {
                break;
            }

            final ScrollRequest scrollRequest = new ScrollRequest.Builder().scrollId(scrollId).build();
            final ScrollResponse<UbiEvent> scrollResponse = client.scroll(scrollRequest, UbiEvent.class);

            scrollId = scrollResponse.scrollId();
            searchHits = scrollResponse.hits().hits();

        }

        indexClickthroughRates(queriesToClickthroughRates);

        return queriesToClickthroughRates;

    }

    @Override
    public Map<Integer, Double> getRankAggregatedClickThrough(final int maxRank) throws Exception {

        final Map<Integer, Double> rankAggregatedClickThrough = new HashMap<>();

        final RangeQuery rangeQuery = RangeQuery.of(r -> r
                .field("event_attributes.position.ordinal")
                .lte(JsonData.of(maxRank))
        );

        // TODO: Is this the same as: final BucketOrder bucketOrder = BucketOrder.key(true);
        final List<Map<String, SortOrder>> sort = new ArrayList<>();
        sort.add(Map.of("_key", SortOrder.Asc));

        final Aggregation positionsAggregator = Aggregation.of(a -> a
                .terms(t -> t
                        .field("event_attributes.position.ordinal")
                        .size(maxRank)
                        .order(sort)
                )
        );

        final Aggregation actionNameAggregation = Aggregation.of(a -> a
                .terms(t -> t
                        .field("action_name")
                        .size(maxRank)
                        .order(sort)
                ).aggregations(Map.of("By_Position", positionsAggregator))
        );

        final Map<String, Aggregation> aggregations = new HashMap<>();
        aggregations.put("By_Action", actionNameAggregation);

        // TODO: Allow for a time period and for a specific application.
        final SearchRequest searchRequest = new SearchRequest.Builder()
                .index(Constants.UBI_EVENTS_INDEX_NAME)
                .aggregations(aggregations)
                .query(q -> q.range(rangeQuery))
                .from(0)
                .size(0)
                .build();

        final SearchResponse<Void> searchResponse = client.search(searchRequest, Void.class);

        final Map<String, Aggregate> aggs = searchResponse.aggregations();
        final StringTermsAggregate byAction = aggs.get("By_Action").sterms();
        final List<StringTermsBucket> byActionBuckets = byAction.buckets().array();

        final Map<Integer, Double> clickCounts = new HashMap<>();
        final Map<Integer, Double> impressionCounts = new HashMap<>();

        for (final StringTermsBucket bucket : byActionBuckets) {

            // Handle the "impression" bucket.
            if (EVENT_IMPRESSION.equalsIgnoreCase(bucket.key())) {

                final Aggregate positionTerms = bucket.aggregations().get("By_Position");

                final List<LongTermsBucket> positionBuckets = positionTerms.lterms().buckets().array();

                for (final LongTermsBucket positionBucket : positionBuckets) {
                    LOGGER.debug("Inserting impression event from position {} with click count {}", positionBucket.key(), (double) positionBucket.docCount());
                    impressionCounts.put(Integer.valueOf(positionBucket.key()), (double) positionBucket.docCount());
                }

            }

            // Handle the "click" bucket.
            if (EVENT_CLICK.equalsIgnoreCase(bucket.key())) {

                final Aggregate positionTerms = bucket.aggregations().get("By_Position");

                final List<LongTermsBucket> positionBuckets = positionTerms.lterms().buckets().array();

                for (final LongTermsBucket positionBucket : positionBuckets) {
                    LOGGER.debug("Inserting click event from position {} with click count {}", positionBucket.key(), (double) positionBucket.docCount());
                    clickCounts.put(Integer.valueOf(positionBucket.key()), (double) positionBucket.docCount());
                }

            }

        }

        for (int rank = 0; rank < maxRank; rank++) {

            if (impressionCounts.containsKey(rank)) {

                if (clickCounts.containsKey(rank)) {

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

        // Index the calculated values.
        indexRankAggregatedClickthrough(rankAggregatedClickThrough);

        return rankAggregatedClickThrough;

    }

    private Collection<String> getQueryIdsHavingUserQuery(final String userQuery) throws Exception {

        final SearchRequest searchRequest = new SearchRequest.Builder().query(q -> q.match(m -> m.field(USER_QUERY_FIELD).query(FieldValue.of(userQuery))))
                .index(Constants.UBI_QUERIES_INDEX_NAME)
                .build();

        final SearchResponse<UbiQuery> searchResponse = client.search(searchRequest, UbiQuery.class);

        final Collection<String> queryIds = new ArrayList<>();

        for (int i = 0; i < searchResponse.hits().hits().size(); i++) {
            queryIds.add(searchResponse.hits().hits().get(i).source().getQueryId());
        }

        return queryIds;

    }

    @Override
    public long getCountOfQueriesForUserQueryHavingResultInRankR(final String userQuery, final String objectId, final int rank) throws Exception {

        long countOfTimesShownAtRank = 0;

        // Get all query IDs matching this user query.
        final Collection<String> queryIds = getQueryIdsHavingUserQuery(userQuery);

        // For each query ID, get the events with action_name = "impression" having a match on objectId and rank (position).
        for (final String queryId : queryIds) {

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

            final String encodedQuery = Base64.getEncoder().encodeToString(query.getBytes(StandardCharsets.UTF_8));

            final WrapperQuery wrapperQuery = new WrapperQuery.Builder()
                    .query(encodedQuery)
                    .build();

            final SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(Constants.UBI_EVENTS_INDEX_NAME)
                    .query(q -> q.wrapper(wrapperQuery))
                    .size(0)
                    .trackTotalHits(TrackHits.of(t -> t.enabled(true)))
                    .build();

            final SearchResponse<UbiEvent> searchResponse = client.search(searchRequest, UbiEvent.class);

            countOfTimesShownAtRank += searchResponse.hits().total().value();

        }

        LOGGER.debug("Count of {} having {} at rank {} = {}", userQuery, objectId, rank, countOfTimesShownAtRank);

        if (countOfTimesShownAtRank > 0) {
            LOGGER.debug("Count of {} having {} at rank {} = {}", userQuery, objectId, rank, countOfTimesShownAtRank);
        }

        return countOfTimesShownAtRank;

    }

    /**
     * Index the rank-aggregated clickthrough values.
     *
     * @param rankAggregatedClickThrough A map of position to clickthrough values.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    @Override
    public void indexRankAggregatedClickthrough(final Map<Integer, Double> rankAggregatedClickThrough) throws Exception {

        if (!rankAggregatedClickThrough.isEmpty()) {

            // TODO: Use bulk indexing.

            for (final int position : rankAggregatedClickThrough.keySet()) {

                final String id = UUID.randomUUID().toString();

                final RankAggregatedClickThrough r = new RankAggregatedClickThrough(id);
                r.setPosition(position);
                r.setCtr(rankAggregatedClickThrough.get(position));

                final IndexRequest<RankAggregatedClickThrough> indexRequest = new IndexRequest.Builder<RankAggregatedClickThrough>().index(INDEX_RANK_AGGREGATED_CTR).id(id).document(r).build();
                client.index(indexRequest);

            }

        }

    }

    /**
     * Index the clickthrough rates.
     *
     * @param clickthroughRates A map of query IDs to a collection of {@link ClickthroughRate} objects.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    @Override
    public void indexClickthroughRates(final Map<String, Set<ClickthroughRate>> clickthroughRates) throws Exception {

        if (!clickthroughRates.isEmpty()) {

            // TODO: Use bulk inserts.

            for (final String userQuery : clickthroughRates.keySet()) {

                for (final ClickthroughRate clickthroughRate : clickthroughRates.get(userQuery)) {

                    final String id = UUID.randomUUID().toString();

                    final ClickThroughRate ctr = new ClickThroughRate(id);
                    ctr.setUserQuery(userQuery);
                    ctr.setClicks(clickthroughRate.getClicks());
                    ctr.setEvents(clickthroughRate.getImpressions());
                    ctr.setCtr(clickthroughRate.getClickthroughRate());
                    ctr.setObjectId(clickthroughRate.getObjectId());

                    LOGGER.debug("Clickthrough rate: {}", ctr);

                    // TODO: This index needs created.
                    final IndexRequest<ClickThroughRate> indexRequest = new IndexRequest.Builder<ClickThroughRate>().index(INDEX_QUERY_DOC_CTR).id(id).document(ctr).build();
                    client.index(indexRequest);

                }

            }

        }

    }

    @Override
    public void indexQueryResultMetric(final QueryResultMetric queryResultMetric) throws Exception {

        // TODO: Use bulk imports.

        final IndexRequest<QueryResultMetric> indexRequest = new IndexRequest.Builder<QueryResultMetric>()
                .index(Constants.DASHBOARD_METRICS_INDEX_NAME)
                .id(queryResultMetric.getId())
                .document(queryResultMetric).build();

        client.index(indexRequest);

    }

    /**
     * Index the judgments.
     *
     * @param judgments A collection of {@link Judgment judgments}.
     * @return The ID of the indexed judgments.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    @Override
    public String indexJudgments(final Collection<Judgment> judgments) throws Exception {

        final String judgmentsId = UUID.randomUUID().toString();
        final String timestamp = TimeUtils.getTimestamp();

        // TODO: Use bulk imports.

        for (final Judgment judgment : judgments) {

            judgment.setJudgmentSetId(judgmentsId);
            judgment.setTimestamp(timestamp);

            final IndexRequest<Judgment> indexRequest = new IndexRequest.Builder<Judgment>().index(Constants.JUDGMENTS_INDEX_NAME).id(judgment.getId()).document(judgment).build();
            client.index(indexRequest);

        }

        return judgmentsId;

    }

    @Override
    public void indexQueryRunResult(final QuerySetRunResult querySetRunResult) throws Exception {

        LOGGER.info("Indexing query run results.");

        // Now, index the metrics as expected by the dashboards.

        // See https://github.com/o19s/opensearch-search-quality-evaluation/blob/main/opensearch-dashboard-prototyping/METRICS_SCHEMA.md
        // See https://github.com/o19s/opensearch-search-quality-evaluation/blob/main/opensearch-dashboard-prototyping/sample_data.ndjson

        createIndex(Constants.DASHBOARD_METRICS_INDEX_NAME, Constants.METRICS_MAPPING_INDEX_MAPPING);

        final String timestamp = TimeUtils.getTimestamp();

        for (final QueryResult queryResult : querySetRunResult.getQueryResults()) {

            for (final SearchMetric searchMetric : queryResult.getSearchMetrics()) {

                final QueryResultMetric queryResultMetric = new QueryResultMetric();
                queryResultMetric.setTimestamp(timestamp);
                queryResultMetric.setSearchConfig(querySetRunResult.getSearchConfig());
                queryResultMetric.setQuerySetId(querySetRunResult.getQuerySetId());
                queryResultMetric.setQuery(queryResult.getQuery());
                queryResultMetric.setMetric(searchMetric.getName());
                queryResultMetric.setValue(searchMetric.getValue());
                queryResultMetric.setApplication(querySetRunResult.getApplication());
                queryResultMetric.setEvaluationId(querySetRunResult.getRunId());
                queryResultMetric.setFrogsPercent(queryResult.getFrogs());

                indexQueryResultMetric(queryResultMetric);

            }

        }

    }

}