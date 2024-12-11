#!/bin/bash -e

QUERY_SET_ID="8a2b8455-a442-49c3-9d34-2985c3ba08d3"
JUDGMENTS_ID="3cf6270b-9ebe-4c5a-bd4b-5ba362bd5a5e"
INDEX="ecommerce"
ID_FIELD="asin"
K="20"
THRESHOLD="1.0" # Default value

curl -s -X DELETE "http://localhost:9200/sqe_metrics_sample_data"

# Keyword search
#curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/run?id=${QUERY_SET_ID}&judgments_id=${JUDGMENTS_ID}&index=${INDEX}&id_field=${ID_FIELD}&k=${K}" \
#   -H "Content-Type: application/json" \
#    --data-binary '{
#                      "multi_match": {
#                        "query": "#$query##",
#                        "fields": ["id", "title", "category", "bullets", "description", "attrs.Brand", "attrs.Color"]
#                      }
#                  }'

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
curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/run?id=${QUERY_SET_ID}&judgments_id=${JUDGMENTS_ID}&index=${INDEX}&id_field=${ID_FIELD}&k=${K}&search_pipeline=hybrid-search-pipeline" \
   -H "Content-Type: application/json" \
    --data-binary '{
                      "hybrid": {
                        "queries": [
                          {
                            "match": {
                              "title": {
                                "query": "#$query##"
                              }
                            }
                          },
                          {
                            "neural": {
                              "title_embedding": {
                                "query_text": "#$query##",
                                "k": "50"
                              }
                            }
                          }
                        ]
                      }
                  }'
