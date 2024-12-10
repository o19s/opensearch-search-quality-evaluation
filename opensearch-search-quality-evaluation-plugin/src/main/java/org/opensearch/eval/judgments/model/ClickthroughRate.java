/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.model;

import org.opensearch.eval.utils.MathUtils;

/**
 * A query result and its number of clicks and total events.
 */
public class ClickthroughRate {

    private final String objectId;
    private int clicks;
    private int events;

    /**
     * Creates a new clickthrough rate for an object.
     * @param objectId The ID of the object.
     */
    public ClickthroughRate(final String objectId) {
        this.objectId = objectId;
        this.clicks = 0;
        this.events = 0;
    }

    /**
     * Creates a new clickthrough rate for an object given counts of clicks and events.
     * @param objectId The object ID.
     * @param clicks The count of clicks.
     * @param events The count of events.
     */
    public ClickthroughRate(final String objectId, final int clicks, final int events) {
        this.objectId = objectId;
        this.clicks = clicks;
        this.events = events;
    }

    @Override
    public String toString() {
        return "object_id: " + objectId + ", clicks: "  + clicks + ", events: " + events + ", ctr: " + MathUtils.round(getClickthroughRate());
    }

    /**
     * Log a click to this object.
     * This increments clicks and events.
     */
    public void logClick() {
        clicks++;
        events++;
    }

    /**
     * Log an event to this object.
     */
    public void logEvent() {
        events++;
    }

    /**
     * Calculate the clickthrough rate.
     * @return The clickthrough rate as clicks divided by events.
     */
    public double getClickthroughRate() {
        return (double) clicks / events;
    }

    /**
     * Gets the count of clicks.
     * @return The count of clicks.
     */
    public int getClicks() {
        return clicks;
    }

    /**
     * Gets the count of events.
     * @return The count of events.
     */
    public int getEvents() {
        return events;
    }

    /**
     * Gets the object ID.
     * @return The object ID.
     */
    public String getObjectId() {
        return objectId;
    }

}
