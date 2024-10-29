# OpenSearch Evaluation Framework

This is an OpenSearch plugin built on the OpenSearch job scheduler plugin.

## Building

Build the project from the top-level directory to build both projects.

```
cd ..
./gradlew build
```

## Running in Docker

```
docker compose build && docker compose up
```

Verify the plugin is installed:

```
curl http://localhost:9200/_cat/plugins
```

In the list returned you should see:

```
opensearch search-quality-evaluation-plugin     2.17.1.0-SNAPSHOT
```

To create a schedule to generate implicit judgments:

```
curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/judgments?id=1&index=ubi&job_name=test2&interval=1"
```

See the created job:

```
curl -s http://localhost:9200/.scheduler_search_quality_eval/_search
```