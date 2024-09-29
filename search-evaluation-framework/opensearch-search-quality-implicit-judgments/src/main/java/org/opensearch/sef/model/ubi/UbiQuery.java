package org.opensearch.sef.model.ubi;

import org.opensearch.search.SearchHit;

import java.util.Map;

/**
 * A UBI query.
 */
public class UbiQuery {

    private final long timestamp;
    private final String queryId;
    private final String clientId;
    private final String userQuery;
    private final String query;
    private Map<String, String> queryAttributes;
    private QueryResponse queryResponse;

    public UbiQuery(final SearchHit hit) {

        this.timestamp = Long.parseLong(hit.getSourceAsMap().get("timestamp").toString());
        this.queryId = hit.getSourceAsMap().get("query_id").toString();
        this.clientId = hit.getSourceAsMap().get("client_id").toString();
        this.userQuery = hit.getSourceAsMap().get("user_query").toString();
        this.query = hit.getSourceAsMap().get("query").toString();

        // TODO: Maybe make this optional so it's only done when needed?
        //this.queryResponse =

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        final UbiQuery ubiQuery = (UbiQuery) o;
        return queryId.equals(ubiQuery.queryId) && (userQuery.equals(ubiQuery.userQuery));
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 29 * result + queryId.hashCode();
        result = 29 * result + userQuery.hashCode();
        return result;
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
