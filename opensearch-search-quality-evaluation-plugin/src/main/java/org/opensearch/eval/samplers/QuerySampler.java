/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.opensearch.eval.judgments.model.ubi.query.UbiQuery;

import java.util.Collection;

/**
 * An interface for sampling UBI queries.
 */
public interface QuerySampler {

    /**
     * Gets the name of the sampler.
     * @return The name of the sampler.
     */
    String getName();

    /**
     * Samples the queries.
     * @param userQueries A collection of user queries from UBI queries.
     * @return A collection of sampled user queries.
     */
    Collection<String> sample(Collection<String> userQueries);

}
