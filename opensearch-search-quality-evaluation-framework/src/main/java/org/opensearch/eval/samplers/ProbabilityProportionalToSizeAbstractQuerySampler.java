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
import org.opensearch.eval.model.ubi.query.UbiQuery;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of {@link AbstractQuerySampler} that uses PPTSS sampling.
 * See https://opensourceconnections.com/blog/2022/10/13/how-to-succeed-with-explicit-relevance-evaluation-using-probability-proportional-to-size-sampling/
 * for more information on PPTSS.
 */
public class ProbabilityProportionalToSizeAbstractQuerySampler extends AbstractQuerySampler {

    public static final String NAME = "pptss";

    private static final Logger LOGGER = LogManager.getLogger(ProbabilityProportionalToSizeAbstractQuerySampler.class);

    private final SearchEngine searchEngine;
    private final ProbabilityProportionalToSizeParameters parameters;

    /**
     * Creates a new PPTSS sampler.
     * @param searchEngine The OpenSearch {@link SearchEngine engine}.
     * @param parameters The {@link ProbabilityProportionalToSizeParameters parameters} for the sampling.
     */
    public ProbabilityProportionalToSizeAbstractQuerySampler(final SearchEngine searchEngine, final ProbabilityProportionalToSizeParameters parameters) {
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

        final List<String> userQueries = ubiQueries.stream()
                .map(UbiQuery::getUserQuery)
                .toList();

        // LOGGER.info("User queries found: {}", userQueries);

        final Map<String, Long> weights = new HashMap<>();

        // Increment the weight for each user query.
        for(final String userQuery : userQueries) {
            weights.merge(userQuery, 1L, Long::sum);
        }

        // The total number of queries will be used to normalize the weights.
        final long countOfQueries = userQueries.size();

        // Calculate the normalized weights by dividing by the total number of queries.
        final Map<String, Double> normalizedWeights = new HashMap<>();
        for(final String userQuery : weights.keySet()) {
            normalizedWeights.put(userQuery, weights.get(userQuery) / (double) countOfQueries);
            //LOGGER.info("{}: {}/{} = {}", userQuery, weights.get(userQuery), countOfQueries, normalizedWeights.get(userQuery));
        }

        // Ensure all normalized weights sum to 1.
        final double sumOfNormalizedWeights = normalizedWeights.values().stream().reduce(0.0, Double::sum);
        if(!compare(1.0, sumOfNormalizedWeights)) {
            throw new RuntimeException("Summed normalized weights do not equal 1.0: Actual value: " + sumOfNormalizedWeights);
        } else {
            LOGGER.info("Summed normalized weights sum to {}", sumOfNormalizedWeights);
        }

        final Map<String, Long> querySet = new HashMap<>();
        final Set<Double> randomNumbers = new HashSet<>();

        // Generate random numbers between 0 and 1 for the size of the query set.
        // Do this until our query set has reached the requested maximum size.
        // This may require generating more random numbers than what was requested
        // because removing duplicate user queries will require randomly picking more queries.
        int count = 1;

        // TODO: How to short-circuit this such that if the same query gets picked over and over, the loop will never end.
        final int max = 5000;
        while(querySet.size() < parameters.getQuerySetSize() && count < max) {

            // Make a random number not yet used.
            double random;
            do {
                random = Math.random();
            } while (randomNumbers.contains(random));
            randomNumbers.add(random);

            // Find the weight closest to the random weight in the map of deltas.
            double smallestDelta = Integer.MAX_VALUE;
            String closestQuery = null;
            for(final String query : normalizedWeights.keySet()) {
                final double delta = Math.abs(normalizedWeights.get(query) - random);
                if(delta < smallestDelta) {
                    smallestDelta = delta;
                    closestQuery = query;
                }
            }

            querySet.put(closestQuery, weights.get(closestQuery));
            count++;

            //LOGGER.info("Generated random value: {}; Smallest delta = {}; Closest query = {}", random, smallestDelta, closestQuery);

        }

        return indexQuerySet(searchEngine, parameters.getName(), parameters.getDescription(), parameters.getSampling(), querySet);

    }

    public static boolean compare(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }

}
