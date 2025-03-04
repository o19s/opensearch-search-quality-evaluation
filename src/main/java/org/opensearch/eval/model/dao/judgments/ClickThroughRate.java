/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model.dao.judgments;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensearch.eval.model.dao.AbstractData;

import java.util.UUID;

public class ClickThroughRate extends AbstractData {

    @JsonProperty("user_query")
    private String userQuery;

    @JsonProperty("clicks")
    private long clicks;

    @JsonProperty("events")
    private long events;

    @JsonProperty("ctr")
    private double ctr;

    @JsonProperty("object_id")
    private String objectId;

    public ClickThroughRate() {
        super(UUID.randomUUID().toString());
    }

    public ClickThroughRate(String id) {
        super(id);
    }

    @Override
    public String toString() {
        return "ClickThroughRate{" +
                "id='" + getId() + '\'' +
                ", userQuery='" + userQuery + '\'' +
                ", clicks=" + clicks +
                ", events=" + events +
                ", ctr=" + ctr +
                ", objectId='" + objectId + '\'' +
                '}';
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
