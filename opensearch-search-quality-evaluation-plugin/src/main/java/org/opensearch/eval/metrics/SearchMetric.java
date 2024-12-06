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

public abstract class SearchMetric {

    private static final Logger LOGGER = LogManager.getLogger(SearchMetric.class);

    protected int k;

    public abstract String getName();

    public abstract double calculate();

    public SearchMetric(final int k) {
        this.k = k;
    }

    public int getK() {
        return k;
    }

}
