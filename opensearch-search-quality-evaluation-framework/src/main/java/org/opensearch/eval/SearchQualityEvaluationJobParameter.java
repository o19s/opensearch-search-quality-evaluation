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

    /**
     * The name of the parameter for providing a name for the scheduled job.
     */
    public static final String NAME_FIELD = "name";

    /**
     * The name of the parameter for creating a job as enabled or disabled.
     */
    public static final String ENABLED_FILED = "enabled";

    /**
     * The name of the parameter for specifying when the job was last updated.
     */
    public static final String LAST_UPDATE_TIME_FIELD = "last_update_time";

    /**
     * The name of the parameter for specifying a readable time for when the job was last updated.
     */
    public static final String LAST_UPDATE_TIME_FIELD_READABLE = "last_update_time_field";
    public static final String SCHEDULE_FIELD = "schedule";
    public static final String ENABLED_TIME_FILED = "enabled_time";
    public static final String ENABLED_TIME_FILED_READABLE = "enabled_time_field";
    public static final String LOCK_DURATION_SECONDS = "lock_duration_seconds";
    public static final String JITTER = "jitter";

    /**
     * The name of the parameter that allows for specifying the type of click model to use.
     */
    public static final String CLICK_MODEL = "click_model";

    /**
     * The name of the parameter that allows for setting a max rank value to use during judgment generation.
     */
    public static final String MAX_RANK = "max_rank";

    // Properties from ScheduledJobParameter.
    private String jobName;
    private Instant lastUpdateTime;
    private Instant enabledTime;
    private boolean enabled;
    private Schedule schedule;
    private Long lockDurationSeconds;
    private Double jitter;

    // Custom properties.
    private String clickModel;
    private int maxRank;

    public SearchQualityEvaluationJobParameter() {

    }

    public SearchQualityEvaluationJobParameter(final String name, final Schedule schedule,
                                               final Long lockDurationSeconds, final Double jitter,
                                               final String clickModel, final int maxRank) {
        this.jobName = name;
        this.schedule = schedule;
        this.enabled = true;
        this.lockDurationSeconds = lockDurationSeconds;
        this.jitter = jitter;

        final Instant now = Instant.now();
        this.enabledTime = now;
        this.lastUpdateTime = now;

        // Custom properties.
        this.clickModel = clickModel;
        this.maxRank = maxRank;

    }

    @Override
    public XContentBuilder toXContent(final XContentBuilder builder, final Params params) throws IOException {

        builder.startObject();

        builder
                .field(NAME_FIELD, this.jobName)
                .field(ENABLED_FILED, this.enabled)
                .field(SCHEDULE_FIELD, this.schedule)
                .field(CLICK_MODEL, this.clickModel)
                .field(MAX_RANK, this.maxRank);

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

    /**
     * Sets the name of the job.
     * @param jobName The name of the job.
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Sets when the job was last updated.
     * @param lastUpdateTime An {@link Instant} of when the job was last updated.
     */
    public void setLastUpdateTime(Instant lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    /**
     * Sets when the job was enabled.
     * @param enabledTime An {@link Instant} of when the job was enabled.
     */
    public void setEnabledTime(Instant enabledTime) {
        this.enabledTime = enabledTime;
    }

    /**
     * Sets whether the job is enabled.
     * @param enabled A boolean representing whether the job is enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Sets the schedule for the job.
     * @param schedule A {@link Schedule} for the job.
     */
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    /**
     * Sets the lock duration for the cluster when running the job.
     * @param lockDurationSeconds The lock duration in seconds.
     */
    public void setLockDurationSeconds(Long lockDurationSeconds) {
        this.lockDurationSeconds = lockDurationSeconds;
    }

    /**
     * Sets the jitter for the job.
     * @param jitter The jitter for the job.
     */
    public void setJitter(Double jitter) {
        this.jitter = jitter;
    }

    /**
     * Gets the type of click model to use for implicit judgment generation.
     * @return The type of click model to use for implicit judgment generation.
     */
    public String getClickModel() {
        return clickModel;
    }

    /**
     * Sets the click model type to use for implicit judgment generation.
     * @param clickModel The click model type to use for implicit judgment generation.
     */
    public void setClickModel(String clickModel) {
        this.clickModel = clickModel;
    }

    /**
     * Gets the max rank to use when generating implicit judgments.
     * @return The max rank to use when generating implicit judgments.
     */
    public int getMaxRank() {
        return maxRank;
    }

    /**
     * Sets the max rank to use when generating implicit judgments.
     * @param maxRank The max rank to use when generating implicit judgments.
     */
    public void setMaxRank(int maxRank) {
        this.maxRank = maxRank;
    }

}
