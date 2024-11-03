package org.opensearch.qef.model.ubi.query;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

/**
 * A UBI query.
 */
public class UbiQuery {

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("query_id")
    private String queryId;

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("user_query")
    private String userQuery;

    @SerializedName("query")
    private String query;

    @SerializedName("query_attributes")
    private Map<String, String> queryAttributes;

    @SerializedName("query_response")
    private QueryResponse queryResponse;

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

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(String userQuery) {
        this.userQuery = userQuery;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, String> getQueryAttributes() {
        return queryAttributes;
    }

    public void setQueryAttributes(Map<String, String> queryAttributes) {
        this.queryAttributes = queryAttributes;
    }

    public QueryResponse getQueryResponse() {
        return queryResponse;
    }

    public void setQueryResponse(QueryResponse queryResponse) {
        this.queryResponse = queryResponse;
    }

}
