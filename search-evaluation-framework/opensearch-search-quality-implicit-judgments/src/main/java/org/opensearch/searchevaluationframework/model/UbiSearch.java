package org.opensearch.searchevaluationframework.model;

import org.opensearch.search.SearchHit;

import java.util.List;
import java.util.Map;

public class UbiSearch {

    private final long timestamp;
    private final String queryId;
    private final String clientId;
    private final String userQuery;
    private final String query;
    private Map<String, String> queryAttributes;
    private QueryResponse queryResponse;

    public UbiSearch(final SearchHit hit) {

        this.timestamp = Long.parseLong(hit.getSourceAsMap().get("timestamp").toString());
        this.queryId = hit.getSourceAsMap().get("query_id").toString();
        this.clientId = hit.getSourceAsMap().get("client_id").toString();
        this.userQuery = hit.getSourceAsMap().get("user_query").toString();
        this.query = hit.getSourceAsMap().get("query").toString();

        //this.queryResponse =

    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getQueryId() {
        return queryId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUserQuery() {
        return userQuery;
    }

    public String getQuery() {
        return query;
    }

    public Map<String, String> getQueryAttributes() {
        return queryAttributes;
    }

    public QueryResponse getQueryResponse() {
        return queryResponse;
    }

}
