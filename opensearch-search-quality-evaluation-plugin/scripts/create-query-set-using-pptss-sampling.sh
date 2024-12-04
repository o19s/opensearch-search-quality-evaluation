#!/bin/bash -e

curl -s -X DELETE "http://localhost:9200/search_quality_eval_query_sets"

curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/queryset?name=test&description=fake&sampling=pptss&query_set_size=100"


#QUERY_SET=`curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/queryset?name=test&description=fake&sampling=pptss" | jq .query_set | tr -d '"'`
#curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/queryset?name=test&description=fake&sampling=pptss&query_set_size=100"

#echo ${QUERY_SET}

#curl -s -X GET http://localhost:9200/search_quality_eval_query_sets/_doc/${QUERY_SET} | jq

# Run the query set now.
#curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/run?id=${QUERY_SET}" | jq

