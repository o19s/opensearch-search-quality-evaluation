/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.runners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.opensearch.OpenSearchClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Base class for query set runners. Classes that extend this class
 * should be specific to a search engine. See the {@link OpenSearchQuerySetRunner} for an example.
 */
public abstract class AbstractQuerySetRunner {

    private static final Logger LOGGER = LogManager.getLogger(AbstractQuerySetRunner.class);

    protected final OpenSearchClient client;

    public AbstractQuerySetRunner(final OpenSearchClient client) {
        this.client = client;
    }

    /**
     * Runs the query set.
     * @param querySetId The ID of the query set to run.
     * @param judgmentsId The ID of the judgments set to use for search metric calculation.
     * @param index The name of the index to run the query sets against.
     * @param searchPipeline The name of the search pipeline to use, or <code>null</code> to not use a search pipeline.
     * @param idField The field in the index that is used to uniquely identify a document.
     * @param query The query that will be used to run the query set.
     * @param k The k used for metrics calculation, i.e. DCG@k.
     * @param threshold The cutoff for binary judgments. A judgment score greater than or equal
     *                  to this value will be assigned a binary judgment value of 1. A judgment score
     *                  less than this value will be assigned a binary judgment value of 0.
     * @return The query set {@link QuerySetRunResult results} and calculated metrics.
     */
    abstract QuerySetRunResult run(String querySetId, final String judgmentsId, final String index, final String searchPipeline,
                                   final String idField, final String query, final int k,
                                   final double threshold) throws Exception;

    /**
     * Saves the query set results to a persistent store, which may be the search engine itself.
     * @param result The {@link QuerySetRunResult results}.
     */
    abstract void save(QuerySetRunResult result) throws Exception;

    /**
     * Gets a query set from the index.
     * @param querySetId The ID of the query set to get.
     * @return The query set as a collection of maps of query to frequency
     * @throws Exception Thrown if the query set cannot be retrieved.
     */
    public final Collection<Map<String, Long>> getQuerySet(final String querySetId) throws Exception {

        // Get the query set.
        final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("_id", querySetId));

        // Will be at most one match.
        sourceBuilder.from(0);
        sourceBuilder.size(1);

        final SearchRequest searchRequest = new SearchRequest(Constants.QUERY_SETS_INDEX_NAME).source(sourceBuilder);

        // TODO: Don't use .get()
        final SearchResponse searchResponse = client.search(searchRequest).get();

        if(searchResponse.getHits().getHits().length > 0) {

            // The queries from the query set that will be run.
            return (Collection<Map<String, Long>>) searchResponse.getHits().getAt(0).getSourceAsMap().get("queries");

        } else {

            LOGGER.error("Unable to get query set with ID {}", querySetId);

            // The query set was not found.
            throw new RuntimeException("The query set with ID " + querySetId + " was not found.");

        }

    }

    /**
     * Get a judgment from the index.
     * @param judgmentsId The ID of the judgments to find.
     * @param query The user query.
     * @param documentId The document ID.
     * @return The value of the judgment, or <code>NaN</code> if the judgment cannot be found.
     */
    public Double getJudgmentValue(final String judgmentsId, final String query, final String documentId) throws Exception {

        // Find a judgment that matches the judgments_id, query_id, and document_id fields in the index.

        final BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("judgments_id", judgmentsId));
        boolQueryBuilder.must(QueryBuilders.termQuery("query", query));
        boolQueryBuilder.must(QueryBuilders.termQuery("document_id", documentId));

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);

        // Will be a max of 1 result since we are getting the judgments by ID.
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);

        // Only include the judgment field in the response.
        final String[] includeFields = new String[] {"judgment"};
        final String[] excludeFields = new String[] {};
        searchSourceBuilder.fetchSource(includeFields, excludeFields);

        final SearchRequest searchRequest = new SearchRequest(Constants.JUDGMENTS_INDEX_NAME).source(searchSourceBuilder);

        Double judgment = Double.NaN;

        final SearchResponse searchResponse = client.search(searchRequest).get();

        if (searchResponse.getHits().getHits().length > 0) {

            final Map<String, Object> j = searchResponse.getHits().getAt(0).getSourceAsMap();

            // LOGGER.debug("Judgment contains a value: {}", j.get("judgment"));

            // TODO: Why does this not exist in some cases?
            if(j.containsKey("judgment")) {
                judgment = (Double) j.get("judgment");
            }

        } else {

            // No judgment for this query/doc pair exists.
            judgment = Double.NaN;

        }

        return judgment;

    }

    /**
     * Gets the judgments for a query / document pairs.
     * @param judgmentsId The judgments collection for which the judgment to retrieve belongs.
     * @param query The user query.
     * @param orderedDocumentIds A list of document IDs returned for the user query.
     * @param k The k used for metrics calculation, i.e. DCG@k.
     * @return An ordered list of relevance scores for the query / document pairs.
     * @throws Exception Thrown if a judgment cannot be retrieved.
     */
    protected RelevanceScores getRelevanceScores(final String judgmentsId, final String query, final List<String> orderedDocumentIds, final int k) throws Exception {

        // Ordered list of scores.
        final List<Double> scores = new ArrayList<>();

        // Count the number of documents without judgments.
        int documentsWithoutJudgmentsCount = 0;

        // For each document (up to k), get the judgment for the document.
        for (int i = 0; i < k && i < orderedDocumentIds.size(); i++) {

            final String documentId = orderedDocumentIds.get(i);

            // Find the judgment value for this combination of query and documentId from the index.
            final Double judgmentValue = getJudgmentValue(judgmentsId, query, documentId);

            // If a judgment for this query/doc pair is not found, Double.NaN will be returned.
            if(!Double.isNaN(judgmentValue)) {
                LOGGER.info("Score found for document ID {} with judgments {} and query {} = {}", documentId, judgmentsId, query, judgmentValue);
                scores.add(judgmentValue);
            } else {
                //LOGGER.info("No score found for document ID {} with judgments {} and query {}", documentId, judgmentsId, query);
                documentsWithoutJudgmentsCount++;
            }

        }

        double frogs = ((double) documentsWithoutJudgmentsCount) / orderedDocumentIds.size();

        if(Double.isNaN(frogs)) {
            frogs = 1.0;
        }

        // Multiply by 100 to be a percentage.
        frogs *= 100;

        LOGGER.info("frogs for query {} = {} ------- {} / {}", query, frogs, documentsWithoutJudgmentsCount, orderedDocumentIds.size());

        return new RelevanceScores(scores, frogs);

    }

}
