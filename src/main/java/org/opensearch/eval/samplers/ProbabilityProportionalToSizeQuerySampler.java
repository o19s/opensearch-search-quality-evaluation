/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.engine.SearchEngine;
import org.opensearch.eval.model.TimeFilter;
import org.opensearch.eval.model.ubi.query.UbiQuery;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link AbstractQuerySampler} that uses PPTSS sampling.
 * See https://opensourceconnections.com/blog/2022/10/13/how-to-succeed-with-explicit-relevance-evaluation-using-probability-proportional-to-size-sampling/
 * for more information on PPTSS.
 */
public class ProbabilityProportionalToSizeQuerySampler extends AbstractQuerySampler {

    public static final String NAME = "pptss";

    private static final Logger LOGGER = LogManager.getLogger(ProbabilityProportionalToSizeQuerySampler.class);

    private final SearchEngine searchEngine;
    private final ProbabilityProportionalToSizeSamplerParameters parameters;

    /**
     * Creates a new PPTSS sampler.
     * @param parameters The {@link ProbabilityProportionalToSizeSamplerParameters parameters} for the sampling.
     */
    public ProbabilityProportionalToSizeQuerySampler(final SearchEngine searchEngine, final ProbabilityProportionalToSizeSamplerParameters parameters) {
        this.searchEngine = searchEngine;
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, Long> sample(final TimeFilter timeFilter) throws IOException {

        final Collection<UbiQuery> ubiQueries = searchEngine.getUbiQueries(parameters.getApplication(), timeFilter);

        // Get all user queries except empty queries.
        final List<String> userQueries = ubiQueries.stream()
                .map(UbiQuery::getUserQuery)
                .filter(userQuery -> !userQuery.isEmpty())
                .toList();

        LOGGER.debug("User queries found: {}", userQueries.size());

        if(!userQueries.isEmpty()) {

            final Map<String, Long> weights = new HashMap<>();
            final Map<String, Double> normalizedWeights = new HashMap<>();
            final Map<String, Double> cumulativeWeights = new HashMap<>();
            final Map<String, Long> querySet = new HashMap<>();

            // Increment the weight for each user query.
            // This gives us a count of each user query.
            for (final String userQuery : userQueries) {
                weights.merge(userQuery, 1L, Long::sum);
            }

            // The total number of queries will be used to normalize the weights.
            final long countOfQueries = userQueries.size();

            for (final String userQuery : weights.keySet()) {
                // Calculate the normalized weights by dividing by the total number of queries.
                normalizedWeights.put(userQuery, weights.get(userQuery) / (double) countOfQueries);
            }

            // Ensure all normalized weights sum to 1.
            final double sumOfNormalizedWeights = normalizedWeights.values().stream().reduce(0.0, Double::sum);
            if (!compare(1.0, sumOfNormalizedWeights)) {
                throw new RuntimeException("Summed normalized weights do not equal 1.0: Actual value: " + sumOfNormalizedWeights);
            } else {
                LOGGER.debug("Summed normalized weights sum to {}", sumOfNormalizedWeights);
            }

            // Create weight "ranges" for each query.
            double lastWeight = 0;
            for(final String userQuery : normalizedWeights.keySet()) {
                lastWeight = normalizedWeights.get(userQuery) + lastWeight;
                cumulativeWeights.put(userQuery, lastWeight);
            }

            // The last weight should be 1.0.
            if(!compare(lastWeight, 1.0)) {
                throw new RuntimeException("The sum of the cumulative weights does not equal 1.0: Actual value: " + lastWeight);
            }

            final UniformRealDistribution uniform = new UniformRealDistribution(0, 1);

            for (int i = 1; i <= parameters.getQuerySetSize(); i++) {

                final double r = uniform.sample();

                for(final String userQuery : cumulativeWeights.keySet()) {

                    final double cumulativeWeight = cumulativeWeights.get(userQuery);
                    if(cumulativeWeight >= r) {
                        // This ignores duplicate queries.
                        querySet.put(userQuery, weights.get(userQuery));
                        break;
                    }

                }

            }

            return querySet;

        } else {
            return Collections.emptyMap();
        }

    }

    private boolean compare(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }

}