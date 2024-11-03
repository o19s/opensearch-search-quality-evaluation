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

public class EventAttributes {

    @SerializedName("object")
    private EventObject object;

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("position")
    private Position position;

    public EventObject getObject() {
        return object;
    }

    public void setObject(EventObject object) {
        this.object = object;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

}
