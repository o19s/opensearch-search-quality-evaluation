package org.opensearch.eval.engine;

import org.opensearch.eval.model.ClickthroughRate;
import org.opensearch.eval.model.data.Judgment;
import org.opensearch.eval.model.data.QuerySet;
import org.opensearch.eval.model.ubi.query.UbiQuery;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class SearchEngine {

    public abstract boolean doesIndexExist(String index) throws IOException;
    public abstract boolean createIndex(String index, Map<String, Object> mapping) throws IOException;
    public abstract boolean deleteIndex(String index) throws IOException;

    public abstract String getUserQuery(final String queryId) throws Exception;
    public abstract UbiQuery getQueryFromQueryId(final String queryId) throws Exception;
    public abstract long getCountOfQueriesForUserQueryHavingResultInRankR(final String userQuery, final String objectId, final int rank) throws Exception;
    public abstract void indexRankAggregatedClickthrough(final Map<Integer, Double> rankAggregatedClickThrough) throws Exception;
    public abstract void indexClickthroughRates(final Map<String, Set<ClickthroughRate>> clickthroughRates) throws Exception;
    public abstract String indexJudgments(final Collection<Judgment> judgments) throws Exception;

    public abstract boolean bulkIndex(String index, Map<String, Object> documents) throws IOException;

    public abstract Collection<Judgment> getJudgments(final String index) throws IOException;

    public abstract String indexQuerySet(QuerySet querySet) throws IOException;
    public abstract Collection<UbiQuery> getUbiQueries() throws IOException;

    /**
     * Gets a query set from the index.
     * @param querySetId The ID of the query set to get.
     * @return The query set as a collection of maps of query to frequency
     * @throws IOException Thrown if the query set cannot be retrieved.
     */
    public abstract QuerySet getQuerySet(String querySetId) throws IOException;

    /**
     * Get a judgment from the index.
     * @param judgmentsId The ID of the judgments to find.
     * @param query The user query.
     * @param documentId The document ID.
     * @return The value of the judgment, or <code>NaN</code> if the judgment cannot be found.
     */
    public abstract Double getJudgmentValue(final String judgmentsId, final String query, final String documentId) throws Exception;

}
