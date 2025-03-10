package org.opensearch.eval.model.dao.querysets;

import com.google.gson.annotations.SerializedName;

/**
 * The parameters used to run a query set.
 */
public class QuerySetRunParameters {

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
    private String application;

    @SerializedName("search_config")
    private String searchConfig;

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

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getSearchConfig() {
        return searchConfig;
    }

    public void setSearchConfig(String searchConfig) {
        this.searchConfig = searchConfig;
    }

}
