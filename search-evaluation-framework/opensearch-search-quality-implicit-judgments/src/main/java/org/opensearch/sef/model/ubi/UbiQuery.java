package org.opensearch.sef.model.ubi;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
                append(queryId).
                append(userQuery).
                append(clientId).
                toHashCode();
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
