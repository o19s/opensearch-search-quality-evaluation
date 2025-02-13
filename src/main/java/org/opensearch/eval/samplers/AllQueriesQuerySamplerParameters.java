/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

public class AllQueriesQuerySamplerParameters extends AbstractSamplerParameters {

    public static final String NAME = "all";

    public AllQueriesQuerySamplerParameters(final String name, final String description, final String sampling,
                                            final int querySetSize) {
        super(NAME, name, description, sampling, querySetSize);
    }

}
