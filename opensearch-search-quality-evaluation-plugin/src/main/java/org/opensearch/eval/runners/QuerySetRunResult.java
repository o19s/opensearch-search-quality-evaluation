/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.runners;

import org.opensearch.eval.metrics.SearchMetrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The results of a query set run.
 */
public class QuerySetRunResult {

    private final String runId;
    private final List<QueryResult> queryResults;
    private final SearchMetrics searchMetrics;

    /**
     * Creates a new query set run result. A random UUID is generated as the run ID.
     * @param queryResults A collection of {@link QueryResult} that contains the queries and search results.
     * @param searchMetrics The {@link SearchMetrics metrics} calculated from the search results.
     */
    public QuerySetRunResult(final List<QueryResult> queryResults, final SearchMetrics searchMetrics) {
        this.runId = UUID.randomUUID().toString();
        this.queryResults = queryResults;
        this.searchMetrics = searchMetrics;
    }

    /**
     * Get the run's ID.
     * @return The run's ID.
     */
    public String getRunId() {
        return runId;
    }

    /**
     * Gets the {@link SearchMetrics metrics} calculated from the run.
     * @return The {@link SearchMetrics metrics} calculated from the run.
     */
    public SearchMetrics getSearchMetrics() {
        return searchMetrics;
    }

    /**
     * Gets the results of the query set run.
     * @return A collection of {@link QueryResult results}.
     */
    public Collection<QueryResult> getQueryResults() {
        return queryResults;
    }

    public Collection<Map<String, Object>> getQueryResultsAsMap() {

        final Collection<Map<String, Object>> qs = new ArrayList<>();

        for(final QueryResult queryResult : queryResults) {

            final Map<String, Object> q = new HashMap<>();

            q.put("query", queryResult.getQuery());
            q.put("document_ids", queryResult.getOrderedDocumentIds());
            q.put("search_metrics", queryResult.getSearchMetrics().getSearchMetricsAsMap());

            qs.add(q);

        }

        return qs;

    }


}
