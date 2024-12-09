#!/bin/bash -e

echo "Deleting existing judgments index..."
curl -s -X DELETE http://localhost:9200/judgments

echo "Creating judgments index..."
#curl -s -X PUT http://localhost:9200/judgments -H 'Content-Type: application/json' -d'
#                                              {
#                                                "mappings": {
#                                                  "properties": {
#                                                    "judgments_id": { "type": "keyword" },
#                                                    "query_id": { "type": "keyword" },
#                                                    "query": { "type": "keyword" },
#                                                    "document_id": { "type": "keyword" },
#                                                    "judgment": { "type": "double" }
#                                                  }
#                                                }
#                                              }'

echo "Creating judgments..."
curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/judgments?click_model=coec&max_rank=20"
