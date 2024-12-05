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

public class NdcgSearchMetric extends DcgSearchMetric {

    private final List<Double> idealRelevanceScores;

    public NdcgSearchMetric(final int k, final List<Double> relevanceScores, final List<Double> idealRelevanceScores) {
        super(k, relevanceScores);
        this.idealRelevanceScores = idealRelevanceScores;
    }

    @Override
    public String getName() {
        return "ndcg_at_" + k;
    }

    @Override
    public double calculate() {

        double dcg = super.calculate();

        double idcg = 0.0;
        for(int i = 0; i < idealRelevanceScores.size(); i++) {
            double relevance = idealRelevanceScores.get(i);
            idcg += relevance / Math.log(i + 2); // Add 2 to avoid log(1) = 0
        }

        if(idcg == 0) {
            return 0;
        }

        return dcg / idcg;

    }

}
