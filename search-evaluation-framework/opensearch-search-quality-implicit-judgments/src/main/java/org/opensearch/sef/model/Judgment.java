package org.opensearch.sef.model;

/**
 * A judgment of a search result's quality for a given query.
 */
public class Judgment {

    private final long timestamp;
    private final String queryId;
    private final String query;
    private final String document;
    private final double judgment;

    public Judgment(final long timestamp, final String queryId, final String query, final String document, final double judgment) {
        this.timestamp = timestamp;
        this.queryId = queryId;
        this.query = query;
        this.document = document;
        this.judgment = judgment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getQueryId() {
        return queryId;
    }

    public String getQuery() {
        return query;
    }

    public String getDocument() {
        return document;
    }

    public double getJudgment() {
        return judgment;
    }

}
