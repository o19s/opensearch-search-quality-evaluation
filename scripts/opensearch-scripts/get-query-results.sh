#!/bin/bash -e

curl -s "http://localhost:9200/srw_query_results/_search" -H "Content-Type: application/json" | jq
