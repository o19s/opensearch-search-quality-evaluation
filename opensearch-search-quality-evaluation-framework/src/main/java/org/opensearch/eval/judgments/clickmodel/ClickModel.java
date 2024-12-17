/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.clickmodel;

/**
 * Base class for creating click models.
 */
public abstract class ClickModel {

    /**
     * Calculate implicit judgments.
     * @return The judgments ID.
     * @throws Exception Thrown if the judgments cannot be created.
     */
    public abstract String calculateJudgments() throws Exception;

}
