/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.clickmodel;

import org.opensearch.eval.judgments.model.Judgment;

import java.io.IOException;
import java.util.Collection;

public abstract class ClickModel<T extends ClickModelParameters> {

    public static final String INDEX_UBI_EVENTS = "ubi_events";
    public static final String INDEX_UBI_QUERIES = "ubi_queries";

    public abstract Collection<Judgment> calculateJudgments() throws Exception;

}