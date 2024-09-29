package org.opensearch.sef.model.ubi;

import java.util.List;

/**
 * A query response for a {@link UbiQuery query}.
 */
public class QueryResponse {

    private final String queryId;
    private final String queryResponseId;
    private final List<String> queryResponseObjectIds;

    /**
     * Creates a query response.
     * @param queryId The ID of the query.
     * @param queryResponseId The ID of the query response.
     * @param queryResponseObjectIds A list of IDs for the hits in the query.
     */
    public QueryResponse(final String queryId, final String queryResponseId, final List<String> queryResponseObjectIds) {
        this.queryId = queryId;
        this.queryResponseId = queryResponseId;
        this.queryResponseObjectIds = queryResponseObjectIds;
    }

    /**
     * Gets the query ID.
     * @return The query ID.
     */
    public String getQueryId() {
        return queryId;
    }

    /**
     * Gets the query response ID.
     * @return The query response ID.
     */
    public String getQueryResponseId() {
        return queryResponseId;
    }

    /**
     * Gets the list of query response hit IDs.
     * @return A list of query response hit IDs.
     */
    public List<String> getQueryResponseObjectIds() {
        return queryResponseObjectIds;
    }

}
