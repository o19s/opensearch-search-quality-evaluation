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

        // For getting the frequency of each user query.
        final Map<String, Long> counts = ubiQueries.stream().collect(Collectors.groupingBy(UbiQuery::getUserQuery, Collectors.counting()));

        if(parameters.getQuerySetSize() >= queriesWithoutDuplicates.size()) {

            // Take all queries since the requested size is equal to or greater than the total number of queries.
            for(final UbiQuery ubiQuery : queriesWithoutDuplicates) {
                querySet.put(ubiQuery.getUserQuery(), counts.get(ubiQuery.getUserQuery()));
            }

        } else {

            // Sort the queries by frequency, highest to lowest.
            final TreeMap<String, Long> sortedMap = new TreeMap<>(Comparator.comparing(counts::get, Comparator.reverseOrder()));
            sortedMap.putAll(counts);

            final Map<String, Long> topNQueries = sortedMap.entrySet().stream()
                    .limit(parameters.getQuerySetSize())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            for(final String ubiQuery : topNQueries.keySet()) {
                querySet.put(ubiQuery, counts.get(ubiQuery));
            }

        }

        return querySet;

    }

}
