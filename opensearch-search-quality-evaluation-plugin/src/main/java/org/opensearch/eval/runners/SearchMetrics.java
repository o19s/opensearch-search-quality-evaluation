/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.runners;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the ability to calculate search metrics and stores them.
 */
public class SearchMetrics {

    private double dcg_at_10 = 0.0;
    private double ndcg_at_10 = 0.0;
    private double prec_at_10 = 0.0;

    /**
     * Gets the metrics as a map for ease of indexing.
     * @return A map of the search metrics.
     */
    public Map<String, Double> getSearchMetricsAsMap() {

        final Map<String, Double> metrics = new HashMap<>();
        metrics.put("dcg_at_10", dcg_at_10);
        metrics.put("ndcg_at_10", ndcg_at_10);
        metrics.put("prec_at_10", prec_at_10);

        return metrics;

    }

    public double getDcg_at_10() {
        return dcg_at_10;
    }

    public void setDcg_at_10(double dcg_at_10) {
        this.dcg_at_10 = dcg_at_10;
    }

    public double getNdcg_at_10() {
        return ndcg_at_10;
    }

    public void setNdcg_at_10(double ndcg_at_10) {
        this.ndcg_at_10 = ndcg_at_10;
    }

    public double getPrec_at_10() {
        return prec_at_10;
    }

    public void setPrec_at_10(double prec_at_10) {
        this.prec_at_10 = prec_at_10;
    }

}
