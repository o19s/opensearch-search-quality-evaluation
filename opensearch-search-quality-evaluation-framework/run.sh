#!/bin/bash -e

# Create a click model.
java -jar ./target/search-evaluation-framework-1.0.0-SNAPSHOT-jar-with-dependencies.jar -c coec

# Run a query set.
#java -jar ./target/search-evaluation-framework-1.0.0-SNAPSHOT-jar-with-dependencies.jar -r queryset.json
