/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.opensearch.eval.judgments.util.MathUtils;

/**
 * A query result and its number of clicks and total events.
 */
public class ClickthroughRate {

    private final String objectId;
    private int clicks;
    private int events;

    public ClickthroughRate(final String objectId) {
        this.objectId = objectId;
        this.clicks = 0;
        this.events = 0;
    }

    public ClickthroughRate(final String objectId, final int clicks, final int events) {
        this.objectId = objectId;
        this.clicks = clicks;
        this.events = events;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 29 * result + objectId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "object_id: " + objectId + ", clicks: "  + clicks + ", events: " + events + ", ctr: " + MathUtils.round(getClickthroughRate());
    }

    public void logClick() {
        clicks++;
        events++;
    }

    public void logEvent() {
        events++;
    }

    public double getClickthroughRate() {
        return (double) clicks / events;
    }

    public int getClicks() {
        return clicks;
    }

    public int getEvents() {
        return events;
    }

    public String getObjectId() {
        return objectId;
    }

}
