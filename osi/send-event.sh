#!/bin/bash -e

# pip install awscurl

# Get from the Terraform output or the AWS Console
OSIS_PIPELINE_ENDPOINT_URL="ubi-pipeline-xjmot6taz7mmcv76a36mlscgg4.us-east-1.osis.amazonaws.com"

awscurl \
	--service osis \
	--region us-east-1 \
	--profile TODO_PUT_YOUR_AWS_PROFILE_NAME_HERE \
	-X POST \
	-H "Content-Type: application/json" \
	-d '[{"type": "event", "action_name": "click", "query_id": "99999999-4455-6677-8899-aabbccddeeff", "event_attributes": {"position": {"ordinal": 1}, "object": {"object_id": "1234", "object_id_field": "ean", "user_id": "abc"}}}]' \
	https://${OSIS_PIPELINE_ENDPOINT_URL}/ubi
