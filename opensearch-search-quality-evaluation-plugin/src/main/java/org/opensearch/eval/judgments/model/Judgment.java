/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.judgments.util.MathUtils;

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

    /**
     * Creates a new judgment.
     * @param queryId The query ID for the judgment.
     * @param query The query for the judgment.
     * @param document The document in the jdugment.
     * @param judgment The judgment value.
     */
    public Judgment(final String queryId, final String query, final String document, final double judgment) {
        this.queryId = queryId;
        this.query = query;
        this.document = document;
        this.judgment = judgment;
    }

    public String toJudgmentString() {
        return queryId + ", " + query + ", " + document + ", " + MathUtils.round(judgment);
    }

    /**
     * A convenience function to output the judgments.
     * @param judgments A collection of {@link Judgment}.
     */
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

    /**
     * Gets the judgment's query ID.
     * @return The judgment's query ID.
     */
    public String getQueryId() {
        return queryId;
    }

    /**
     * Gets the judgment's query.
     * @return The judgment's query.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Gets the judgment's document.
     * @return The judgment's document.
     */
    public String getDocument() {
        return document;
    }

    /**
     * Gets the judgment's value.
     * @return The judgment's value.
     */
    public double getJudgment() {
        return judgment;
    }

}
