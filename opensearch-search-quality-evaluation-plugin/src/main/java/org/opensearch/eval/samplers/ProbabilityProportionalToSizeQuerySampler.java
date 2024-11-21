/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.opensearch.eval.judgments.model.ubi.query.UbiQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An implementation of {@link QuerySampler} that uses PPTSS sampling.
 * See https://opensourceconnections.com/blog/2022/10/13/how-to-succeed-with-explicit-relevance-evaluation-using-probability-proportional-to-size-sampling/
 * for more information on PPTSS.
 */
public class ProbabilityProportionalToSizeQuerySampler implements QuerySampler {

    private final ProbabilityProportionalToSizeParameters parameters;

    /**
     * Creates a new PPTSS sampler.
     * @param parameters The {@link ProbabilityProportionalToSizeParameters parameters} for the sampling.
     */
    public ProbabilityProportionalToSizeQuerySampler(final ProbabilityProportionalToSizeParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return "pptss";
    }

    @Override
    public Collection<String> sample(final Collection<String> userQueries) {

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
        }

        // Ensure all normalized weights sum to 1.
        final double sumOfNormalizedWeights = normalizedWeights.values().stream().reduce(0.0, Double::sum);
        if(sumOfNormalizedWeights != 1.0) {
            throw new RuntimeException("Summed normalized weights do not equal 1.0");
        }

        final Collection<String> querySet = new ArrayList<>();
        final Set<Double> randomNumbers = new HashSet<>();

        // Generate a random number between 0 and 1 for the size of the query set.
        for(int count = 0; count < parameters.getQuerySetSize(); count++) {

            // Make a random number not yet used.
            double random;
            do {
                random = Math.random();
            } while (randomNumbers.contains(random));
            randomNumbers.add(random);

            // Find the weight closest to the random weight.
            double finalRandom = random;
            double nearestWeight = normalizedWeights.values().stream()
                    .min(Comparator.comparingDouble(i -> Math.abs(i - finalRandom)))
                    .orElseThrow(() -> new NoSuchElementException("No value present"));

            // Find the query having the weight closest to this random number.
            for(Map.Entry<String, Double> entry : normalizedWeights.entrySet()) {
                if(entry.getValue() == nearestWeight) {
                    querySet.add(entry.getKey());
                    break;
                }
            }

        }

        return querySet;

    }

}
