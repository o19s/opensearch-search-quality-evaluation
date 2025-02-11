/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model.ubi.event;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Attributes on an UBI event.
 */
public class EventAttributes {

    @SerializedName("object")
    private EventObject object;

    @JsonProperty("session_id")
    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("position")
    private Position position;

    private final Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * Creates a new object.
     */
    public EventAttributes() {

    }

    /**
     * Gets the {@link EventObject} for the event.
     * @return A {@link EventObject}.
     */
    public EventObject getObject() {
        return object;
    }

    /**
     * Sets the {@link EventObject} for the event.
     * @param object A {@link EventObject}.
     */
    public void setObject(EventObject object) {
        this.object = object;
    }

    /**
     * Gets the session ID for the event.
     * @return The session ID for the event.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session ID for the event.
     * @param sessionId The session ID for the evnet.
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets the {@link Position} associated with the event.
     * @return The {@link Position} associated with the event.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the {@link Position} associated with the event.
     * @param position The {@link Position} associated with the event.
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Adds an unrecognized property to the additional properties map.
     * @param key   The property name.
     * @param value The property value.
     */
    @JsonAnySetter
    public void addAdditionalProperty(String key, Object value) {
        this.additionalProperties.put(key, value);
    }

    /**
     * Gets the additional properties.
     * @return The additional properties map.
     */
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

}
