package org.opensearch.qef.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.opensearch.qef.Utils;

/**
 * A judgment of a search result's quality for a given query.
 */
public class Judgment {

    private final String queryId;
    private final String query;
    private final String document;
    private final double judgment;

    public Judgment(final String queryId, final String query, final String document, final double judgment) {
        this.queryId = queryId;
        this.query = query;
        this.document = document;
        this.judgment = judgment;
    }

    public String toJudgmentString() {
        return queryId + ":" + query + ":" + document + ":" + Utils.toSignificantFiguresString(judgment, 6);
    }

    @Override
    public String toString() {
        return "query_id: " + queryId + ", query: " + query + ", document: " + document + ", judgment: " + judgment;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
                append(queryId).
                append(query).
                append(document).
                append(judgment).
                toHashCode();
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
