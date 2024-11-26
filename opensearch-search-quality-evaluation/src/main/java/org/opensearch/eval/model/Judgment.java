package org.opensearch.eval.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.util.MathUtils;

import java.util.Collection;

/**
 * A judgment of a search result's quality for a given query.
 */
public class Judgment {

    private static final Logger LOGGER = LogManager.getLogger(Judgment.class.getName());

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
        return queryId + ", " + query + ", " + document + ", " + MathUtils.round(judgment);
    }

    public static void showJudgments(final Collection<Judgment> judgments) {

        LOGGER.info("query_id, query, document, judgment");

        for(final Judgment judgment : judgments) {
            LOGGER.info(judgment.toJudgmentString());
        }

    }

    @Override
    public String toString() {
        return "query_id: " + queryId + ", query: " + query + ", document: " + document + ", judgment: " + MathUtils.round(judgment);
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
