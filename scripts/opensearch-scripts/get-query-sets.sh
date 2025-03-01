#!/bin/bash -e

curl -s "http://localhost:9200/srw_query_sets/_search" | jq
