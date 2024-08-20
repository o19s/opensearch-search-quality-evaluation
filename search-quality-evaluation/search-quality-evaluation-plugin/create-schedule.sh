#!/bin/bash -e

curl -v -X POST "http://localhost:9200/_plugins/search_quality_evaluation/watch?id=1&index=ubi&job_name=test2&interval=1"
