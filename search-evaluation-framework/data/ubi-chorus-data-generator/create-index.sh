#!/bin/bash -e

curl -s -X PUT "http://localhost:9200/ecommerce/" -H 'Content-Type: application/json' --data-binary @./schema.json
curl -s -X POST "http://localhost:9200/ecommerce/_bulk?pretty=false&filter_path=-items" -H 'Content-Type: application/json' --data-binary @transformed_data.json
