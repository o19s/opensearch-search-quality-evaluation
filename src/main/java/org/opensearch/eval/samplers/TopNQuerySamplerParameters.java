/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

public class TopNQuerySamplerParameters extends AbstractQuerySamplerParameters {

    public TopNQuerySamplerParameters(String name, String description, String sampling, int querySetSize, final String application) {
        super("topn", name, description, sampling, querySetSize, application);
    }

    public TopNQuerySamplerParameters(String name, String description, String sampling, int querySetSize) {
        super("topn", name, description, sampling, querySetSize, "");
    }

}
