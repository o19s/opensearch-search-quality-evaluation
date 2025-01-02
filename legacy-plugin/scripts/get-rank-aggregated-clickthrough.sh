#!/bin/bash -e

curl -s "http://localhost:9200/rank_aggregated_ctr/_search" | jq
