/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.node.NodeClient;
import org.opensearch.eval.SearchQualityEvaluationPlugin;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of {@link AbstractQuerySampler} that uses all UBI queries without any sampling.
 */
public class AllQueriesQuerySampler extends AbstractQuerySampler {

    private final NodeClient client;
    private final AllQueriesQuerySamplerParameters parameters;

    /**
     * Creates a new sampler.
     * @param client The OpenSearch {@link NodeClient client}.
     */
    public AllQueriesQuerySampler(final NodeClient client, final AllQueriesQuerySamplerParameters parameters) {
        this.client = client;
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return "none";
    }

    @Override
    public String sample() throws Exception {

        // Get queries from the UBI queries index.
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(parameters.getQuerySetSize());

        final SearchRequest searchRequest = new SearchRequest(SearchQualityEvaluationPlugin.UBI_QUERIES_INDEX_NAME);
        searchRequest.source(searchSourceBuilder);

        final SearchResponse searchResponse = client.search(searchRequest).get();

        // LOGGER.info("Found {} user queries from the ubi_queries index.", searchResponse.getHits().getTotalHits().toString());

        final Set<String> queries = new HashSet<>();
        for(final SearchHit hit : searchResponse.getHits().getHits()) {
            final Map<String, Object> fields = hit.getSourceAsMap();
            queries.add(fields.get("user_query").toString());
        }

        // LOGGER.info("Found {} user queries from the ubi_queries index.", queries.size());

        return indexQuerySet(client, parameters.getName(), parameters.getDescription(), parameters.getSampling(), queries);

    }

}