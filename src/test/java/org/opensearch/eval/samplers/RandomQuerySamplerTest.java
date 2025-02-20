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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class RandomQuerySamplerTest extends AbstractSamplerTest {

    @Disabled
    @Test
    public void simpleSample() throws IOException {

        final Map<String, Long> mockQuerySet = new HashMap<>();
        mockQuerySet.put("user1", 3L);
        mockQuerySet.put("user2", 2L);

        final SearchEngine searchEngine = Mockito.mock(SearchEngine.class);
        when(searchEngine.getUbiQueries(2)).thenReturn(mockQuerySet);

        final RandomQuerySamplerParameters parameters = new RandomQuerySamplerParameters("name", "description", "sampling", 2);

        final RandomQuerySampler sampler = new RandomQuerySampler(searchEngine, parameters);
        final Map<String, Long> querySet = sampler.sample();

        showQueries(querySet);

        Assertions.assertEquals(2, querySet.size());

    }

}
