#!/bin/bash -e

# Example walkthrough end-to-end for the plugin.

# Delete existing UBI indexes and create new ones.
curl -s -X DELETE "http://localhost:9200/ubi_queries,ubi_events"
curl -s -X POST "http://localhost:9200/_plugins/ubi/initialize"

# IMPORTANT: Now index data (UBI and ESCI).

# Create judgments.
curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/judgments?click_model=coec&max_rank=20"

# Create a query set.
curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/queryset?name=test&description=fake&sampling=pptss&query_set_size=100"

# Run the query set.
./run-query-set.sh ${QUERY_SET_ID}

# Look at the results.
curl -s "http://localhost:9200/search_quality_eval_query_sets_run_results/_search" | jq
