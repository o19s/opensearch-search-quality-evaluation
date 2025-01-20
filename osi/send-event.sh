#!/bin/bash -e

. stack.properties

OSIS_PIPELINE_ENDPOINT_URL=`terraform output -json "opensearch_ingest_pipeline_endpoint" | jq -r .[0]`

awscurl \
	--service osis \
	--region ${AWS_REGION} \
	--profile ${AWS_PROFILE} \
	-X POST \
	-H "Content-Type: application/json" \
	-d '[{"type": "event", "action_name": "click", "query_id": "99999999-4455-6677-8899-aabbccddeeff", "event_attributes": {"position": {"ordinal": 1}, "object": {"object_id": "1234", "object_id_field": "ean", "user_id": "abc"}}}]' \
	https://${OSIS_PIPELINE_ENDPOINT_URL}/ubi
