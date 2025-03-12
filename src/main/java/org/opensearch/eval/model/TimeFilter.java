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
    private final boolean active;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    public static TimeFilter fromQuerySamplerParameters(final AbstractQuerySamplerParameters parameters) {

        // If the start timestamp is not empty, validate it and return a TimeFilter with the start timestamp.
        if(StringUtils.isNotEmpty(parameters.getStartTimestamp())) {

            validateTimetampFormat(parameters.getStartTimestamp());

            // If the end timestamp is not empty, validate it and return a TimeFilter with both start and end timestamps.
            if(StringUtils.isNotEmpty(parameters.getEndTimestamp())) {

                validateTimetampFormat(parameters.getEndTimestamp());
                return new TimeFilter(parameters.getStartTimestamp(), parameters.getEndTimestamp());

            } else {

                // No end timestamp - just start timestamp.
                return new TimeFilter(parameters.getStartTimestamp(), "");

            }

        } else {

            // If the end timestamp is not empty, validate it and return a TimeFilter with both start and end timestamps.
            if(StringUtils.isNotEmpty(parameters.getEndTimestamp())) {

                // No start timestamp - just end timestamp.
                validateTimetampFormat(parameters.getEndTimestamp());
                return new TimeFilter("", parameters.getEndTimestamp());

            } else {

                // No start or end timestamp.
                return new TimeFilter();

            }

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

    @Override
    public String toString() {
        return "TimeFilter{startTimestamp='" + startTimestamp + "', endTimestamp='" + endTimestamp + "'}";
    }

    public TimeFilter() {
        this.startTimestamp = "";
        this.endTimestamp = "";
        this.active = false;
    }

    public TimeFilter(final String startTimestamp) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = "";
        this.active = true;
    }

    public TimeFilter(final String startTimestamp, final String endTimestamp) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.active = true;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public String getEndTimestamp() {
        return endTimestamp;
    }

    public boolean isActive() {
        return active;
    }

}
