#!/bin/bash -e

curl -X GET http://localhost:9200/ubi_events/_search -H "Content-Type: application/json" -d'
{
  "size": 0,
  "aggs": {
    "By_Action": {
      "terms": {
        "field": "action_name"
      },
      "aggs": {
        "By_Position": {
          "terms": {
            "field": "event_attributes.position.index"
          }
        }
      }
    }
  }
}' | jq