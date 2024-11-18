#!/bin/bash -e

#QUERY_SET=`curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/queryset?name=test&description=fake&sampling=pptss" | jq .query_set | tr -d '"'`
curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/queryset?name=test&description=fake&sampling=none&max_queries=500"

#echo ${QUERY_SET}

#curl -s http://localhost:9200/search_quality_eval_query_sets/_search | jq

# Run the query set now.
#curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/run?id=${QUERY_SET}" | jq

