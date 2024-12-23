#!/bin/bash -e

. stack.properties

OPENSEARCH_ENDPOINT=`terraform output "opensearch_domain_endpoint" | jq -r .`

awscurl \
  "https://${OPENSEARCH_ENDPOINT}/ubi_events,ubi_queries" \
  -X DELETE \
  --region ${AWS_REGION} \
  --service es \
  --profile ${AWS_PROFILE}
