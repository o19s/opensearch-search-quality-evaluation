/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

public class ProbabilityProportionalToSizeParameters {

    private final int querySetSize;

    public ProbabilityProportionalToSizeParameters(int querySetSize) {
        this.querySetSize = querySetSize;
    }

    public int getQuerySetSize() {
        return querySetSize;
    }

}