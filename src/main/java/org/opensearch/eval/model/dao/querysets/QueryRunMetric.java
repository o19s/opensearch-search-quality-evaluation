package org.opensearch.eval.model.dao.querysets;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensearch.eval.model.dao.AbstractData;

import java.util.UUID;

/**
 * The result from running a single query from a query set.
 */
public class QueryRunMetric extends AbstractData {

    @JsonProperty("query_set_run_id")
    private String querySetRunId;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("search_config")
    private String searchConfig;

    @JsonProperty("query_set_id")
    private String querySetId;

    @JsonProperty("user_query")
    private String userQuery;

    @JsonProperty("metric")
    private String metric;

    @JsonProperty("value")
    private double value;

    @JsonProperty("application")
    private String application;

    @JsonProperty("evaluation_id")
    private String evaluationId;

    @JsonProperty("frogs_percent")
    private double frogsPercent;

    public QueryRunMetric(String id) {
        super(id);
    }

    public QueryRunMetric() {
        super(UUID.randomUUID().toString());
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSearchConfig() {
        return searchConfig;
    }

    public void setSearchConfig(String searchConfig) {
        this.searchConfig = searchConfig;
    }

    public String getQuerySetId() {
        return querySetId;
    }

    public void setQuerySetId(String querySetId) {
        this.querySetId = querySetId;
    }

    public String getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(String userQuery) {
        this.userQuery = userQuery;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(String evaluationId) {
        this.evaluationId = evaluationId;
    }

    public double getFrogsPercent() {
        return frogsPercent;
    }

    public void setFrogsPercent(double frogsPercent) {
        this.frogsPercent = frogsPercent;
    }

    public String getQuerySetRunId() {
        return querySetRunId;
    }

    public void setQuerySetRunId(String querySetRunId) {
        this.querySetRunId = querySetRunId;
    }

}
