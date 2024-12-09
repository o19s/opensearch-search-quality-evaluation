/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.judgments.util.MathUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public Map<String, Object> getJudgmentAsMap() {

        final Map<String, Object> judgmentMap = new HashMap<>();
        judgmentMap.put("query_id", queryId);
        judgmentMap.put("query", query);
        judgmentMap.put("document_id", document);
        judgmentMap.put("judgment", judgment);

        return judgmentMap;

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

    /**
     * Find a judgment in a collection of judgments.
     * @param judgments The collection of {@link Judgment judgments}.
     * @param query The query to find.
     * @param documentId The document ID to find.
     * @return The matching {@link Judgment judgment}.
     */
    public static Judgment findJudgment(final Collection<Judgment> judgments, final String query, final String documentId) {

        for(final Judgment judgment : judgments) {

            // LOGGER.info("Comparing {}:{} with {}:{}", judgment.getQuery(), judgment.getDocument(), query, documentId);

            if(judgment.getQuery().equalsIgnoreCase(query) && judgment.getDocument().equalsIgnoreCase(documentId)) {
                LOGGER.info("Judgment score of {}  for query {} and document {} was found.", judgment.getJudgment(), query, documentId);
                return judgment;
            }

        }

        // A judgment for this query and document was not found.
        LOGGER.warn("A judgment for query {} and document {} was not found.", query, documentId);
        // TODO: Would this ever happen?
        return null;

    }

    @Override
    public String toString() {
        return "query_id: " + queryId + ", query: " + query + ", document: " + document + ", judgment: " + MathUtils.round(judgment);
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
