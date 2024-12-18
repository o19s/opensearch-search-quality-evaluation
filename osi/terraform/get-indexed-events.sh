#!/bin/bash -e

#curl -X GET https://search-osi-ubi-domain-pjju5yl7neorgz4jcsqhq5o7fq.us-east-1.es.amazonaws.com/_cat/indices | jq

#curl -X GET https://search-osi-ubi-domain-pjju5yl7neorgz4jcsqhq5o7fq.us-east-1.es.amazonaws.com/ubi_events/_search | jq

OPENSEARCH_ENDPOINT="search-osi-ubi-domain-pjju5yl7neorgz4jcsqhq5o7fq.us-east-1.es.amazonaws.com"

awscurl \
  "https://${OPENSEARCH_ENDPOINT}/_cat/indices" \
  -X GET \
  --region us-east-1 \
  --service es \
  --profile mtnfog

awscurl \
  "https://${OPENSEARCH_ENDPOINT}/ubi_events/_search" \
  -X GET \
  --region us-east-1 \
  --service es \
  --profile mtnfog | jq
