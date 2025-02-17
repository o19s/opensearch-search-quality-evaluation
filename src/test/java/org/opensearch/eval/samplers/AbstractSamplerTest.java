/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import java.util.Map;

public abstract class AbstractSamplerTest {

    protected void showQueries(final Map<String, Long> querySet) {

        for(final String query : querySet.keySet()) {
            System.out.println("Query: " + query + ", Frequency: " + querySet.get(query));
        }

    }

}
