#!/bin/bash -e

# Run a query set.
java -jar ../target/search-evaluation-framework.jar -o http://localhost:9200 -r run-query-set.json
