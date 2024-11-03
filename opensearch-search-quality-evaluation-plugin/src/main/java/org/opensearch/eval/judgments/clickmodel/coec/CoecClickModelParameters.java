/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.clickmodel.coec;

import org.opensearch.eval.judgments.clickmodel.ClickModelParameters;

public class CoecClickModelParameters extends ClickModelParameters {

    private final boolean persist;
    private final int maxRank;
    private int roundingDigits = 3;

    public CoecClickModelParameters(boolean persist, final int maxRank) {
        this.persist = persist;
        this.maxRank = maxRank;
    }

    public CoecClickModelParameters(boolean persist, final int maxRank, final int roundingDigits) {
        this.persist = persist;
        this.maxRank = maxRank;
        this.roundingDigits = roundingDigits;
    }

    public boolean isPersist() {
        return persist;
    }

    public int getMaxRank() {
        return maxRank;
    }

    public int getRoundingDigits() {
        return roundingDigits;
    }

}
