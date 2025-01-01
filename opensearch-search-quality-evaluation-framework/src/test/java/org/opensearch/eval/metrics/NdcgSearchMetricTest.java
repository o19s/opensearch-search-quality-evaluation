/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.metrics;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NdcgSearchMetricTest {

    @Test
    public void testCalculate() {

        final int k = 10;
        final List<Double> relevanceScores = List.of(1.0, 2.0, 3.0, 1.0, 2.0, 3.0, 1.0, 2.0, 3.0, 0.0);

        final NdcgSearchMetric ndcgSearchMetric = new NdcgSearchMetric(k, relevanceScores);
        final double ndcg = ndcgSearchMetric.calculate();

        assertEquals(0.7151195094457645, ndcg, 0.0);

    }

    @Test
    public void testCalculateAllZeros() {

        final int k = 10;
        final List<Double> relevanceScores = List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        final NdcgSearchMetric ndcgSearchMetric = new NdcgSearchMetric(k, relevanceScores);
        final double ndcg = ndcgSearchMetric.calculate();

        assertEquals(0.0, ndcg, 0.0);

    }

}
