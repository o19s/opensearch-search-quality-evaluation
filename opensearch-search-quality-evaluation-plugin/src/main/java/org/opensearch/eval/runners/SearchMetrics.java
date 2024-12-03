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
import java.util.List;
import java.util.Map;

/**
 * Provides the ability to calculate search metrics and stores them.
 */
public class SearchMetrics {

    private final int k;

    private double dcg = 0.0;
    private double ndcg = 0.0;
    private double precision = 0.0;

    /**
     * Create the search metrics for an entire query set.
     * @param queryResults A list of {@link QueryResult}.
     * @param k The k used for metrics calculation, i.e. DCG@k.
     */
    public SearchMetrics(final List<QueryResult> queryResults, final int k) {
        this.k = k;

        // TODO: Calculate the metrics for the whole query set.
    }

    /**
     * Create the search metrics for a single query.

     * @param k The k used for metrics calculation, i.e. DCG@k.
     */
    public SearchMetrics(final String query, final List<String> orderedDocumentIds, final int k) {
        this.k = k;

        // TODO: Calculate the metrics for the single query.
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
