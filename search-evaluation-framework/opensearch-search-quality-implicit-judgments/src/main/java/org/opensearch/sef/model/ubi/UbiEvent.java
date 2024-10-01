package org.opensearch.sef.model.ubi;

import org.opensearch.search.SearchHit;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A UBI event.
 * An example event is under src/test/resources/ubi_event.json
 */
public class UbiEvent {

    private final String actionName;
    private final String clientId;
    private final String queryId;
    private final String objectId;
    private final String sessionId;
    private final int position;

    public UbiEvent(final SearchHit hit) throws IOException {

        this.actionName = hit.getSourceAsMap().get("action_name").toString();
        this.clientId = hit.getSourceAsMap().get("client_id").toString();

        if(hit.getSourceAsMap().containsKey("query_id") && hit.getSourceAsMap().get("query_id") != null) {
            this.queryId = hit.getSourceAsMap().get("query_id").toString();
        } else {
            this.queryId = null;
        }

        @SuppressWarnings("unchecked")
        final Map<String, Object> eventAttributes = (Map<String, Object>) hit.getSourceAsMap().get("event_attributes");

        if(eventAttributes.containsKey("session_id")) {
            this.sessionId = eventAttributes.get("session_id").toString();
        } else {
            this.sessionId = null;
        }

        if(eventAttributes.containsKey("object")) {
            @SuppressWarnings("unchecked")
            // TODO: Why is object a map of String to List<String>?
            final Map<String, List<String>> object = (Map<String, List<String>>) eventAttributes.get("object");
            if(object.containsKey("object_id")) {
                this.objectId = String.valueOf(object.get("object_id"));
            } else {
                this.objectId = null;
            }
        } else {
            this.objectId = null;
        }

        if(eventAttributes.containsKey("position")) {
            // position is an object, and I am using it solely as an integer.
            @SuppressWarnings("unchecked") final Map<String, Integer> position = (Map<String, Integer>) eventAttributes.get("position");
            if (position != null) {
                this.position = position.get("index");
            } else {
                this.position = -1;
            }
        } else {
            this.position = -1;
        }

    }

    @Override
    public String toString() {
        return actionName + ", " + clientId + ", " + queryId + ", " + objectId + ", " + position;
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

    public String getObjectId() {
        return objectId;
    }

    public int getPosition() {
        return position;
    }

    public String getSessionId() {
        return sessionId;
    }

}
