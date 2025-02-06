package org.opensearch.eval.runners;

import com.google.gson.annotations.SerializedName;

public class RunQuerySetParameters {

    @SerializedName("query_set_id")
    private String querySetId;

    @SerializedName("judgments_id")
    private String judgmentsId;

    @SerializedName("index")
    private String index;

    @SerializedName("search_pipeline")
    private String searchPipeline;

    @SerializedName("id_field")
    private String idField;

    @SerializedName("query")
    private String query;

    @SerializedName("k")
    private int k;

    @SerializedName("threshold")
    private double threshold;

    @SerializedName("application")
    private double application;

    @SerializedName("search_config")
    private double searchConfig;

    public String getQuerySetId() {
        return querySetId;
    }

    public void setQuerySetId(String querySetId) {
        this.querySetId = querySetId;
    }

    public String getJudgmentsId() {
        return judgmentsId;
    }

    public void setJudgmentsId(String judgmentsId) {
        this.judgmentsId = judgmentsId;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getSearchPipeline() {
        return searchPipeline;
    }

    public void setSearchPipeline(String searchPipeline) {
        this.searchPipeline = searchPipeline;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getApplication() {
        return application;
    }

    public void setApplication(double application) {
        this.application = application;
    }

    public double getSearchConfig() {
        return searchConfig;
    }

    public void setSearchConfig(double searchConfig) {
        this.searchConfig = searchConfig;
    }

}
