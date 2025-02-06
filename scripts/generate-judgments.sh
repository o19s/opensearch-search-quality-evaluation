#!/bin/bash -e

# Create a click model.
java -jar ../target/search-evaluation-framework.jar -o http://localhost:9200 -c judgments.json
