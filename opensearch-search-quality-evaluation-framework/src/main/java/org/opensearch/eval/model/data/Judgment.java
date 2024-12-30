/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.utils.MathUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * A judgment of a search result's quality for a given query.
 */
public class Judgment extends AbstractData {

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
