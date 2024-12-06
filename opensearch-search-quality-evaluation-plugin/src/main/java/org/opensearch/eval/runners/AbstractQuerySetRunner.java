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

import java.util.Map;

/**
 * Base class for query set runners. Classes that extend this class
 * should be specific to a search engine. See the {@link OpenSearchAbstractQuerySetRunner} for an example.
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
     * Get a judgment from the index.
     * @param judgmentsId The judgements ID the judgment to find belongs to.
     * @param query The user query.
     * @param documentId The document ID.
     * @return The value of the judgment, or <code>NaN</code> if the judgment cannot be found.
     * @throws Exception Thrown if the indexed cannot be queried for the judgment.
     */
    public Double getJudgment(final String judgmentsId, final String query, final String documentId) throws Exception {

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
        String[] includeFields = new String[] {"judgment"};
        String[] excludeFields = new String[] {};
        searchSourceBuilder.fetchSource(includeFields, excludeFields);

        final SearchRequest searchRequest = new SearchRequest(SearchQualityEvaluationPlugin.JUDGMENTS_INDEX_NAME);
        searchRequest.source(searchSourceBuilder);

        // TODO: Don't use .get()
        final SearchResponse searchResponse = client.search(searchRequest).get();

        if(searchResponse.getHits().getHits().length == 0) {

            // The judgments_id is probably not valid.
            return Double.NaN;

        } else {

            final Map<String, Object> j = searchResponse.getHits().getAt(0).getSourceAsMap();
            return Double.parseDouble(j.get("judgment").toString());

        }

    }

}
