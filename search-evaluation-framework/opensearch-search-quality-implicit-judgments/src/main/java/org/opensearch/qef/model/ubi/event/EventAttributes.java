package org.opensearch.qef.model.ubi.event;

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
