/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.runners;

import java.util.List;

/**
 * Contains the search results for a single query.
 */
public class QueryResult {

    private final String query;
    private final List<String> orderedDocumentIds;
    private final SearchMetrics searchMetrics;

    /**
     * Creates the search results.
     * @param query The query used to generate this result.
     * @param orderedDocumentIds A list of ordered document IDs in the same order as they appeared
     *                           in the query.
     * @param k The k used for metrics calculation, i.e. DCG@k.
     */
    public QueryResult(final String query, final List<String> orderedDocumentIds, final int k) {
        this.query = query;
        this.orderedDocumentIds = orderedDocumentIds;
        this.searchMetrics = new SearchMetrics(query, orderedDocumentIds, k);
    }

    /**
     * Gets the query used to generate this result.
     * @return The query used to generate this result.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Gets the list of ordered document IDs.
     * @return A list of ordered documented IDs.
     */
    public List<String> getOrderedDocumentIds() {
        return orderedDocumentIds;
    }

    /**
     * Gets the search metrics for this query.
     * @return The {@link SearchMetrics} for this query.
     */
    public SearchMetrics getSearchMetrics() {
        return searchMetrics;
    }

}
