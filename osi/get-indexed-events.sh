#!/bin/bash -e

# pip install awscurl

# Get from the Terraform output or the AWS Console
OPENSEARCH_ENDPOINT="search-osi-ubi-domain-pjju5yl7neorgz4jcsqhq5o7fq.us-east-1.es.amazonaws.com"

awscurl \
  "https://${OPENSEARCH_ENDPOINT}/_cat/indices" \
  -X GET \
  --region us-east-1 \
  --service es \
  --profile TODO_PUT_YOUR_AWS_PROFILE_NAME_HERE

awscurl \
  "https://${OPENSEARCH_ENDPOINT}/ubi_events/_search" \
  -X GET \
  --region us-east-1 \
  --service es \
  --profile TODO_PUT_YOUR_AWS_PROFILE_NAME_HERE | jq
