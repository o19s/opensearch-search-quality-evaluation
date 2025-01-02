/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.metrics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for search metrics.
 */
public abstract class SearchMetric {

    protected static final Logger LOGGER = LogManager.getLogger(SearchMetric.class);

    protected int k;

    /**
     * Gets the name of the metric, i.e. ndcg.
     * @return The name of the metric.
     */
    public abstract String getName();

    /**
     * Calculates the metric.
     * @return The value of the metric.
     */
    public abstract double calculate();

    private Double value = Double.NaN;

    /**
     * Creates the metric.
     * @param k The <code>k</code> value.
     */
    public SearchMetric(final int k) {
        this.k = k;
    }

    /**
     * Gets the <code>k</code> value.
     * @return The <code>k</code> value.
     */
    public int getK() {
        return k;
    }

    /**
     * Gets the value of the metric. If the metric has not yet been calculated,
     * the metric will first be calculated by calling <code>calculate</code>. This
     * function should be used in cases where repeated access to the metrics value is
     * needed without recalculating the metrics value.
     * @return The value of the metric.
     */
    public double getValue() {

        if(Double.isNaN(value)) {
            this.value = calculate();
        }

        return value;

    }

}
