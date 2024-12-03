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

    private final int k;

    private double dcg = 0.0;
    private double ndcg = 0.0;
    private double precision = 0.0;

    public SearchMetrics(final int k) {
        this.k = k;
    }

    /**
     * Gets the metrics as a map for ease of indexing.
     * @return A map of the search metrics.
     */
    public Map<String, Double> getSearchMetricsAsMap() {

        final Map<String, Double> metrics = new HashMap<>();
        metrics.put("dcg_at_" + k, dcg);
        metrics.put("ndcg_at_" + k, ndcg);
        metrics.put("prec_at_" + k, precision);

        return metrics;

    }

    public int getK() {
        return k;
    }

    public double getDcg() {
        return dcg;
    }

    public void setDcg(double dcg) {
        this.dcg = dcg;
    }

    public double getNdcg() {
        return ndcg;
    }

    public void setNdcg(double ndcg) {
        this.ndcg = ndcg;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

}
