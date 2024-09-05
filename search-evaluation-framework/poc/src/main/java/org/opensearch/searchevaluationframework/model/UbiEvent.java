package org.opensearch.searchevaluationframework.model;

import org.opensearch.search.SearchHit;

import java.util.List;
import java.util.Map;

public class UbiEvent {

//       {
//        "_index": "ubi_events",
//        "_id": "6e7020c4-a7c5-482d-8478-4a0d32c07564",
//        "_score": 1,
//        "_source": {
//          "action_name": "product_hover",
//          "client_id": "USER-eeed-43de-959d-90e6040e84f9",
//          "query_id": null,
//          "page_id": "/",
//          "message_type": "INFO",
//          "message": "Xerox 006R90321 toner cartridge Original Black 6 pc(s) (undefined)",
//          "timestamp": 1725541171702,
//          "event_attributes": {
//            "object": {
//              "object_id_field": "product",
//              "object_id": "3920564",
//              "description": "Xerox 006R90321 toner cartridge Original Black 6 pc(s)",
//              "object_detail": null
//            },
//            "position": null,
//            "browser": null,
//            "session_id": null,
//            "page_id": null,
//            "dwell_time": null
//          }
//        }
//      }

    private String actionName;
    private String clientId;
    private String queryId;
    private String objectId;
    private String sessionId;
    private int position;

    public UbiEvent(SearchHit hit) {
        this.actionName = hit.getSourceAsMap().get("action_name").toString();
        this.clientId = hit.getSourceAsMap().get("client_id").toString();

        if(hit.getSourceAsMap().containsKey("query_id")) {
            if(hit.getSourceAsMap().get("query") != null) {
                this.queryId = hit.getSourceAsMap().get("query_id").toString();
            }
        }

        @SuppressWarnings("unchecked")
        final Map<String, Object> eventAttributes = (Map<String, Object>) hit.getSourceAsMap().get("event_attributes");
        this.sessionId = eventAttributes.get("session_id").toString();

        // TODO: Why is object_id a list?
        @SuppressWarnings("unchecked")
        final Map<String, List<String>> object = (Map<String, List<String>>) eventAttributes.get("object");
        this.objectId = object.get("object_id").get(0);

        // TODO: position is an object and I am using it solely as an integer.
        @SuppressWarnings("unchecked")
        final Map<String, Integer> position = (Map<String, Integer>) eventAttributes.get("position");
        this.position = position.get("index");

    }

    private UbiEvent() {

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
