/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.runners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.engine.SearchEngine;
import org.opensearch.eval.metrics.DcgSearchMetric;
import org.opensearch.eval.metrics.NdcgSearchMetric;
import org.opensearch.eval.metrics.PrecisionSearchMetric;
import org.opensearch.eval.metrics.SearchMetric;
import org.opensearch.eval.model.data.QueryResultMetric;
import org.opensearch.eval.model.data.QuerySet;
import org.opensearch.eval.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.opensearch.eval.Constants.DASHBOARD_METRICS_INDEX_NAME;

/**
 * A {@link AbstractQuerySetRunner} for Amazon OpenSearch.
 */
public class OpenSearchQuerySetRunner extends AbstractQuerySetRunner {

    private static final Logger LOGGER = LogManager.getLogger(OpenSearchQuerySetRunner.class);

    public static final String QUERY_PLACEHOLDER = "#?query##";

    /**
     * Creates a new query set runner
     *
     * @param searchEngine An OpenSearch engine {@link SearchEngine}.
     */
    public OpenSearchQuerySetRunner(final SearchEngine searchEngine) {
        super(searchEngine);
    }

    @Override
    public QuerySetRunResult run(final String querySetId, final String judgmentsId, final String index,
                                 final String searchPipeline, final String idField, final String query,
                                 final int k, final double threshold) throws Exception {

        final QuerySet querySet = searchEngine.getQuerySet(querySetId);
        LOGGER.info("Found {} queries in query set {}", querySet.getQuerySetQueries().size(), querySetId);

        try {

            // The results of each query.
            final List<QueryResult> queryResults = new ArrayList<>();

            for (Map<String, Long> queryMap : querySet.getQuerySetQueries()) {

                // Loop over each query in the map and run each one.
                for (final String userQuery : queryMap.keySet()) {

                    // Replace the query placeholder with the user query.
                    final String parsedQuery = query.replace(QUERY_PLACEHOLDER, userQuery);

                    // Build the query from the one that was passed in.
                    final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

                    searchSourceBuilder.query(QueryBuilders.wrapperQuery(parsedQuery));
                    searchSourceBuilder.from(0);
                    searchSourceBuilder.size(k);

                    final String[] includeFields = new String[]{idField};
                    final String[] excludeFields = new String[]{};
                    searchSourceBuilder.fetchSource(includeFields, excludeFields);

                    // LOGGER.info(searchSourceBuilder.toString());

                    final SearchRequest searchRequest = new SearchRequest(index);
                    searchRequest.source(searchSourceBuilder);

                    if (searchPipeline != null) {
                        searchSourceBuilder.pipeline(searchPipeline);
                        searchRequest.pipeline(searchPipeline);
                    }

                    // This is to keep OpenSearch from rejecting queries.
                    // TODO: Look at using the Workload Management in 2.18.0.
                    Thread.sleep(50);

                    client.search(searchRequest, new ActionListener<>() {

                        @Override
                        public void onResponse(final SearchResponse searchResponse) {

                            final List<String> orderedDocumentIds = new ArrayList<>();

                            for (final SearchHit hit : searchResponse.getHits().getHits()) {

                                final String documentId;

                                if ("_id".equals(idField)) {
                                    documentId = hit.getId();
                                } else {
                                    // TODO: Need to check this field actually exists.
                                    documentId = hit.getSourceAsMap().get(idField).toString();
                                }

                                orderedDocumentIds.add(documentId);

                            }

                            try {

                                final RelevanceScores relevanceScores = getRelevanceScores(judgmentsId, userQuery, orderedDocumentIds, k);

                                // Calculate the metrics for this query.
                                final SearchMetric dcgSearchMetric = new DcgSearchMetric(k, relevanceScores.getRelevanceScores());
                                final SearchMetric ndcgSearchmetric = new NdcgSearchMetric(k, relevanceScores.getRelevanceScores());
                                final SearchMetric precisionSearchMetric = new PrecisionSearchMetric(k, threshold, relevanceScores.getRelevanceScores());

                                final Collection<SearchMetric> searchMetrics = List.of(dcgSearchMetric, ndcgSearchmetric, precisionSearchMetric);

                                queryResults.add(new QueryResult(userQuery, orderedDocumentIds, k, searchMetrics, relevanceScores.getFrogs()));

                            } catch (Exception ex) {
                                LOGGER.error("Unable to get relevance scores for judgments {} and user query {}.", judgmentsId, userQuery, ex);
                            }

                        }

                        @Override
                        public void onFailure(Exception ex) {
                            LOGGER.error("Unable to search using query: {}", searchSourceBuilder.toString(), ex);
                        }
                    });

                }

            }

            // Calculate the search metrics for the entire query set given the individual query set metrics.
            // Sum up the metrics for each query per metric type.
            final int querySetSize = queryResults.size();
            final Map<String, Double> sumOfMetrics = new HashMap<>();
            for (final QueryResult queryResult : queryResults) {
                for (final SearchMetric searchMetric : queryResult.getSearchMetrics()) {
                    //LOGGER.info("Summing: {} - {}", searchMetric.getName(), searchMetric.getValue());
                    sumOfMetrics.merge(searchMetric.getName(), searchMetric.getValue(), Double::sum);
                }
            }

            // Now divide by the number of queries.
            final Map<String, Double> querySetMetrics = new HashMap<>();
            for (final String metric : sumOfMetrics.keySet()) {
                //LOGGER.info("Dividing by the query set size: {} / {}", sumOfMetrics.get(metric), querySetSize);
                querySetMetrics.put(metric, sumOfMetrics.get(metric) / querySetSize);
            }

            final String querySetRunId = UUID.randomUUID().toString();
            final QuerySetRunResult querySetRunResult = new QuerySetRunResult(querySetRunId, querySetId, queryResults, querySetMetrics);

            LOGGER.info("Query set run complete: {}", querySetRunId);

            return querySetRunResult;

        } catch (Exception ex) {
            throw new RuntimeException("Unable to run query set.", ex);
        }

    }

