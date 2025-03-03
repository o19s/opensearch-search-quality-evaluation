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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A time filter for retrieving UBI queries and events.
 */
public class TimeFilter {

    private final String startTimestamp;
    private final String endTimestamp;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    public static TimeFilter fromQuerySamplerParameters(final AbstractQuerySamplerParameters parameters) {

        if(StringUtils.isNotEmpty(parameters.getStartTimestamp())) {

            validateTimetampFormat(parameters.getStartTimestamp());

            if(StringUtils.isNotEmpty(parameters.getEndTimestamp())) {
                validateTimetampFormat(parameters.getEndTimestamp());
                return new TimeFilter(parameters.getStartTimestamp(), parameters.getEndTimestamp());
            } else {
                return new TimeFilter(parameters.getStartTimestamp(), "");
            }

        } else {

            return new TimeFilter();

        }

    }

    public static boolean validateTimetampFormat(final String timestamp) {

        // The timestamp should align with OpenSearch's strict_date_time format which is yyyy-MM-ddTHH:mm:ss.SSSZ.
        // strict_date_time is what's specified in the UBI Queries index mapping in the opensearch-project/user-behavior-insights repository.

        try {
            sdf.setLenient(false);
            final Date date = sdf.parse(timestamp);
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Invalid timestamp format: " + timestamp, ex);
        }

        return true;

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
