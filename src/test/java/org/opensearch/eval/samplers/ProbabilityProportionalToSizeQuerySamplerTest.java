/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensearch.eval.engine.SearchEngine;
import org.opensearch.eval.model.TimeFilter;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class ProbabilityProportionalToSizeQuerySamplerTest extends AbstractSamplerTest {

    @Disabled
    @Test
    public void simpleSample() throws Exception {

        final TimeFilter timeFilter = new TimeFilter();

        final Map<String, Long> mockQuerySet = new HashMap<>();
        mockQuerySet.put("user1", 3L);
        mockQuerySet.put("user2", 2L);

        final SearchEngine searchEngine = Mockito.mock(SearchEngine.class);
        when(searchEngine.getUbiQueries(2, "", timeFilter)).thenReturn(mockQuerySet);

        final ProbabilityProportionalToSizeSamplerParameters parameters = new ProbabilityProportionalToSizeSamplerParameters("name", "description", "sampling", 10);

        final ProbabilityProportionalToSizeQuerySampler sampler = new ProbabilityProportionalToSizeQuerySampler(searchEngine, parameters);
        final Map<String, Long> querySet = sampler.sample(timeFilter);

        Assertions.assertTrue(querySet.size() <= 10);

    }

}
