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

public class EventObject {

    @JsonProperty("object_id_field")
    @SerializedName("object_id_field")
    private String objectIdField;

    @JsonProperty("object_id")
    @SerializedName("object_id")
    private String objectId;

    private final Map<String, Object> additionalProperties = new HashMap<>();

    @Override
    public String toString() {
        return "[" + objectIdField + ", " + objectId + "]";
    }

    /**
     * Gets the object ID.
     * @return The object ID.
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Sets the object ID.
     * @param objectId The object ID.
     */
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    /**
     * Gets the object ID field.
     * @return The object ID field.
     */
    public String getObjectIdField() {
        return objectIdField;
    }

    /**
     * Sets the object ID field.
     * @param objectIdField The object ID field.
     */
    public void setObjectIdField(String objectIdField) {
        this.objectIdField = objectIdField;
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
