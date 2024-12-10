#!/bin/bash -e

QUERY_SET_ID="1b25e19b-24d2-4efc-9c3e-59470c68f7b6"
JUDGMENTS_ID="0615e159-675b-4c60-875a-24daeb8c126c"
INDEX="ecommerce"
ID_FIELD="asin"
K="20"
THRESHOLD="1.0" # Default value

curl -s -X DELETE "http://localhost:9200/search_quality_eval_query_sets_run_results"

# Keyword search
curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/run?id=${QUERY_SET_ID}&judgments_id=${JUDGMENTS_ID}&index=${INDEX}&id_field=${ID_FIELD}&k=${K}" \
   -H "Content-Type: application/json" \
    --data-binary '{
                      "multi_match": {
                        "query": "#$query##",
                        "fields": ["id", "title", "category", "bullets", "description", "attrs.Brand", "attrs.Color"]
                      }
                  }'

## Neural search
#curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/run?id=${QUERY_SET_ID}&judgments_id=${JUDGMENTS_ID}&index=${INDEX}&id_field=${ID_FIELD}&k=${K}&search_pipeline=neural-search-pipeline" \
#   -H "Content-Type: application/json" \
#    --data-binary '{
#                      "neural": {
#                        "title_embedding": {
#                          "query_text": ""#$query##",
#                          "k": "50"
#                        }
#                      }
#                  }'

# Hybrid search
#curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/run?id=${QUERY_SET_ID}&judgments_id=${JUDGMENTS_ID}&index=${INDEX}&id_field=${ID_FIELD}&k=${K}&search_pipeline=hybrid-search-pipeline" \
#   -H "Content-Type: application/json" \
#    --data-binary '{
#                      "hybrid": {
#                        "queries": [
#                          {
#                            "match": {
#                              "title": {
#                                "query": "#$query##"
#                              }
#                            }
#                          },
#                          {
#                            "neural": {
#                              "title_embedding": {
#                                "query_text": "#$query##",
#                                "k": "50"
#                              }
#                            }
#                          }
#                        ]
#                      }
#                  }'
