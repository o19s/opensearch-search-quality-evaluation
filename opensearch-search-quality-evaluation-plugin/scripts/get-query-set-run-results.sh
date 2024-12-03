#!/bin/bash -e

curl -s "http://localhost:9200/search_quality_eval_query_sets_run_results/_search" | jq
