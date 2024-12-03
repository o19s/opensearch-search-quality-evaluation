#!/bin/bash -e

QUERY_SET_ID="${1}"
JUDGMENTS_ID="12345"
INDEX="ecommerce"
ID_FIELD="asin"

curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/run?id=${QUERY_SET_ID}&judgments_id=${JUDGMENTS_ID}&index=${INDEX}&id_field=${ID_FIELD}" \
   -H "Content-Type: application/json" \
    --data-binary '{
                  "query": {
                    "match": {
                      "description": {
                        "query": "#$query##"
                      }
                    }
                  }
                }'
