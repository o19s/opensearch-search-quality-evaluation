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

import java.io.IOException;
import java.util.Map;

/**
 * A sampler that randomly selects a given number of queries.
 */
public class RandomQuerySampler extends AbstractQuerySampler {

    public static final String NAME = "random";

    final SearchEngine searchEngine;
    final RandomQuerySamplerParameters parameters;

    public RandomQuerySampler(final SearchEngine searchEngine, final RandomQuerySamplerParameters parameters) {
        this.searchEngine = searchEngine;
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, Long> sample() throws IOException {
        return searchEngine.getRandomUbiQueries(parameters.getQuerySetSize());
    }

}
