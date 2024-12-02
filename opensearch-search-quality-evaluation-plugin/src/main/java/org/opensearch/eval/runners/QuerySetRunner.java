/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.runners;

/**
 * Interface for query set runners. Classes that implement this interface
 * should be specific to a search engine. See the {@link OpenSearchQuerySetRunner} for an example.
 */
public interface QuerySetRunner {

    /**
     * Runs the query set.
     * @param querySetId The ID of the query set to run.
     * @param judgmentsId The ID of the judgments set to use for search metric calcuation.
     * @return The query set {@link QuerySetRunResult results} and calculated metrics.
     */
    QuerySetRunResult run(String querySetId, final String judgmentsId);

    /**
     * Saves the query set results to a persistent store, which may be the search engine itself.
     * @param result The {@link QuerySetRunResult results}.
     */
    void save(QuerySetRunResult result) throws Exception;

}
