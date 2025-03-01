#!/bin/bash -e

curl -s "http://localhost:9200/srw_coec_ctr/_search" | jq
