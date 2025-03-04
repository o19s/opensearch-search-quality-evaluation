/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model.dao.querysets;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensearch.eval.model.dao.AbstractData;

import java.util.List;
import java.util.UUID;

public class QueryRunResults extends AbstractData {

    public QueryRunResults() {
        super(UUID.randomUUID().toString());
    }

    @JsonProperty("query_set_id")
    private String querySetId;

    @JsonProperty("result_set")
    private List<String> resultSet;

    @JsonProperty("user_query")
    private String user_query;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("evaluation_id")
    private String evaluationId;

    @JsonProperty("number_of_results")
    private int numberOfResults;

    public String getQuerySetId() {
        return querySetId;
    }

    public void setQuerySetId(String querySetId) {
        this.querySetId = querySetId;
    }

    public List<String> getResultSet() {
        return resultSet;
    }

    public void setResultSet(List<String> resultSet) {
        this.resultSet = resultSet;
    }

    public String getUser_query() {
        return user_query;
    }

    public void setUser_query(String user_query) {
        this.user_query = user_query;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(String evaluationId) {
        this.evaluationId = evaluationId;
    }

    public int getNumberOfResults() {
        return numberOfResults;
    }

    public void setNumberOfResults(int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

}
