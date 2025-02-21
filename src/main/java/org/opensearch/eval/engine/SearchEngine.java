package org.opensearch.eval.engine;

import org.opensearch.eval.model.ClickthroughRate;
import org.opensearch.eval.model.data.Judgment;
import org.opensearch.eval.model.data.QueryResultMetric;
import org.opensearch.eval.model.data.QuerySet;
import org.opensearch.eval.model.ubi.query.UbiQuery;
import org.opensearch.eval.runners.QuerySetRunResult;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for the underlying search engine functionality.
 */
public abstract class SearchEngine {

    /**
     * Determines if an index exists.
     * @param index The name of the index.
     * @return <code>true</code> if the index exists.
     * @throws IOException Thrown if unable to determine if the index exists.
     */
    public abstract boolean doesIndexExist(String index) throws IOException;

    /**
     * Creates an index from a given mapping.
     * @param index The name of the index.
     * @param mapping The mapping JSON.
     * @return <code>true</code> if the index was created successfully.
     * @throws IOException Thrown if the index could not be created.
     */
    public abstract boolean createIndex(String index, String mapping) throws IOException;

    /**
     * Get a user query for a given query ID.
     * @param queryId The query ID.
     * @return The user query.
     * @throws Exception Thrown if the user query cannot be retrieved.
     */
    public abstract String getUserQuery(final String queryId) throws Exception;

    /**
     * Get a {@link UbiQuery} from a query ID.
     * @param queryId The query ID.
     * @return a {@link UbiQuery}.
     * @throws Exception Thrown if the UBI query cannot be retrieved.
     */
    public abstract UbiQuery getQueryFromQueryId(final String queryId) throws Exception;

    /**
     * Get the count of queries for a user query that have a specific document in rank R.
     * @param userQuery The user query.
     * @param objectId The document ID.
     * @param rank The rank R.
     * @return A count of queries for a user query that have a specific document in rank R.
     * @throws Exception Thrown if the count cannot be retrieved.
     */
    public abstract long getCountOfQueriesForUserQueryHavingResultInRankR(final String userQuery, final String objectId, final int rank) throws Exception;

    /**
     * Index the rank-aggregated clickthrough values.
     * @param rankAggregatedClickThrough A map of positions to clickthroughs.
     * @throws Exception Thrown if the rank-aggregated clickthrough map cannot be indexed.
     */
    public abstract void indexRankAggregatedClickthrough(final Map<Integer, Double> rankAggregatedClickThrough) throws Exception;

    /**
     * Index the clickthrough rates.
     * @param clickthroughRates A map of documents to clickthrough rates.
     * @throws Exception Thrown if the clickthrough rates cannot be indexed.
     */
    public abstract void indexClickthroughRates(final Map<String, Set<ClickthroughRate>> clickthroughRates) throws Exception;

    /**
     * Index the judgments.
     * @param judgments A collection of {@link Judgment}.
     * @return The judgment set ID.
     * @throws Exception Thrown if the judgments cannot be indexed.
     */
    public abstract String indexJudgments(final Collection<Judgment> judgments) throws Exception;

    /**
     * Index the query result metrics.
     * @param queryResultMetric The {@link QueryResultMetric}.
     * @throws Exception Thrown if the metrics cannot be indexed.
     */
    public abstract void indexQueryResultMetric(final QueryResultMetric queryResultMetric) throws Exception;

    /**
     * Bulk index a set of documents.
     * @param index The index to use.
     * @param documents A map of document IDs to documents.
     * @return <code>true</code> if the bulk index completed without error.
     * @throws IOException Thrown if the bulk index encounters an error.
     */
    public abstract boolean bulkIndex(String index, Map<String, Object> documents) throws IOException;

    /**
     * Get all judgments.
     * @return A collection of {@link Judgment}.
     * @throws IOException Thrown if the judgments cannot be retrieved.
     */
    public abstract Collection<Judgment> getJudgments() throws IOException;

