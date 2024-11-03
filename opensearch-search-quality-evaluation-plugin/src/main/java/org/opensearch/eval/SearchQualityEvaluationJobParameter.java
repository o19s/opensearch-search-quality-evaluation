/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval;

import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.jobscheduler.spi.ScheduledJobParameter;
import org.opensearch.jobscheduler.spi.schedule.Schedule;

import java.io.IOException;
import java.time.Instant;

public class SearchQualityEvaluationJobParameter implements ScheduledJobParameter {

    public static final String NAME_FIELD = "name";
    public static final String ENABLED_FILED = "enabled";
    public static final String LAST_UPDATE_TIME_FIELD = "last_update_time";
    public static final String LAST_UPDATE_TIME_FIELD_READABLE = "last_update_time_field";
    public static final String SCHEDULE_FIELD = "schedule";
    public static final String ENABLED_TIME_FILED = "enabled_time";
    public static final String ENABLED_TIME_FILED_READABLE = "enabled_time_field";
    public static final String LOCK_DURATION_SECONDS = "lock_duration_seconds";
    public static final String JITTER = "jitter";

    // Properties from ScheduledJobParameter.
    private String jobName;
    private Instant lastUpdateTime;
    private Instant enabledTime;
    private boolean enabled;
    private Schedule schedule;
    private Long lockDurationSeconds;
    private Double jitter;

    // Custom properties for this job.
    private String indexToWatch;

    public SearchQualityEvaluationJobParameter() {

    }

    public SearchQualityEvaluationJobParameter(final String name, final String indexToWatch, final Schedule schedule,
                                               final Long lockDurationSeconds, final Double jitter) {
        this.jobName = name;
        this.schedule = schedule;
        this.enabled = true;
        this.lockDurationSeconds = lockDurationSeconds;
        this.jitter = jitter;

        final Instant now = Instant.now();
        this.enabledTime = now;
        this.lastUpdateTime = now;

        this.indexToWatch = indexToWatch;

    }

    @Override
    public String getName() {
        return this.jobName;
    }

    @Override
    public Instant getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    @Override
    public Instant getEnabledTime() {
        return this.enabledTime;
    }

    @Override
    public Schedule getSchedule() {
        return this.schedule;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public Long getLockDurationSeconds() {
        return this.lockDurationSeconds;
    }

    @Override
    public Double getJitter() {
        return jitter;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setLastUpdateTime(Instant lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void setEnabledTime(Instant enabledTime) {
        this.enabledTime = enabledTime;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setIndexToWatch(String indexToWatch) {
        this.indexToWatch = indexToWatch;
    }

    public void setLockDurationSeconds(Long lockDurationSeconds) {
        this.lockDurationSeconds = lockDurationSeconds;
    }

    public void setJitter(Double jitter) {
        this.jitter = jitter;
    }

    @Override
    public XContentBuilder toXContent(final XContentBuilder builder, final Params params) throws IOException {

        builder.startObject();

        builder
            .field(NAME_FIELD, this.jobName)
            .field(ENABLED_FILED, this.enabled)
            .field(SCHEDULE_FIELD, this.schedule);

        if (this.enabledTime != null) {
            builder.timeField(ENABLED_TIME_FILED, ENABLED_TIME_FILED_READABLE, this.enabledTime.toEpochMilli());
        }

        if (this.lastUpdateTime != null) {
            builder.timeField(LAST_UPDATE_TIME_FIELD, LAST_UPDATE_TIME_FIELD_READABLE, this.lastUpdateTime.toEpochMilli());
        }

        if (this.lockDurationSeconds != null) {
            builder.field(LOCK_DURATION_SECONDS, this.lockDurationSeconds);
        }

        if (this.jitter != null) {
            builder.field(JITTER, this.jitter);
        }

        builder.endObject();

        return builder;

    }

}
