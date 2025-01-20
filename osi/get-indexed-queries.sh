#!/bin/bash -e

. stack.properties

OPENSEARCH_ENDPOINT=`terraform output "opensearch_domain_endpoint" | jq -r .`

awscurl \
  "https://${OPENSEARCH_ENDPOINT}/_cat/indices" \
  -X GET \
  --region ${AWS_REGION} \
  --service es \
  --profile ${AWS_PROFILE}

awscurl \
  "https://${OPENSEARCH_ENDPOINT}/ubi_queries/_search" \
  -X GET \
  --region ${AWS_REGION} \
  --service es \
  --profile ${AWS_PROFILE} | jq
