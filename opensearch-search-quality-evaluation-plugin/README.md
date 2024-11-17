# OpenSearch Evaluation Framework

This is an OpenSearch plugin built on the OpenSearch job scheduler plugin.

## API Endpoints

| Method | Endpoint                                | Description                                                                                                                            |
|--------|-----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/_plugins/search_quality_eval/queryset`  | Create a query set by sampling from the `ubi_queries` index. The `name`, `description`, and `sampling` method parameters are required. |
| `POST` | `/_plugins/search_quality_eval/run`       | Initiate a run of a query set. The `name` of the query set is a required parameter.                                                    |
| `POST` | `/_plugins/search_quality_eval/judgments` | Generate implicit judgments from UBI events and queries now.                                                                           |
| `POST`  | `/_plugins/search_quality_eval/schedule`  | Create a scheduled job to generate implicit judgments.                                                                                 |


## Building

Build the project from the top-level directory to build all projects.

```
cd ..
./gradlew build
```

## Running in Docker

From this directory:

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
curl -s -X POST "http://localhost:9200/_plugins/search_quality_eval/schedule?id=1&click_model=coec&job_name=test&interval=60" | jq
```

See the created job:

```
curl -s http://localhost:9200/search_quality_eval_scheduled_jobs/_search | jq
```

To run an on-demand job without scheduling:

```
curl -X POST "http://localhost:9200/_plugins/search_quality_eval/judgments?click_model=coec&max_rank=20" | jq
```

To see the job runs:

```
curl -X POST "http://localhost:9200/search_quality_eval_completed_jobs/_search" | jq
```

See the first 10 judgments:

```
curl -s http://localhost:9200/judgments/_search | jq
```