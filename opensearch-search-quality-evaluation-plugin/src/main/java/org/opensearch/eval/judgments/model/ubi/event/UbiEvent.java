/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.model.ubi.event;

import com.google.gson.annotations.SerializedName;

/**
 * A UBI event.
 */
public class UbiEvent {

    @SerializedName("action_name")
    private String actionName;

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("query_id")
    private String queryId;

    @SerializedName("event_attributes")
    private EventAttributes eventAttributes;

    @Override
    public String toString() {
        return actionName + ", " + clientId + ", " + queryId + ", " + eventAttributes.getObject() + ", " + eventAttributes.getPosition().getIndex();
    }

    public String getActionName() {
        return actionName;
    }

    public String getClientId() {
        return clientId;
    }

    public String getQueryId() {
        return queryId;
    }

    public EventAttributes getEventAttributes() {
        return eventAttributes;
    }

    public void setEventAttributes(EventAttributes eventAttributes) {
        this.eventAttributes = eventAttributes;
    }

}
