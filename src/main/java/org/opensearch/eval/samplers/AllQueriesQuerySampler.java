/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.apache.commons.lang3.StringUtils;
import org.opensearch.eval.model.ubi.query.UbiQuery;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link AbstractQuerySampler} that uses all UBI queries without any sampling.
 */
public class AllQueriesQuerySampler extends AbstractQuerySampler {

    public static final String NAME = "all";

    private final AllQueriesQuerySamplerParameters parameters;

    /**
     * Creates a new sampler.
     */
    public AllQueriesQuerySampler(final AllQueriesQuerySamplerParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, Long> sample(final Collection<UbiQuery> ubiQueries) throws Exception {

        final Map<String, Long> queries = new HashMap<>();

        for(final UbiQuery ubiQuery : ubiQueries) {

            // Ignore if the user query is empty.
            if(StringUtils.isNotEmpty(ubiQuery.getUserQuery())) {

                queries.merge(ubiQuery.getUserQuery(), 1L, Long::sum);

                // Will be useful for paging once implemented.
                if(queries.size() >= parameters.getQuerySetSize()) {
                    break;
                }

            }

        }

        return queries;
    }

}
