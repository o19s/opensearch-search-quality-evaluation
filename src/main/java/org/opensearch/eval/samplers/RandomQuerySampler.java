/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.opensearch.eval.model.ubi.query.UbiQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A sampler that randomly selects a given number of queries.
 * It does not allow duplicate queries into the query set.
 */
public class RandomQuerySampler extends AbstractQuerySampler {

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

        if(parameters.getQuerySetSize() >= queries.size()) {

            // Take all queries since the requested size is equal to or greater than
            // the total number of queries.
            for(final UbiQuery ubiQuery : queries) {
                querySet.put(ubiQuery.getQuery(), 1L);
            }

        } else {

            // Create random integers up to the max query set size and then shuffle them.
            final List<Integer> randomNumbers = IntStream.range(0, parameters.getQuerySetSize()).boxed().collect(Collectors.toList());
            Collections.shuffle(randomNumbers);

            // For getting the frequency of each user query.
            final Map<String, Long> counts = ubiQueries.stream().collect(Collectors.groupingBy(UbiQuery::getUserQuery, Collectors.counting()));

            for(final int randomNumber : randomNumbers) {

                final UbiQuery randomQuery = ubiQueries.stream()
                        .skip(randomNumber)
                        .findFirst()
                        .orElse(null);

                querySet.put(randomQuery.getUserQuery(), counts.get(randomQuery.getUserQuery()));

            }

        }

        return querySet;

    }

}
