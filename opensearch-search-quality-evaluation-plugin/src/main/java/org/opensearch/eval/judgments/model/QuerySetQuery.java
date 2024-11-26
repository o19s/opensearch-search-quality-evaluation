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
