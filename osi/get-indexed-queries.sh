#!/bin/bash -e

OPENSEARCH_ENDPOINT="search-osi-ubi-domain-pjju5yl7neorgz4jcsqhq5o7fq.us-east-1.es.amazonaws.com"

awscurl \
  "https://${OPENSEARCH_ENDPOINT}/_cat/indices" \
  -X GET \
  --region us-east-1 \
  --service es \
  --profile mtnfog

awscurl \
  "https://${OPENSEARCH_ENDPOINT}/ubi_queries/_search" \
  -X GET \
  --region us-east-1 \
  --service es \
  --profile mtnfog | jq
