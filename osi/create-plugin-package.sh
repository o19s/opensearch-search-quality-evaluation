#!/bin/bash -e

# https://docs.aws.amazon.com/opensearch-service/latest/developerguide/custom-plugins.html
# Note that custom plugins cannot implement ActionPlugin.

FILE_NAME="search-quality-evaluation-plugin-0.0.1.zip"

wget -O ${FILE_NAME} https://github.com/o19s/opensearch-search-quality-evaluation/releases/download/0.0.1/${FILE_NAME}

aws s3 cp ${FILE_NAME} s3://ubi-queries-events-sink/${FILE_NAME} \
    --profile TODO_PUT_YOUR_AWS_PROFILE_NAME_HERE \
    --region us-east-1

aws opensearch create-package \
    --region us-east-1 \
    --profile TODO_PUT_YOUR_AWS_PROFILE_NAME_HERE \
    --package-name search-eval-framework \
    --package-type ZIP-PLUGIN \
    --package-source S3BucketName=ubi-queries-events-sink,S3Key=search-quality-evaluation-plugin-0.0.1.zip \
    --engine-version OpenSearch_2.17

# aws opensearch describe-packages \
#     --region us-east-1 \
#     --profile TODO_PUT_YOUR_AWS_PROFILE_NAME_HERE \
#     --filters '[{"Name": "PackageType","Value": ["ZIP-PLUGIN"]}, {"Name": "PackageName","Value": ["search-eval-framework"]}]'

# when done, grab the package id and put into command:

# PACKAGE_ID="pkg-b618759e2c2d03c7b9934b214ce6d09fcfaa8547"

# aws opensearch associate-package \
#     --region us-east-1 \
#     --domain-name osi-ubi-domain \
#     --profile TODO_PUT_YOUR_AWS_PROFILE_NAME_HERE
#     --package-id ${PACKAGE_ID}


# aws opensearch list-packages-for-domain
#     --domain-name osi-ubi-domain \
#     --region $REGION \
#     --profile TODO_PUT_YOUR_AWS_PROFILE_NAME_HERE