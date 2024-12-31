package org.opensearch.eval.model.data;

import java.util.UUID;

public class QueryResultMetric extends AbstractData {

    private String datetime;
    private String searchConfig;
    private String querySetId;
    private String query;
    private String metric;
    private double value;
    private String application;
    private String evaluationId;
    private double frogsPercent;

    public QueryResultMetric(String id) {
        super(id);
    }

    public QueryResultMetric() {
        super(UUID.randomUUID().toString());
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
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
