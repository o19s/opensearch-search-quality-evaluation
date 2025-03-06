package org.opensearch.eval.model.dao.searchconfigurations;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensearch.eval.model.dao.AbstractData;

import java.util.List;
import java.util.UUID;

public class SearchConfiguration extends AbstractData {

    @JsonProperty("query_set_id")
    private String querySetId;

    @JsonProperty("result_sets")
    private List<String> resultSets;

    @JsonProperty("metrics")
    private List<String> metrics;

    @JsonProperty("k")
    private int k;

    public SearchConfiguration() {
        super(UUID.randomUUID().toString());
    }

    public SearchConfiguration(String id) {
        super(id);
    }

    public String getQuerySetId() {
        return querySetId;
    }

    public void setQuerySetId(String querySetId) {
        this.querySetId = querySetId;
    }

    public List<String> getResultSets() {
        return resultSets;
    }

    public void setResultSets(List<String> resultSets) {
        this.resultSets = resultSets;
    }

    public List<String> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<String> metrics) {
        this.metrics = metrics;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

}
