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
 * Subclass of {@link SearchMetric} that calculates Discounted Cumulative Gain @ k.
 */
public class DcgSearchMetric extends SearchMetric {

    protected final List<Double> relevanceScores;

    /**
     * Creates new DCG metrics.
     * @param k The <code>k</code> value.
     * @param relevanceScores A list of relevance scores.
     */
    public DcgSearchMetric(final int k, final List<Double> relevanceScores) {
        super(k);
        this.relevanceScores = relevanceScores;
    }

    @Override
    public String getName() {
        return "dcg_at_" + k;
    }

    @Override
    public double calculate() {
        return calculateDcg(relevanceScores);
    }

    protected double calculateDcg(final List<Double> relevanceScores) {

        double dcg = 0.0;
        for(int i = 1; i <= relevanceScores.size(); i++) {

            final double relevanceScore = relevanceScores.get(i - 1);
            final double numerator = Math.pow(2, relevanceScore) - 1.0;
            final double denominator = Math.log(i) / Math.log(i + 2);

            LOGGER.info("numerator = {}, denominator = {}", numerator, denominator);
            dcg += (numerator / denominator);

        }

        return dcg;

    }

}
