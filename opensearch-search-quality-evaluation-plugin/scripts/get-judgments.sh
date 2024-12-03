#!/bin/bash -e

curl -s "http://localhost:9200/judgments/_search" | jq
