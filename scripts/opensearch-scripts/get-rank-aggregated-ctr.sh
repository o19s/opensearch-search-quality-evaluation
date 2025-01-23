#!/bin/bash -e

curl -s "http://localhost:9200/rank_aggregated_ctr/_search" -H "Content-type: application/json" -d'{
  "query": {
    "range": {
      "ctr": {
        "gt": 0 
      }
    }
  }
}'