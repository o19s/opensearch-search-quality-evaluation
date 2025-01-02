/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model.ubi.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

/**
 * Creates a representation of a UBI event.
 */
public class UbiEvent {

    @JsonProperty("action_name")
    @SerializedName("action_name")
    private String actionName;

    @JsonProperty("client_id")
    @SerializedName("client_id")
    private String clientId;

    @JsonProperty("query_id")
    @SerializedName("query_id")
    private String queryId;

    @JsonProperty("session_id")
    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("application")
    private String application;

    @JsonProperty("event_attributes")
    @SerializedName("event_attributes")
    private EventAttributes eventAttributes;

    @JsonProperty("user_query")
    @SerializedName("user_query")
    private String userQuery;

    @JsonProperty("message_type")
    @SerializedName("message_type")
    private String messageType;

    @JsonProperty("message")
    @SerializedName("message")
    private String message;

    private String timestamp;

    /**
     * Creates a new representation of an UBI event.
     */
    public UbiEvent() {

    }

    @Override
    public String toString() {
        return actionName + ", " + clientId + ", " + queryId + ", " + eventAttributes.getObject().toString() + ", " + eventAttributes.getPosition().getOrdinal();
    }

    /**
     * Gets the name of the action.
     * @return The name of the action.
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Gets the client ID.
     * @return The client ID.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the query ID.
     * @return The query ID.
     */
    public String getQueryId() {
        return queryId;
    }

    /**
     * Gets the event attributes.
     * @return The {@link EventAttributes}.
     */
    public EventAttributes getEventAttributes() {
        return eventAttributes;
    }

    /**
     * Sets the event attributes.
     * @param eventAttributes The {@link EventAttributes}.
     */
    public void setEventAttributes(EventAttributes eventAttributes) {
        this.eventAttributes = eventAttributes;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(String userQuery) {
        this.userQuery = userQuery;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