    /**
     * Get the count of judgments for a given judgment set.
     * @param judgmentsSetId The judgment set ID.
     * @return The count of judgments for the given judgment set.
     * @throws IOException Thrown if the judgment count cannot be retrieved.
     */
    public abstract long getJudgmentsCount(final String judgmentsSetId) throws IOException;

    /**
     * Run a user query from a query set.
     * @param index The index to run the query against.
     * @param query The search engine query that will be run.
     * @param k The value of k (the depth of the search results).
     * @param userQuery The user query.
     * @param idField The field in the index that uniquely identifies each document in the index.
     * @param pipeline The search pipeline. Pass <code>""</code> to not use a search pipeline.
     * @return A list of document IDs from the search result.
     * @throws IOException Thrown if the query cannot be run.
     */
    public abstract List<String> runQuery(final String index, final String query, final int k, final String userQuery, final String idField, final String pipeline) throws IOException;

    /**
     * Index a query set.
     * @param querySet The {@link QuerySet} to index.
     * @return The query set ID.
     * @throws IOException Thrown if the query set cannot be indexed.
     */
    public abstract String indexQuerySet(QuerySet querySet) throws IOException;

    /**
     * Get the top <code>n</code> UBI queries.
     * @param n The number of top queries to return.
     * @return The user queries with their frequencies.
     * @throws IOException Thrown if the UBI queries cannot be retrieved.
     */
    public abstract Map<String, Long> getUbiQueries(final int n) throws IOException;

    /**
     * Get random UBI queries.
     * @param n The number of random queries to return.
     * @return The user queries with their frequencies.
     * @throws IOException Thrown if the UBI queries cannot be retrieved.
     */
    public abstract Map<String, Long> getRandomUbiQueries(final int n) throws IOException;

    /**
     * Get all UBI queries.
     * @return A collection of all {@link UbiQuery}.
     * @throws IOException Thrown if the UBI queries cannot be retrieved.
     */
    public abstract Collection<UbiQuery> getUbiQueries() throws IOException;

    /**
     * Index a query set run result.
     * @param querySetRunResult The {@link QuerySetRunResult} to index.
     * @throws Exception Thrown if the query set run result cannot be indexed.
     */
    public abstract void indexQueryRunResult(final QuerySetRunResult querySetRunResult) throws Exception;

    /**
     * Gets a query set from the index.
     * @param querySetId The ID of the query set to get.
     * @return The query set as a collection of maps of query to frequency
     * @throws IOException Thrown if the query set cannot be retrieved.
     */
    public abstract QuerySet getQuerySet(String querySetId) throws IOException;

    /**
     * Determines if a query set exists.
     * @param querySetId The ID of the query set to get.
     * @return <code>true</code> if the query set exists.
     * @throws IOException Thrown upon an error searching.
     */
    public abstract boolean doesQuerySetExist(String querySetId) throws IOException;

    /**
     * Get a judgment from the index.
     * @param judgmentsId The ID of the judgments to find.
     * @param query The user query.
     * @param documentId The document ID.
     * @return The value of the judgment, or <code>NaN</code> if the judgment cannot be found.
     */
    public abstract Double getJudgmentValue(final String judgmentsId, final String query, final String documentId) throws Exception;

    /**
     * Gets the clickthrough rates for each query and its results.
     * @return A map of user_query to the clickthrough rate for each query result.
     * @throws IOException Thrown when a problem accessing OpenSearch.
     */
    public abstract Map<String, Set<ClickthroughRate>> getClickthroughRate(final int maxRank) throws Exception;

    /**
     * Calculate the rank-aggregated click through from the UBI events.
     * @return A map of positions to clickthrough rates.
     * @throws IOException Thrown when a problem accessing OpenSearch.
     */
    public abstract Map<Integer, Double> getRankAggregatedClickThrough(int maxRank) throws Exception;

}
