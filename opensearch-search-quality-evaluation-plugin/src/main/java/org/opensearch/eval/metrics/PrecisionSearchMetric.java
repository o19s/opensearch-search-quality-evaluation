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

public class PrecisionSearchMetric extends SearchMetric {

    private final List<Double> relevanceScores;

    public PrecisionSearchMetric(final int k, final List<Double> relevanceScores) {
        super(k);
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

}
