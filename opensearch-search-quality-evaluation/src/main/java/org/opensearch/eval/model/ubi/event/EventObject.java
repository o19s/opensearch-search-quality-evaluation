package org.opensearch.eval.model.ubi.event;

import com.google.gson.annotations.SerializedName;

public class EventObject {

    @SerializedName("object_id_field")
    private String objectIdField;

    @SerializedName("object_id")
    private String objectId;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectIdField() {
        return objectIdField;
    }

    public void setObjectIdField(String objectIdField) {
        this.objectIdField = objectIdField;
    }

}
