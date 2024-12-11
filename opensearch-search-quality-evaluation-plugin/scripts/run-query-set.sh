#!/bin/bash -e

QUERY_SET_ID="ca3c8091-ad48-4978-a16f-58e2cc5698b3"
JUDGMENTS_ID="97021d5d-d8c6-4147-a2f0-bbdacfe89b8a"
INDEX="ecommerce"
ID_FIELD="asin"
K="10"
THRESHOLD="1.0" # Default value

curl -s -X DELETE "http://localhost:9200/sqe_metrics_sample_data"

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
