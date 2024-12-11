#!/bin/bash -e

echo "Deleting existing ubi_events and ubi_queries indexes..."
curl -s -X DELETE "http://localhost:9200/ubi_queries,ubi_events"

echo "Initializing UBI..."
curl -s -X POST "http://localhost:9200/_plugins/ubi/initialize"

echo "Indexing queries and events..."
curl -s -T "http://localhost:9200/_bulk?pretty" -H "Content-Type: application/x-ndjson" --data-binary @ubi_queries_events.ndjson
