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
 * Subclass of {@link SearchMetric} that calculates Normalized Discounted Cumulative Gain @ k.
 */
public class NdcgSearchMetric extends DcgSearchMetric {

    /**
     * Creates new NDCG metrics.
     * @param k The <code>k</code> value.
     * @param relevanceScores A list of relevancy scores.
     */
    public NdcgSearchMetric(final int k, final List<Double> relevanceScores) {
        super(k, relevanceScores);
    }

    @Override
    public String getName() {
        return "ndcg_at_" + k;
    }

    @Override
    public double calculate() {

        // Make the ideal relevance scores by sorting the relevance scores largest to smallest.
        relevanceScores.sort(Double::compare);

        double dcg = super.calculate();
        double idcg = super.calculateDcg(relevanceScores);

        return dcg / idcg;

    }

}
