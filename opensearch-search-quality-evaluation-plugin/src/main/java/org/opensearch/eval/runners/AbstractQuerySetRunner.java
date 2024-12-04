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
import org.opensearch.eval.judgments.model.Judgment;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    public List<Judgment> getJudgments(final String judgmentsId) throws Exception {

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("_id", judgmentsId));
        searchSourceBuilder.trackTotalHits(true);

        // Will be a max of 1 result since we are getting the judgments by ID.
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);

        final SearchRequest getQuerySetSearchRequest = new SearchRequest(SearchQualityEvaluationPlugin.JUDGMENTS_INDEX_NAME);
        getQuerySetSearchRequest.source(searchSourceBuilder);

        // TODO: Don't use .get()
        final SearchResponse searchResponse = client.search(getQuerySetSearchRequest).get();

        final List<Judgment> judgments = new ArrayList<>();

        if(searchResponse.getHits().getTotalHits().value == 0) {

            // The judgment_id is probably not valid.
            // This will return an empty list.

        } else {

            // TODO: Make sure the search gets something back.
            final Collection<Map<String, Object>> j = (Collection<Map<String, Object>>) searchResponse.getHits().getAt(0).getSourceAsMap().get("judgments");

            for (final Map<String, Object> judgment : j) {

                final String queryId = judgment.get("query_id").toString();
                final double judgmentValue = Double.parseDouble(judgment.get("judgment").toString());
                final String query = judgment.get("query").toString();
                final String document = judgment.get("document").toString();

                final Judgment jobj = new Judgment(queryId, query, document, judgmentValue);
                LOGGER.info("Judgment: {}", jobj.toJudgmentString());

                judgments.add(jobj);

            }

        }

        return judgments;

    }


}
