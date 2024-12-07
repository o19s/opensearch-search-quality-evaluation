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
import org.opensearch.eval.judgments.model.Judgment;
import org.opensearch.eval.metrics.DcgSearchMetric;
import org.opensearch.eval.metrics.NdcgSearchMetric;
import org.opensearch.eval.metrics.PrecisionSearchMetric;
import org.opensearch.eval.metrics.SearchMetric;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.opensearch.eval.SearchQualityEvaluationRestHandler.QUERY_PLACEHOLDER;

/**
 * A {@link AbstractQuerySetRunner} for Amazon OpenSearch.
 */
public class OpenSearchAbstractQuerySetRunner extends AbstractQuerySetRunner {

    private static final Logger LOGGER = LogManager.getLogger(OpenSearchAbstractQuerySetRunner.class);

    /**
     * Creates a new query set runner
     * @param client An OpenSearch {@link Client}.
     */
    public OpenSearchAbstractQuerySetRunner(final Client client) {
        super(client);
    }

    @Override
    public QuerySetRunResult run(final String querySetId, final String judgmentsId, final String index, final String idField, final String query, final int k) throws Exception {

        // Get the query set.
        final SearchSourceBuilder getQuerySetSearchSourceBuilder = new SearchSourceBuilder();
        getQuerySetSearchSourceBuilder.query(QueryBuilders.matchQuery("_id", querySetId));
        getQuerySetSearchSourceBuilder.from(0);
        // TODO: Need to page through to make sure we get all of the queries.
        getQuerySetSearchSourceBuilder.size(500);

        final SearchRequest getQuerySetSearchRequest = new SearchRequest(SearchQualityEvaluationPlugin.QUERY_SETS_INDEX_NAME);
        getQuerySetSearchRequest.source(getQuerySetSearchSourceBuilder);

        try {

            // TODO: Don't use .get()
            final SearchResponse searchResponse = client.search(getQuerySetSearchRequest).get();

            // The queries from the query set that will be run.
            final Collection<Map<String, Long>> queries = (Collection<Map<String, Long>>) searchResponse.getHits().getAt(0).getSourceAsMap().get("queries");

            // The results of each query.
            final List<QueryResult> queryResults = new ArrayList<>();

            for(Map<String, Long> queryMap : queries) {

                // Loop over each query in the map and run each one.
                for (final String userQuery : queryMap.keySet()) {

                    // Replace the query placeholder with the user query.
                    final String q = query.replace(QUERY_PLACEHOLDER, userQuery);

                    // Build the query from the one that was passed in.
                    final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                    searchSourceBuilder.query(QueryBuilders.wrapperQuery(q));
                    searchSourceBuilder.from(0);
                    searchSourceBuilder.size(k);

                    String[] includeFields = new String[] {idField};
                    String[] excludeFields = new String[] {};
                    searchSourceBuilder.fetchSource(includeFields, excludeFields);

                    // TODO: Allow for setting this index name.
                    final SearchRequest searchRequest = new SearchRequest(index);
                    getQuerySetSearchRequest.source(searchSourceBuilder);

                    client.search(searchRequest, new ActionListener<>() {

                        @Override
                        public void onResponse(final SearchResponse searchResponse) {

                            final List<String> orderedDocumentIds = new ArrayList<>();

                            for (final SearchHit hit : searchResponse.getHits().getHits()) {

                                final Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                                final String documentId = sourceAsMap.get(idField).toString();

                                orderedDocumentIds.add(documentId);

                            }

                            // TODO: Use getJudgment() to get the judgment for this document.
                            final List<Double> relevanceScores = getRelevanceScores(query, orderedDocumentIds, k);

                            final SearchMetric dcgSearchMetric = new DcgSearchMetric(k, relevanceScores);
                            // TODO: Add these metrics in, too.
                            //final SearchMetric ndcgSearchmetric = new NdcgSearchMetric(k, relevanceScores, idealRelevanceScores);
                            //final SearchMetric precisionSearchMetric = new PrecisionSearchMetric(k, relevanceScores);

                            final Collection<SearchMetric> searchMetrics = List.of(dcgSearchMetric); // ndcgSearchmetric, precisionSearchMetric);

                            queryResults.add(new QueryResult(userQuery, orderedDocumentIds, k, searchMetrics));

                        }

                        @Override
                        public void onFailure(Exception ex) {
                            LOGGER.error("Unable to search for query: {}", query, ex);
                        }
                    });

                }

            }

            // TODO: Calculate the search metrics for the entire query set given the results and the judgments.
            final List<String> orderedDocumentIds = new ArrayList<>();
            final List<Double> relevanceScores = getRelevanceScores(query, orderedDocumentIds, k);
            final SearchMetric dcgSearchMetric = new DcgSearchMetric(k, relevanceScores);
            // TODO: Add these metrics in, too.
            //final SearchMetric ndcgSearchmetric = new NdcgSearchMetric(k, relevanceScores, idealRelevanceScores);
            //final SearchMetric precisionSearchMetric = new PrecisionSearchMetric(k, relevanceScores);

            final Collection<SearchMetric> searchMetrics = List.of(dcgSearchMetric); // ndcgSearchmetric, precisionSearchMetric);

            return new QuerySetRunResult(queryResults, searchMetrics);

        } catch (Exception ex) {
            throw new RuntimeException("Unable to run query set.", ex);
        }

    }

    @Override
    public void save(final QuerySetRunResult result) throws Exception {

        // Index the results into OpenSearch.

        final Map<String, Object> results = new HashMap<>();

        results.put("run_id", result.getRunId());
        results.put("query_results", result.getQueryResultsAsMap());

        // Calculate and add each metric to the object to index.
        for(final SearchMetric searchMetric : result.getSearchMetrics()) {
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

    public List<Double> getRelevanceScores(final String query, final List<String> orderedDocumentIds, final int k) {

        // Ordered list of scores.
        final List<Double> scores = new ArrayList<>();

        // Go through each document up to k and get the score.
        for(int i = 0; i < k; i++) {

            final String documentId = orderedDocumentIds.get(i);

            // TODO: Find the judgment value for this combination of query and documentId from the index.
            final double judgment = 0.1;

            scores.add(judgment);

            if(i == orderedDocumentIds.size()) {
                // k is greater than the actual length of documents.
                break;
            }

        }

        String listOfScores = scores.stream().map(Object::toString).collect(Collectors.joining(", "));
        LOGGER.info("Got relevance scores: {}", listOfScores);

        return scores;

    }

}
