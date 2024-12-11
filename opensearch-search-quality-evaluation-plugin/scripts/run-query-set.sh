#!/bin/bash -e

QUERY_SET_ID="dcbf3db4-56ea-47cd-87ea-3d13d067ae7a"
JUDGMENTS_ID="78f0e4e4-1cbf-47b4-9737-5feef65dad4d"
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
