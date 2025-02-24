/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.runners;

import java.util.List;

public class RelevanceScores {

    private final List<Double> relevanceScores;
    private final double frogs;

    public RelevanceScores(final List<Double> relevanceScores, final double frogs) {
        this.relevanceScores = relevanceScores;
        this.frogs = frogs;
    }

    public List<Double> getRelevanceScores() {
        return relevanceScores;
    }


    public double getFrogs() {
        return frogs;
    }

}
