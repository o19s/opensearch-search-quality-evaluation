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
import org.opensearch.core.common.util.CollectionUtils;
import org.opensearch.eval.SearchQualityEvaluationPlugin;
import org.opensearch.eval.judgments.model.Judgment;
import org.opensearch.eval.metrics.SearchMetrics;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opensearch.eval.SearchQualityEvaluationRestHandler.QUERY_PLACEHOLDER;

/**
 * A {@link QuerySetRunner} for Amazon OpenSearch.
 */
public class OpenSearchQuerySetRunner implements QuerySetRunner {

    private static final Logger LOGGER = LogManager.getLogger(OpenSearchQuerySetRunner.class);

    final Client client;

    /**
     * Creates a new query set runner
     * @param client An OpenSearch {@link Client}.
     */
    public OpenSearchQuerySetRunner(final Client client) {
        this.client = client;
    }

    @Override
    public QuerySetRunResult run(final String querySetId, final String judgmentsId, final String index, final String idField, final String query, final int k) throws Exception {

        // Get the judgments we will use for metric calculation.
        final List<Judgment> judgments = getJudgments(judgmentsId);

        if(CollectionUtils.isEmpty(judgments)) {
            // We have to have judgments to continue. The judgmentsId is either wrong or doesn't exist.
            throw new RuntimeException("Judgments are empty. Check judgment_id and try again.");
        }

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

                            queryResults.add(new QueryResult(userQuery, orderedDocumentIds, judgments, k));

                        }

                        @Override
                        public void onFailure(Exception ex) {
                            LOGGER.error("Unable to search for query: {}", query, ex);
                        }
                    });

                }

            }

            // TODO: Calculate the search metrics given the results and the judgments.
            final SearchMetrics searchMetrics = new SearchMetrics(queryResults, judgments, k);

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
        results.put("search_metrics", result.getSearchMetrics().getSearchMetricsAsMap());
        results.put("query_results", result.getQueryResultsAsMap());

        final IndexRequest indexRequest = new IndexRequest(SearchQualityEvaluationPlugin.QUERY_SETS_RUN_RESULTS_INDEX_NAME);
        indexRequest.source(results);

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

    private List<Judgment> getJudgments(final String judgmentsId) throws Exception {

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("_id", judgmentsId));
        searchSourceBuilder.trackTotalHits(true);

        final SearchRequest getQuerySetSearchRequest = new SearchRequest(SearchQualityEvaluationPlugin.JUDGMENTS_INDEX_NAME);
        getQuerySetSearchRequest.source(searchSourceBuilder);

        // TODO: Don't use .get()
        final SearchResponse searchResponse = client.search(getQuerySetSearchRequest).get();

        final List<Judgment> judgments = new ArrayList<>();

        if(searchResponse.getHits().getTotalHits().value == 0) {

            // The judgment_id is probably not valid.
            // This will return an empty list.

        } else {

            // TODO: Make sure the search gets something back.
            final Collection<Map<String, Object>> j = (Collection<Map<String, Object>>) searchResponse.getHits().getAt(0).getSourceAsMap().get("judgments");

            for (final Map<String, Object> judgment : j) {

                final String queryId = judgment.get("query_id").toString();
                final double judgmentValue = Double.parseDouble(judgment.get("judgment").toString());
                final String query = judgment.get("query").toString();
                final String document = judgment.get("document").toString();

                final Judgment jobj = new Judgment(queryId, query, document, judgmentValue);
                LOGGER.info("Judgment: {}", jobj.toJudgmentString());

                judgments.add(jobj);

            }

        }

        return judgments;

    }

}
