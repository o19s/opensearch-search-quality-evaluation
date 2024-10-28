#!/bin/bash -e

echo "Creating ubi_events index"

curl -X DELETE http://localhost:9200/ubi_events
curl -X PUT http://localhost:9200/ubi_events -H "Content-Type: application/json"
curl -X PUT http://localhost:9200/ubi_events/_mapping -H "Content-Type: application/json" -d @./events-mapping.json