    @Override
    public void save(final QuerySetRunResult result) throws Exception {

        // Now, index the metrics as expected by the dashboards.

        // See https://github.com/o19s/opensearch-search-quality-evaluation/blob/main/opensearch-dashboard-prototyping/METRICS_SCHEMA.md
        // See https://github.com/o19s/opensearch-search-quality-evaluation/blob/main/opensearch-dashboard-prototyping/sample_data.ndjson

        final boolean dashboardMetricsIndexExists = searchEngine.doesIndexExist(DASHBOARD_METRICS_INDEX_NAME);

        if (!dashboardMetricsIndexExists) {

            // Create the index.
            // TODO: Read this mapping from a resource file instead.
            final String mapping = "{\n" +
                    "              \"properties\": {\n" +
                    "                \"datetime\": { \"type\": \"date\", \"format\": \"strict_date_time\" },\n" +
                    "                \"search_config\": { \"type\": \"keyword\" },\n" +
                    "                \"query_set_id\": { \"type\": \"keyword\" },\n" +
                    "                \"query\": { \"type\": \"keyword\" },\n" +
                    "                \"metric\": { \"type\": \"keyword\" },\n" +
                    "                \"value\": { \"type\": \"double\" },\n" +
                    "                \"application\": { \"type\": \"keyword\" },\n" +
                    "                \"evaluation_id\": { \"type\": \"keyword\" },\n" +
                    "                \"frogs_percent\": { \"type\": \"double\" }\n" +
                    "              }\n" +
                    "          }";

            // TODO: Make sure the index gets created successfully.
            searchEngine.createIndex(DASHBOARD_METRICS_INDEX_NAME, mapping);

        }

        final String timestamp = TimeUtils.getTimestamp();

        for(final QueryResult queryResult : result.getQueryResults()) {

            for(final SearchMetric searchMetric : queryResult.getSearchMetrics()) {

                final QueryResultMetric queryResultMetric = new QueryResultMetric();
                queryResultMetric.setDatetime(timestamp);
                queryResultMetric.setSearchConfig("research_1");
                queryResultMetric.setQuerySetId(result.getQuerySetId());
                queryResultMetric.setQuery(queryResult.getQuery());
                queryResultMetric.setMetric(searchMetric.getName());
                queryResultMetric.setValue(searchMetric.getValue());
                queryResultMetric.setApplication("sample_data");
                queryResultMetric.setEvaluationId(result.getRunId());
                queryResultMetric.setFrogsPercent(queryResult.getFrogs());

                searchEngine.indexQueryResultMetric(queryResultMetric);

            }

        }



    }

}
