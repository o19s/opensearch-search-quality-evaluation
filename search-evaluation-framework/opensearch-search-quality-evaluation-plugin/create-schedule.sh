#!/bin/bash -e

curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/watch?id=1&index=ubi&job_name=test2&interval=1" | jq
