#!/bin/bash -e

curl -s -X DELETE http://localhost:9200/ubi_queries/
curl -s -X DELETE http://localhost:9200/ubi_events/
