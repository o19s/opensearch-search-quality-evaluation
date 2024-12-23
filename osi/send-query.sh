#!/bin/bash -e

. stack.properties

OSIS_PIPELINE_ENDPOINT_URL=`terraform output -json "opensearch_ingest_pipeline_endpoint" | jq -r .[0]`

awscurl \
	--service osis \
	--region ${AWS_REGION} \
	--profile ${AWS_PROFILE} \
	-X POST \
	-H "Content-Type: application/json" \
	-d '[{"type": "query", "user_query": "computer", "query_id": "00112233-4455-6677-8899-aabbccddeeff"}]' \
	https://${OSIS_PIPELINE_ENDPOINT_URL}/ubi
