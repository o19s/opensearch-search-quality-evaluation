#!/bin/bash -e

STACK_NAME=${1:-"ubi"}

aws cloudformation delete-stack \
    --stack-name ${STACK_NAME} \
    --region us-east-1 \
    --profile mtnfog
