/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model.data;

public abstract class AbstractData {

    private final String id;

    public AbstractData(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
