/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.opensearch.eval.Constants;
import org.opensearch.eval.engine.SearchEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link AbstractQuerySampler} that uses all UBI queries without any sampling.
 */
public class AllQueriesQuerySampler extends AbstractQuerySampler {

    public static final String NAME = "none";

    private final SearchEngine searchEngine;
    private final AllQueriesQuerySamplerParameters parameters;

    /**
     * Creates a new sampler.
     * @param searchEngine The OpenSearch {@link SearchEngine engine}.
     */
    public AllQueriesQuerySampler(final SearchEngine searchEngine, final AllQueriesQuerySamplerParameters parameters) {
        this.searchEngine = searchEngine;
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String sample() throws Exception {

        // Get queries from the UBI queries index.
        // TODO: This needs to use scroll or something else.
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(parameters.getQuerySetSize());

        final SearchRequest searchRequest = new SearchRequest(Constants.UBI_QUERIES_INDEX_NAME).source(searchSourceBuilder);

        // TODO: Don't use .get()
        final SearchResponse searchResponse = client.search(searchRequest).get();

        final Map<String, Long> queries = new HashMap<>();

        for(final SearchHit hit : searchResponse.getHits().getHits()) {

            final Map<String, Object> fields = hit.getSourceAsMap();
            queries.merge(fields.get("user_query").toString(), 1L, Long::sum);

            // Will be useful for paging once implemented.
            if(queries.size() > parameters.getQuerySetSize()) {
                break;
            }

        }

        return indexQuerySet(searchEngine, parameters.getName(), parameters.getDescription(), parameters.getSampling(), queries);

    }

}
