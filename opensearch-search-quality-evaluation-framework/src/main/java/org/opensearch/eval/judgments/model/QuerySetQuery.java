/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.model;

public class QuerySetQuery {

    private final String query;
    private final long frequency;

    public QuerySetQuery(final String query, final long frequency) {
        this.query = query;
        this.frequency = frequency;
    }

    public String getQuery() {
        return query;
    }

    public long getFrequency() {
        return frequency;
    }

}
