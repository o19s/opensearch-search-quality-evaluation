#!/bin/bash -e

curl -s "http://localhost:9200/sqe_metrics_sample_data/_search" -H "Content-Type: application/json" -d'{
  "query": {
    "range": {
      "frogs": {
        "lt": 100 
      }
    }
  }
}' | jq
