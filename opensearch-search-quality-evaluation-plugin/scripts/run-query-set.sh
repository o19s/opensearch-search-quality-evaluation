#!/bin/bash -e

QUERY_SET_ID="${1}"
JUDGMENTS_ID="9183599e-46dd-49e0-9584-df816164a4c2"
INDEX="ecommerce"
ID_FIELD="asin"
K="20"

curl -s -X DELETE "http://localhost:9200/search_quality_eval_query_sets_run_results"

curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/run?id=${QUERY_SET_ID}&judgments_id=${JUDGMENTS_ID}&index=${INDEX}&id_field=${ID_FIELD}&k=${K}" \
   -H "Content-Type: application/json" \
    --data-binary '{
                      "match": {
                        "description": {
                          "query": "#$query##"
                        }
                      }
                  }'
