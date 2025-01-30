/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensearch.eval.utils.MathUtils;

/**
 * A judgment of a search result's quality for a given query.
 */
public class Judgment {

    @JsonProperty("id")
    private String id;

    @JsonProperty("query_id")
    private String queryId;

    @JsonProperty("query")
    private String query;

    @JsonProperty("document")
    private String document;

    @JsonProperty("judgment")
    private double judgment;

    @JsonProperty("judgment_set_id")
    private String judgmentSetId;

    @JsonProperty("timestamp")
    private String timestamp;

    public Judgment() {
        // Empty constructor used for deserialization.
    }

    /**
     * Creates a new judgment.
     * @param queryId The query ID for the judgment.
     * @param query The query for the judgment.
     * @param document The document in the judgment.
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

    public String getJudgmentSetId() {
        return judgmentSetId;
    }

    public void setJudgmentSetId(String judgmentSetId) {
        this.judgmentSetId = judgmentSetId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
