#!/bin/bash -e

. stack.properties

OPENSEARCH_ENDPOINT=`terraform output "opensearch_domain_endpoint" | jq -r .`

awscurl \
  "https://${OPENSEARCH_ENDPOINT}/ubi_events/_mappings" \
  --region ${AWS_REGION} \
  --service es \
  --profile ${AWS_PROFILE} | jq

awscurl \
  "https://${OPENSEARCH_ENDPOINT}/ubi_queries/_mappings" \
  --region ${AWS_REGION} \
  --service es \
  --profile ${AWS_PROFILE} | jq
