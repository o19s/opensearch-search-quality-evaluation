#!/bin/bash -e

curl -s "http://localhost:9200/srw_metrics/_search" -H "Content-Type: application/json" | jq


#curl -s "http://localhost:9200/srw_metrics/_search" -H "Content-Type: application/json" -d'{
#  "query": {
#    "range": {
#      "frogs_percent": {
#        "lt": 100
#      }
#    }
#  }
#}' | jq
