#!/bin/bash -e

# Script for indexing queries with empty string "" user queries.

for (( i=0; i<50; i++ ))
do

  QUERY_ID=$(uuidgen)
  CLIENT_ID=$(uuidgen)

  curl http://localhost:9200/ubi_queries/_doc/1 -H "Content-type: application/json" -d"{
    \"application\": \"esci_ubi_sample\",
    \"query_id\": \"${QUERY_ID}\",
    \"client_id\": \"${CLIENT_ID}\",
    \"user_query\": \"\",
    \"query_attributes\": {},
    \"timestamp\": \"2024-12-10T07:29:16.130Z\"
  }"

done

