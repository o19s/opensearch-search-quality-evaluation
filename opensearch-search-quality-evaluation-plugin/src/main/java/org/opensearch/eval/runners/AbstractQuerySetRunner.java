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
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.Client;
import org.opensearch.eval.SearchQualityEvaluationPlugin;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base class for query set runners. Classes that extend this class
 * should be specific to a search engine. See the {@link OpenSearchQuerySetRunner} for an example.
 */
public abstract class AbstractQuerySetRunner {

    private static final Logger LOGGER = LogManager.getLogger(AbstractQuerySetRunner.class);

    protected final Client client;

    public AbstractQuerySetRunner(final Client client) {
        this.client = client;
    }

    /**
     * Runs the query set.
     * @param querySetId The ID of the query set to run.
     * @param judgmentsId The ID of the judgments set to use for search metric calculation.
     * @param index The name of the index to run the query sets against.
     * @param idField The field in the index that is used to uniquely identify a document.
     * @param query The query that will be used to run the query set.
     * @param k The k used for metrics calculation, i.e. DCG@k.
     * @return The query set {@link QuerySetRunResult results} and calculated metrics.
     */
    abstract QuerySetRunResult run(String querySetId, final String judgmentsId, final String index, final String idField, final String query, final int k) throws Exception;

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
        sourceBuilder.trackTotalHits(true);

        final SearchRequest searchRequest = new SearchRequest(SearchQualityEvaluationPlugin.QUERY_SETS_INDEX_NAME).source(sourceBuilder);

        // TODO: Don't use .get()
        final SearchResponse searchResponse = client.search(searchRequest).get();

        if(searchResponse.getHits().getTotalHits().value > 0) {

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
    public Double getJudgmentValue(final String judgmentsId, final String query, final String documentId) {

        // Find a judgment that matches the judgments_id, query_id, and document_id fields in the index.

        final BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("judgments_id", judgmentsId));
        boolQueryBuilder.must(QueryBuilders.matchQuery("query", query));
        boolQueryBuilder.must(QueryBuilders.matchQuery("document_id", documentId));

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);

        // Will be a max of 1 result since we are getting the judgments by ID.
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);

        // Only include the judgment field.
        final String[] includeFields = new String[] {"judgment"};
        final String[] excludeFields = new String[] {};
        searchSourceBuilder.fetchSource(includeFields, excludeFields);

        final SearchRequest searchRequest = new SearchRequest(SearchQualityEvaluationPlugin.JUDGMENTS_INDEX_NAME);
        searchRequest.source(searchSourceBuilder);

        try {

            // TODO: Don't use .get()
            final SearchResponse searchResponse = client.search(searchRequest).get();

            if (searchResponse.getHits().getHits().length > 0) {

                final Map<String, Object> j = searchResponse.getHits().getAt(0).getSourceAsMap();
                return Double.parseDouble(j.get("judgment").toString());

            } else {

                LOGGER.warn("Unable to find judgments with ID {} for query {} and document ID {}", judgmentsId, query, documentId);

                // TODO: What to return here when there is no judgment?
                return Double.NaN;

            }

        } catch (Exception ex) {

            LOGGER.error("Unable to run query to find judgment for judgmentsIs {}, query = {}, documentId = {}", judgmentsId, query, documentId, ex);
            throw new RuntimeException("Unable to run query to find judgment.", ex);

        }

    }

    public List<Double> getRelevanceScores(final String judgmentsId, final String query, final List<String> orderedDocumentIds, final int k) {

        // Ordered list of scores.
        final List<Double> scores = new ArrayList<>();

        // Go through each document up to k and get the score.
        for (int i = 0; i < k; i++) {

            final String documentId = orderedDocumentIds.get(i);

            // Find the judgment value for this combination of query and documentId from the index.
            final double judgmentValue = getJudgmentValue(judgmentsId, query, documentId);

            // If a judgment for this query/doc pair is not found, Double.NaN will be returned.
            if(!Double.isNaN(judgmentValue)) {
                scores.add(judgmentValue);
            }

            if (i == orderedDocumentIds.size()) {
                // k is greater than the actual length of documents.
                break;
            }

        }

        String listOfScores = scores.stream().map(Object::toString).collect(Collectors.joining(", "));
        LOGGER.info("Got relevance scores: {}", listOfScores);

        return scores;

    }

}
