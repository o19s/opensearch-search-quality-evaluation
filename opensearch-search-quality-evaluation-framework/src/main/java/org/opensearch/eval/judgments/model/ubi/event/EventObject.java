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

public class EventObject {

    @SerializedName("object_id_field")
    private String objectIdField;

    @SerializedName("object_id")
    private String objectId;

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

}