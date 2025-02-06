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

import java.util.Map;

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

    @JsonProperty("judgment_set_type")
    private String judgmentSetType;

    @JsonProperty("judgment_set_generator")
    private String judgmentSetGenerator;

    @JsonProperty("judgment_set_name")
    private String judgmentSetName;

    @JsonProperty("judgment_set_description")
    private String judgmentSetDescription;

    @JsonProperty("judgment_set_parameters")
    private Map<String, Object> judgmentSetParameters;

    @JsonProperty("timestamp")
    private String timestamp;

    public Judgment() {
        // Empty constructor used for deserialization.
    }

    public String toJudgmentString() {
        return queryId + ", " + query + ", " + document + ", " + MathUtils.round(judgment);
    }

    @Override
    public String toString() {
        return "query_id: " + queryId + ", query: " + query + ", document: " + document + ", judgment: " + MathUtils.round(judgment);
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public void setJudgment(double judgment) {
        this.judgment = judgment;
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

    public String getJudgmentSetType() {
        return judgmentSetType;
    }

    public void setJudgmentSetType(String judgmentSetType) {
        this.judgmentSetType = judgmentSetType;
    }

    public String getJudgmentSetGenerator() {
        return judgmentSetGenerator;
    }

    public void setJudgmentSetGenerator(String judgmentSetGenerator) {
        this.judgmentSetGenerator = judgmentSetGenerator;
    }

    public String getJudgmentSetName() {
        return judgmentSetName;
    }

    public void setJudgmentSetName(String judgmentSetName) {
        this.judgmentSetName = judgmentSetName;
    }

    public String getJudgmentSetDescription() {
        return judgmentSetDescription;
    }

    public void setJudgmentSetDescription(String judgmentSetDescription) {
        this.judgmentSetDescription = judgmentSetDescription;
    }

    public Map<String, Object> getJudgmentSetParameters() {
        return judgmentSetParameters;
    }

    public void setJudgmentSetParameters(Map<String, Object> judgmentSetParameters) {
        this.judgmentSetParameters = judgmentSetParameters;
    }

}
