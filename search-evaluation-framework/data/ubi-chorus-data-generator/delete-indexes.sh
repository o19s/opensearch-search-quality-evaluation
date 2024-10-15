#!/bin/bash -e

# Delete the UBI indexes.
curl -s -X DELETE http://localhost:9200/ubi_queries,ubi_events/ | jq

# Delete the judgment indexes.
curl -s -X DELETE http://localhost:9200/rank_aggregated_ctr,click_through_rates,judgments/ | jq
