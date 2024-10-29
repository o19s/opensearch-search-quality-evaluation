#!/bin/bash -e

curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/judgments?id=1&index=ubi&job_name=test2&interval=1" | jq

curl -s "http://localhost:9200/.scheduler_search_quality_eval/_search" | jq
