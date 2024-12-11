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

        // k should equal the size of relevanceScores.

        double dcg = 0.0;

        for (int i = 0; i < relevanceScores.size(); i++) {

            double d = log2(i + 2);
            double n = Math.pow(2, relevanceScores.get(i)) - 1;

            if(d != 0) {
                dcg += (n / d);
            }

        }
        return dcg;

    }

    private double log2(int N) {
        return Math.log(N) / Math.log(2);
    }

}
