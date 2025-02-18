/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model;

import org.apache.commons.lang3.StringUtils;
import org.opensearch.eval.samplers.AbstractQuerySamplerParameters;

/**
 * A time filter for retrieving UBI queries and events.
 */
public class TimeFilter {

    private final String startTimestamp;
    private final String endTimestamp;

    public static TimeFilter fromQuerySamplerParameters(final AbstractQuerySamplerParameters parameters) {

        if(StringUtils.isNotEmpty(parameters.getStartTimestamp())) {

            if(StringUtils.isNotEmpty(parameters.getEndTimestamp())) {
                return new TimeFilter(parameters.getStartTimestamp(), parameters.getEndTimestamp());
            } else {
                return new TimeFilter(parameters.getStartTimestamp(), "");
            }

        } else {

            return new TimeFilter();

        }

    }

    public TimeFilter() {
        this.startTimestamp = "";
        this.endTimestamp = "";
    }

    public TimeFilter(final String startTimestamp) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = "";
    }

    public TimeFilter(final String startTimestamp, final String endTimestamp) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public String getEndTimestamp() {
        return endTimestamp;
    }

}
