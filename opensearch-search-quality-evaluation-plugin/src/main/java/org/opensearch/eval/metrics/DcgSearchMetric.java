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

public class DcgSearchMetric extends SearchMetric {

    protected final List<Double> relevanceScores;

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

        double dcg = 0.0;
        for(int i = 0; i < relevanceScores.size(); i++) {
            double relevance = relevanceScores.get(i);
            dcg += relevance / Math.log(i + 2); // Add 2 to avoid log(1) = 0
        }
        return dcg;

    }

}
