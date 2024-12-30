/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model.data;

import java.util.UUID;

public class ClickThroughRate extends AbstractData {

    private String userQuery;
    private long clicks;
    private long events;
    private double ctr;
    private String objectId;

    public ClickThroughRate() {
        super(UUID.randomUUID().toString());
    }

    public ClickThroughRate(String id) {
        super(id);
    }

    public String getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(String userQuery) {
        this.userQuery = userQuery;
    }

    public long getClicks() {
        return clicks;
    }

    public void setClicks(long clicks) {
        this.clicks = clicks;
    }

    public long getEvents() {
        return events;
    }

    public void setEvents(long events) {
        this.events = events;
    }

    public double getCtr() {
        return ctr;
    }

    public void setCtr(double ctr) {
        this.ctr = ctr;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

}
