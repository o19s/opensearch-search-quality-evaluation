#!/bin/bash -e

STACK_NAME=${1:-"ubi"}

aws cloudformation update-stack \
    --stack-name ${STACK_NAME} \
    --template-body file://template.yaml \
    --capabilities CAPABILITY_IAM \
    --region us-east-1 \
    --profile mtnfog
