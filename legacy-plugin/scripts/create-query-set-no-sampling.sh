#!/bin/bash -e

curl -s -X DELETE "http://localhost:9200/search_quality_eval_query_sets"

curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/queryset?name=test&description=fake&sampling=none&query_set_size=10"
