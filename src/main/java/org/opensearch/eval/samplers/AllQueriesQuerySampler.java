/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.opensearch.eval.engine.SearchEngine;
import org.opensearch.eval.model.ubi.query.UbiQuery;

import java.util.Collection;
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

        final Collection<UbiQuery> ubiQueries = searchEngine.getUbiQueries();

        final Map<String, Long> queries = new HashMap<>();

        for(final UbiQuery ubiQuery : ubiQueries) {

            queries.merge(ubiQuery.getUserQuery(), 1L, Long::sum);

            // Will be useful for paging once implemented.
            if(queries.size() > parameters.getQuerySetSize()) {
                break;
            }

        }

        return indexQuerySet(searchEngine, parameters.getName(), parameters.getDescription(), parameters.getSampling(), queries);

    }

}
