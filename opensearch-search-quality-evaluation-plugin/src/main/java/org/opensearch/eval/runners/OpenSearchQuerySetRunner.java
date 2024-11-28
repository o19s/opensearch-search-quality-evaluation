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
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

            // The results of each query.
            final Collection<QueryResult> queryResults = new ArrayList<>();

            // TODO: Initiate the running of the query set.
            for(final String query : queries) {

                // TODO: What should this query be?
                final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.matchQuery("title", query));
                // TODO: Just fetch the id ("asin") field and not all the unnecessary fields.

                // TODO: Allow for setting this index name.
                final SearchRequest searchRequest = new SearchRequest("ecommerce");
                getQuerySetSearchRequest.source(getQuerySetSearchSourceBuilder);

                final SearchResponse sr = client.search(searchRequest).get();

                final List<String> orderedDocumentIds = new ArrayList<>();

                for(final SearchHit hit : sr.getHits().getHits()) {

                    // TODO: This field needs to be customizable.
                    orderedDocumentIds.add(hit.getFields().get("asin").toString());

                }

                queryResults.add(new QueryResult(orderedDocumentIds));

            }

            // TODO: Calculate the search metrics given the results and the judgments.
            final SearchMetrics searchMetrics = new SearchMetrics();

            return new QuerySetRunResult(queryResults, searchMetrics);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
