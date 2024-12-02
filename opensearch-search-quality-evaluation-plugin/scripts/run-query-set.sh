#!/bin/bash -e

QUERY_SET_ID="${1}"

curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/run?id=${QUERY_SET_ID}" | jq
