#!/bin/bash -e

curl -s http://localhost:9200/ecommerce/_search | jq
