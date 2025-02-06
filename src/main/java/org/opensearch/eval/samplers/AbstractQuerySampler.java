/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.engine.SearchEngine;
import org.opensearch.eval.model.data.QuerySet;
import org.opensearch.eval.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An interface for sampling UBI queries.
 */
public abstract class AbstractQuerySampler {

    private static final Logger LOGGER = LogManager.getLogger(AbstractQuerySampler.class);

    /**
     * Gets the name of the sampler.
     * @return The name of the sampler.
     */
    public abstract String getName();

    /**
     * Samples the queries and inserts the query set into an index.
     * @return A query set ID.
     */
    public abstract String sample() throws Exception;

    /**
     * Index the query set.
     */
    protected String indexQuerySet(final SearchEngine searchEngine, final String name, final String description, final String sampling, Map<String, Long> queries) throws Exception {

        LOGGER.info("Indexing {} queries for query set {}", queries.size(), name);

        final Collection<Map<String, Long>> querySetQueries = new ArrayList<>();

        // Convert the queries map to an object.
        for(final String query : queries.keySet()) {

            // Map of the query itself to the frequency of the query.
            final Map<String, Long> querySetQuery = new HashMap<>();
            querySetQuery.put(query, queries.get(query));

            querySetQueries.add(querySetQuery);

        }

        final QuerySet querySet = new QuerySet();
        querySet.setName(name);
        querySet.setDescription(description);
        querySet.setSampling(sampling);
        querySet.setQuerySetQueries(querySetQueries);
        querySet.setTimestamp(TimeUtils.getTimestamp());

        return searchEngine.indexQuerySet(querySet);

    }

}
