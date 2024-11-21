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

/**
 * The parameters for the {@link CoecClickModel}.
 */
public class CoecClickModelParameters extends ClickModelParameters {

    private final boolean persist;
    private final int maxRank;
    private int roundingDigits = 3;

    /**
     * Creates new parameters.
     * @param persist Whether to persist the calculated judgments.
     * @param maxRank The max rank to use when calculating the judgments.
     */
    public CoecClickModelParameters(boolean persist, final int maxRank) {
        this.persist = persist;
        this.maxRank = maxRank;
    }

    /**
     * Creates new parameters.
     * @param persist Whether to persist the calculated judgments.
     * @param maxRank The max rank to use when calculating the judgments.
     * @param roundingDigits The number of decimal places to round calculated values to.
     */
    public CoecClickModelParameters(boolean persist, final int maxRank, final int roundingDigits) {
        this.persist = persist;
        this.maxRank = maxRank;
        this.roundingDigits = roundingDigits;
    }

    /**
     * Gets whether to persist the calculated judgments.
     * @return Whether to persist the calculated judgments.
     */
    public boolean isPersist() {
        return persist;
    }

    /**
     * Gets the max rank for the implicit judgments calculation.
     * @return The max rank for the implicit judgments calculation.
     */
    public int getMaxRank() {
        return maxRank;
    }

    /**
     * Gets the number of rounding digits to use for judgments.
     * @return The number of rounding digits to use for judgments.
     */
    public int getRoundingDigits() {
        return roundingDigits;
    }

}
