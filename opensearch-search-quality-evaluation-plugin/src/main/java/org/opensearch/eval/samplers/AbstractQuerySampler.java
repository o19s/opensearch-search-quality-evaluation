/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.node.NodeClient;
import org.opensearch.eval.SearchQualityEvaluationPlugin;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * An interface for sampling UBI queries.
 */
public abstract class AbstractQuerySampler {

    /**
     * Gets the name of the sampler.
     * @return The name of the sampler.
     */
    abstract String getName();

    /**
     * Samples the queries and inserts the query set into an index.
     * @return A query set ID.
     */
    abstract String sample() throws Exception;

    /**
     * Index the query set.
     */
    protected String indexQuerySet(final NodeClient client, final String name, final String description, final String sampling, Collection<String> queries) throws Exception {

        final Map<String, Object> querySet = new HashMap<>();
        querySet.put("name", name);
        querySet.put("description", description);
        querySet.put("sampling", sampling);
        querySet.put("queries", queries);
        querySet.put("created_at", Instant.now().toEpochMilli());

        final String querySetId = UUID.randomUUID().toString();

        final IndexRequest indexRequest = new IndexRequest().index(SearchQualityEvaluationPlugin.QUERY_SETS_INDEX_NAME)
                .id(querySetId)
                .source(querySet)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        client.index(indexRequest).get();

        return querySetId;

    }

}