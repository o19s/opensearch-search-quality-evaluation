#!/bin/bash -e

. stack.properties

# This script is just here for an example.
# AWS OpenSearch does not (yet) support ActionPlugins.

# https://docs.aws.amazon.com/opensearch-service/latest/developerguide/custom-plugins.html
# Note that custom plugins cannot implement ActionPlugin.

FILE_NAME="search-quality-evaluation-plugin-0.0.1.zip"

wget -O ${FILE_NAME} https://github.com/o19s/opensearch-search-quality-evaluation/releases/download/0.0.1/${FILE_NAME}

aws s3 cp ${FILE_NAME} s3://ubi-queries-events-sink/${FILE_NAME} \
    --profile ${AWS_PROFILE} \
    --region ${AWS_REGION}

aws opensearch create-package \
    --region ${AWS_REGION} \
    --profile ${AWS_PROFILE} \
    --package-name search-eval-framework \
    --package-type ZIP-PLUGIN \
    --package-source S3BucketName=ubi-queries-events-sink,S3Key=search-quality-evaluation-plugin-0.0.1.zip \
    --engine-version OpenSearch_2.17

# aws opensearch describe-packages \
#     --region ${AWS_REGION} \
#     --profile ${AWS_PROFILE} \
#     --filters '[{"Name": "PackageType","Value": ["ZIP-PLUGIN"]}, {"Name": "PackageName","Value": ["search-eval-framework"]}]'

# when done, grab the package id and put into command:

# PACKAGE_ID="pkg-b618759e2c2d03c7b9934b214ce6d09fcfaa8547"

# aws opensearch associate-package \
#     --region ${AWS_REGION} \
#     --domain-name osi-ubi-domain \
#     --profile ${AWS_PROFILE}
#     --package-id ${PACKAGE_ID}


# aws opensearch list-packages-for-domain
#     --domain-name osi-ubi-domain \
#     --region $REGION \
#     --profile ${AWS_PROFILE}