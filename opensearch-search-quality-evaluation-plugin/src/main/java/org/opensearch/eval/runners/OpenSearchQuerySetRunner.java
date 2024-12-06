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
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.Client;
import org.opensearch.core.action.ActionListener;
import org.opensearch.eval.SearchQualityEvaluationPlugin;
import org.opensearch.eval.metrics.DcgSearchMetric;
import org.opensearch.eval.metrics.SearchMetric;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.opensearch.eval.SearchQualityEvaluationRestHandler.QUERY_PLACEHOLDER;

/**
 * A {@link AbstractQuerySetRunner} for Amazon OpenSearch.
 */
public class OpenSearchQuerySetRunner extends AbstractQuerySetRunner {

    private static final Logger LOGGER = LogManager.getLogger(OpenSearchQuerySetRunner.class);

    /**
     * Creates a new query set runner
     *
     * @param client An OpenSearch {@link Client}.
     */
    public OpenSearchQuerySetRunner(final Client client) {
        super(client);
    }

    @Override
    public QuerySetRunResult run(final String querySetId, final String judgmentsId, final String index,
                                 final String searchPipeline, final String idField, final String query,
                                 final int k) throws Exception {

        final Collection<Map<String, Long>> querySet = getQuerySet(querySetId);
        LOGGER.info("Found {} queries in query set {}", querySet.size(), querySetId);

        try {

            // The results of each query.
            final List<QueryResult> queryResults = new ArrayList<>();

            for (Map<String, Long> queryMap : querySet) {

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

                    if(searchPipeline != null) {
                        searchSourceBuilder.pipeline(searchPipeline);
                    }

                    final SearchRequest searchRequest = new SearchRequest(index);
                    searchRequest.source(searchSourceBuilder);

                    // This is to keep OpenSearch from rejecting queries.
                    // TODO: Look at using the Workload Management in 2.18.0.
                    Thread.sleep(50);

                    client.search(searchRequest, new ActionListener<>() {

                        @Override
                        public void onResponse(final SearchResponse searchResponse) {

                            final List<String> orderedDocumentIds = new ArrayList<>();

                           // LOGGER.info("Found {} results for query {}", searchResponse.getHits().getTotalHits().value, userQuery);

                            for (final SearchHit hit : searchResponse.getHits().getHits()) {

                                final Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                                final String documentId = sourceAsMap.get(idField).toString();

                                //LOGGER.info("     -- query {}, documentId: {}", userQuery, documentId);
                                orderedDocumentIds.add(documentId);

                            }

                            //LOGGER.info("Number of documents: " + orderedDocumentIds.size());

                            // TODO: If no hits are returned, there's no need to get the relevance scores.
                            final List<Double> relevanceScores = getRelevanceScores(judgmentsId, userQuery, orderedDocumentIds, k);

                            final SearchMetric dcgSearchMetric = new DcgSearchMetric(k, relevanceScores);
                            // TODO: Add these metrics in, too.
                            //final SearchMetric ndcgSearchmetric = new NdcgSearchMetric(k, relevanceScores, idealRelevanceScores);
                            //final SearchMetric precisionSearchMetric = new PrecisionSearchMetric(k, relevanceScores);

                            LOGGER.info("query set dcg = " + dcgSearchMetric.getValue());

                            final Collection<SearchMetric> searchMetrics = List.of(dcgSearchMetric); // ndcgSearchmetric, precisionSearchMetric);

                            queryResults.add(new QueryResult(userQuery, orderedDocumentIds, k, searchMetrics));

                        }

                        @Override
                        public void onFailure(Exception ex) {
                            LOGGER.error("Unable to search using query: {}", parsedQuery, ex);
                        }
                    });

                }

            }

            // TODO: Calculate the search metrics for the entire query set given the results and the judgments.
            /*final List<String> orderedDocumentIds = new ArrayList<>();
            final List<Double> relevanceScores = getRelevanceScores(judgmentsId, "TODO", orderedDocumentIds, k);
            final SearchMetric dcgSearchMetric = new DcgSearchMetric(k, relevanceScores);
            // TODO: Add these metrics in, too.
            //final SearchMetric ndcgSearchmetric = new NdcgSearchMetric(k, relevanceScores, idealRelevanceScores);
            //final SearchMetric precisionSearchMetric = new PrecisionSearchMetric(k, relevanceScores);*/

            final Collection<SearchMetric> searchMetrics = new ArrayList<>(); // List.of(dcgSearchMetric); // ndcgSearchmetric, precisionSearchMetric);
            final String querySetRunId = UUID.randomUUID().toString();
            final QuerySetRunResult querySetRunResult = new QuerySetRunResult(querySetRunId, queryResults, searchMetrics);

            LOGGER.info("Query set run complete: {}", querySetRunId);

            return querySetRunResult;

        } catch (Exception ex) {
            throw new RuntimeException("Unable to run query set.", ex);
        }

    }

    @Override
    public void save(final QuerySetRunResult result) throws Exception {

        // Index the query results into OpenSearch.

        final Map<String, Object> results = new HashMap<>();

        results.put("run_id", result.getRunId());
        results.put("query_results", result.getQueryResultsAsMap());

        // Calculate and add each metric to the object to index.
        for (final SearchMetric searchMetric : result.getSearchMetrics()) {
            results.put(searchMetric.getName(), searchMetric.calculate());
        }

        final IndexRequest indexRequest = new IndexRequest(SearchQualityEvaluationPlugin.QUERY_SETS_RUN_RESULTS_INDEX_NAME)
                .source(results);

        client.index(indexRequest, new ActionListener<>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                LOGGER.debug("Query set results indexed.");
            }

            @Override
            public void onFailure(Exception ex) {
                throw new RuntimeException(ex);
            }
        });

    }

}
