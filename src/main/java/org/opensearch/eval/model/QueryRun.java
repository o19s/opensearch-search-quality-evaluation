/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model;

import java.util.List;

public class QueryRun {

    private final List<String> documentIds;
    private final int numberOfResults;

    public QueryRun(final List<String> documentIds, final int numberOfResults) {
        this.documentIds = documentIds;
        this.numberOfResults = numberOfResults;
    }

    public List<String> getDocumentIds() {
        return documentIds;
    }

    public int getNumberOfResults() {
        return numberOfResults;
    }

}
