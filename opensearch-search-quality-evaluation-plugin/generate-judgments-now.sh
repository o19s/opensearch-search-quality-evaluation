#!/bin/bash -e

curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/judgments?click_model=coec&max_rank=20"