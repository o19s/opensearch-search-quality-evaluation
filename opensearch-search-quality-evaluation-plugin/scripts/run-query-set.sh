#!/bin/bash -e

QUERY_SET_ID="${1}"
JUDGMENTS_ID="a42ebf21-718b-402e-9d3a-259c555cbaed"
INDEX="ecommerce"
ID_FIELD="asin"
K="10"

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
