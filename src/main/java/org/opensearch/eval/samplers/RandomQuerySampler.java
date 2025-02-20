/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.engine.SearchEngine;

import java.io.IOException;
import java.util.Map;

/**
 * A sampler that randomly selects a given number of queries.
 * It does not allow duplicate queries into the query set.
 */
public class RandomQuerySampler extends AbstractQuerySampler {

    private static final Logger LOGGER = LogManager.getLogger(RandomQuerySampler.class.getName());

    public static final String NAME = "random";

    final SearchEngine searchEngine;
    final RandomQuerySamplerParameters parameters;

    public RandomQuerySampler(final SearchEngine searchEngine,  RandomQuerySamplerParameters parameters) {
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
