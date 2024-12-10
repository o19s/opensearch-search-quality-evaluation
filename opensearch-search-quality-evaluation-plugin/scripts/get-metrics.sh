#!/bin/bash -e

curl -s "http://localhost:9200/sqe_metrics_sample_data/_search" | jq
