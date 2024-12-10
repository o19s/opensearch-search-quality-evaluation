#!/bin/bash -e

curl -s -X DELETE "http://localhost:9200/judgments,search_quality_eval_completed_jobs,search_quality_eval_query_sets_run_results" | jq
curl -s -X DELETE "http://localhost:9200/search_quality_eval_completed_jobs" | jq
curl -s -X DELETE "http://localhost:9200/search_quality_eval_query_sets_run_results" | jq
curl -s -X DELETE "http://localhost:9200/ubi_queries,ubi_events" | jq
