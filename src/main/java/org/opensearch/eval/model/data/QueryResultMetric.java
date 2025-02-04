package org.opensearch.eval.model.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class QueryResultMetric extends AbstractData {

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("search_config")
    private String searchConfig;

    @JsonProperty("query_set_id")
    private String querySetId;

    @JsonProperty("query")
    private String query;

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

    public QueryResultMetric(String id) {
        super(id);
    }

    public QueryResultMetric() {
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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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

}
