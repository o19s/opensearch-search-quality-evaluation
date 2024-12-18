#!/bin/bash -e

aws cloudformation validate-template \
    --template-body file://template.yaml \
    --region us-east-1 \
    --profile mtnfog
