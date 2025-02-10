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
 * A position represents the location of a search result in an event.
 */
public class Position {

    @SerializedName("ordinal")
    private int ordinal;

    private final Map<String, Object> additionalProperties = new HashMap<>();

    @Override
    public String toString() {
        return String.valueOf(ordinal);
    }

    /**
     * Gets the ordinal of the position.
     * @return The ordinal of the position.
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * Sets the ordinal of the position.
     * @param ordinal The ordinal of the position.
     */
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
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
