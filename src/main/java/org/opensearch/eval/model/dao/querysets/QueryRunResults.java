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

    @JsonProperty("query")
    private String query;

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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
