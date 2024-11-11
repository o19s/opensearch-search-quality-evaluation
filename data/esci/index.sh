#!/bin/bash -e

curl -X POST "http://localhost:9200/_bulk?pretty" -H "Content-Type: application/x-ndjson" --data-binary @ubi_queries_events_1000.ndjson
