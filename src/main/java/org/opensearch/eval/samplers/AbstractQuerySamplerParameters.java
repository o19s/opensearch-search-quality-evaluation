/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

public abstract class AbstractQuerySamplerParameters {

    private final String sampler;
    private final String name;
    private final String description;
    private final String sampling;
    private final int querySetSize;
    private final String application;
    private final String startTimestamp;
    private final String endTimestamp;

    public AbstractQuerySamplerParameters(final String sampler, final String name, final String description, final String sampling,
                                          final int querySetSize, final String application, final String startTimestamp, final String endTimestamp) {
        this.sampler = sampler;
        this.name = name;
        this.description = description;
        this.sampling = sampling;
        this.querySetSize = querySetSize;
        this.application = application;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public AbstractQuerySamplerParameters(final String sampler, final String name, final String description, final String sampling,
                                          final int querySetSize, final String application) {
        this.sampler = sampler;
        this.name = name;
        this.description = description;
        this.sampling = sampling;
        this.querySetSize = querySetSize;
        this.application = application;
        this.startTimestamp = "";
        this.endTimestamp = "";
    }

    public AbstractQuerySamplerParameters(final String sampler, final String name, final String description, final String sampling,
                                          final int querySetSize) {
        this.sampler = sampler;
        this.name = name;
        this.description = description;
        this.sampling = sampling;
        this.querySetSize = querySetSize;
        this.application = "";
        this.startTimestamp = "";
        this.endTimestamp = "";
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSampling() {
        return sampling;
    }

    public int getQuerySetSize() {
        return querySetSize;
    }

    public String getSampler() {
        return sampler;
    }

    public String getApplication() {
        return application;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public String getEndTimestamp() {
        return endTimestamp;
    }

}
