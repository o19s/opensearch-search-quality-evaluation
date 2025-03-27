/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

public class ProbabilityProportionalToSizeSamplerParameters extends AbstractQuerySamplerParameters {

    public static final String NAME = "pptss";

    public ProbabilityProportionalToSizeSamplerParameters(final String name, final String description, final String sampling,
                                                          final int querySetSize, final String application) {
        super(NAME, name, description, sampling, querySetSize, application);
    }

    public ProbabilityProportionalToSizeSamplerParameters(final String name, final String description, final String sampling,
                                                          final int querySetSize) {
        super(NAME, name, description, sampling, querySetSize, "");
    }


}
