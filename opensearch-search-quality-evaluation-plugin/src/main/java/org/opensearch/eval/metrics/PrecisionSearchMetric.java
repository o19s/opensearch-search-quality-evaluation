/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.metrics;

import java.util.List;

/**
 * Subclass of {@link SearchMetric} that calculates Precision @ k.
 */
public class PrecisionSearchMetric extends SearchMetric {

    private final double threshold;
    private final List<Double> relevanceScores;

    /**
     * Creates new precision metrics.
     * @param k The <code>k</code> value.
     * @param threshold The threshold for assigning binary relevancy scores to non-binary scores.
     *                  Scores greater than or equal to this value will be assigned a relevancy score of 1 (relevant).
     *                  Scores less than this value will be assigned a relevancy score of 0 (not relevant).
     * @param relevanceScores A list of relevance scores.
     */
    public PrecisionSearchMetric(final int k, final double threshold, final List<Double> relevanceScores) {
        super(k);
        this.threshold = threshold;
        this.relevanceScores = relevanceScores;
    }

    @Override
    public String getName() {
        return "precision_at_" + k;
    }

    @Override
    public double calculate() {

        // TODO: Implement precision calculation.
        return 0.0;

    }

    /**
     * Gets the threshold value.
     * @return The threshold value.
     */
    public double threshold() {
        return threshold;
    }

}
