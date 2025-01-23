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

//         * @param querySetId The ID of the query set to run.
//     * @param judgmentsId The ID of the judgments set to use for search metric calculation.
//     * @param index The name of the index to run the query sets against.
//            * @param searchPipeline The name of the search pipeline to use, or <code>null</code> to not use a search pipeline.
//            * @param idField The field in the index that is used to uniquely identify a document.
//            * @param query The query that will be used to run the query set.
//            * @param k The k used for metrics calculation, i.e. DCG@k.
//     * @param threshold The cutoff for binary judgments. A judgment score greater than or equal
//     *                  to this value will be assigned a binary judgment value of 1. A judgment score
//     *                  less than this value will be assigned a binary judgment value of 0.

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

}
