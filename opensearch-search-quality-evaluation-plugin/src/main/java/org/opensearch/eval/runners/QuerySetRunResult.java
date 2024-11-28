/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.runners;

import java.util.Collection;

public class QuerySetRunResult {

    private final Collection<QueryResult> queryResults;
    private final SearchMetrics searchMetrics;

    public QuerySetRunResult(final Collection<QueryResult> queryResults, final SearchMetrics searchMetrics) {
        this.queryResults = queryResults;
        this.searchMetrics = searchMetrics;
    }

    public SearchMetrics getSearchMetrics() {
        return searchMetrics;
    }

    public Collection<QueryResult> getQueryResults() {
        return queryResults;
    }

}
