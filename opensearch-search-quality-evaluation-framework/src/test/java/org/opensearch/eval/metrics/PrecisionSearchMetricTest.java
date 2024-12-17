/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.metrics;

import org.opensearch.test.OpenSearchTestCase;

import java.util.List;

public class PrecisionSearchMetricTest extends OpenSearchTestCase {

    public void testCalculate() {

        final int k = 10;
        final double threshold = 1.0;
        final List<Double> relevanceScores = List.of(1.0, 2.0, 3.0, 1.0, 2.0, 3.0, 1.0, 2.0, 3.0, 0.0);

        final PrecisionSearchMetric precisionSearchMetric = new PrecisionSearchMetric(k, threshold, relevanceScores);
        final double precision = precisionSearchMetric.calculate();

        assertEquals(0.9, precision, 0.0);

    }

}
