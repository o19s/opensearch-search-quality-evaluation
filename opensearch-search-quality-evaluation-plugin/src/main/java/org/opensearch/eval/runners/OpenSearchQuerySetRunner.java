/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.runners;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.Client;
import org.opensearch.eval.SearchQualityEvaluationPlugin;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.Collection;

public class OpenSearchQuerySetRunner extends QuerySetRunner {

    final Client client;

    public OpenSearchQuerySetRunner(final Client client) {
        this.client = client;
    }

    @Override
    public QuerySetRunResult run(String querySetId) {

        // Get the query set.
        final SearchSourceBuilder getQuerySetSearchSourceBuilder = new SearchSourceBuilder();
        getQuerySetSearchSourceBuilder.query(QueryBuilders.matchQuery("_id", querySetId));

        final SearchRequest getQuerySetSearchRequest = new SearchRequest(SearchQualityEvaluationPlugin.QUERY_SETS_INDEX_NAME);
        getQuerySetSearchRequest.source(getQuerySetSearchSourceBuilder);

        try {

            final SearchResponse searchResponse = client.search(getQuerySetSearchRequest).get();

            // The queries from the query set that will be run.
            final Collection<String> queries = (Collection<String>) searchResponse.getHits().getAt(0).getSourceAsMap().get("queries");

            // TODO: Initiate the running of the query set.
            for(final String query : queries) {

                // TODO: What should this query be?
                final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.matchQuery("_id", querySetId));

            }

            final SearchMetrics searchMetrics = new SearchMetrics();
            final QuerySetRunResult querySetRunResult = new QuerySetRunResult(searchMetrics);
            return querySetRunResult;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
