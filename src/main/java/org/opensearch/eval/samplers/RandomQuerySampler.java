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
import org.opensearch.eval.engine.OpenSearchEngine;
import org.opensearch.eval.model.ubi.query.UbiQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A sampler that randomly selects a given number of queries.
 * It does not allow duplicate queries into the query set.
 */
public class RandomQuerySampler extends AbstractQuerySampler {

    private static final Logger LOGGER = LogManager.getLogger(OpenSearchEngine.class.getName());

    public static final String NAME = "random";

    final RandomQuerySamplerParameters parameters;

    public RandomQuerySampler(final RandomQuerySamplerParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, Long> sample(final Collection<UbiQuery> ubiQueries) {

        // Remove duplicates from the queries.
        final Set<UbiQuery> queries = new HashSet<>(ubiQueries);

        final Map<String, Long> querySet = new HashMap<>();

        // For getting the frequency of each user query.
        final Map<String, Long> counts = ubiQueries.stream().collect(Collectors.groupingBy(UbiQuery::getUserQuery, Collectors.counting()));

        if(parameters.getQuerySetSize() >= queries.size()) {

            querySet.putAll(counts);

        } else {

            // Create random integers up to the max query set size and then shuffle them.
            final Set<Integer> randomNumbers = generateRandomNumbers(parameters.getQuerySetSize(), queries.size());
            for(final int randomNumber : randomNumbers) {

                final UbiQuery randomQuery = queries.stream()
                        .skip(randomNumber)
                        .findFirst()
                        .orElse(null);

                querySet.put(randomQuery.getUserQuery(), counts.get(randomQuery.getUserQuery()));

            }

        }

        return querySet;

    }

    private Set<Integer> generateRandomNumbers(final int n, final int max) {

        final Random random = new Random();
        final Set<Integer> randomIndexes = new HashSet<>();

        while (randomIndexes.size() < n) {
            final int randomNumber = random.nextInt(max);
            randomIndexes.add(randomNumber);
        }

        return randomIndexes;

    }

}
