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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * A sampler that selects the top N queries.
 */
public class TopNQuerySampler extends AbstractQuerySampler {

    public static final String NAME = "topn";

    final TopNQuerySamplerParameters parameters;

    public TopNQuerySampler(final TopNQuerySamplerParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, Long> sample(final Collection<UbiQuery> ubiQueries) {

        final Map<String, Long> querySet = new HashMap<>();

        // Remove duplicates from the queries.
        final Set<UbiQuery> queriesWithoutDuplicates = new HashSet<>(ubiQueries);

        if(parameters.getQuerySetSize() >= queriesWithoutDuplicates.size()) {

            // Take all queries since the requested size is equal to or greater than
            // the total number of queries.
            for(final UbiQuery ubiQuery : queriesWithoutDuplicates) {
                // TODO: Get the frequency for the user query.
                querySet.put(ubiQuery.getQuery(), 1L);
            }

        } else {

            // For getting the frequency of each user query.
            final Map<UbiQuery, Long> counts = ubiQueries.stream()
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

            // Sort the queries by frequency, highest to lowest.
            final TreeMap<UbiQuery, Long> sortedMap = new TreeMap<>(Comparator.comparing(counts::get, Comparator.reverseOrder()));
            sortedMap.putAll(counts);

            for(final Map.Entry<UbiQuery, Long> entry : sortedMap.entrySet()) {
                System.out.println(entry.getKey().getQuery() + " " + entry.getValue());
            }

            final Map<UbiQuery, Long> topNQueries = sortedMap.entrySet().stream()
                    .limit(parameters.getQuerySetSize())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            for(final UbiQuery ubiQuery : topNQueries.keySet()) {
                querySet.put(ubiQuery.getQuery(), counts.get(ubiQuery.getUserQuery()));
            }

        }

        return querySet;

    }

}
