# Search Evaluation Framework

This repository contains the search quality evaluation framework as described in the [RFC](https://github.com/opensearch-project/OpenSearch/issues/15354).

## Repository Contents

* `data` - The data directory contains scripts for creating random UBI queries and events for purposes of development and testing.
* `opensearch-search-quality-evaluation-plugin` - An OpenSearch plugin that extends the OpenSearch Scheduler plugin that provides the ability to generate scheduled (and on-demand) implicit judgments from UBI data.
* `opensearch-search-quality-implicit-judgments` - A standalone Java application to generate implicit judgments from indexed UBI data.

## OpenSearch Search Quality Evaluation Plugin

This is an OpenSearch plugin that extends the OpenSearch Scheduler plugin that provides the ability to generate scheduled (and on-demand) implicit judgments from UBI data.

To use the plugin:

```
./gradlew build
cd opensearch-search-quality-evaluation-plugin
docker compose build
docker compose up
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

## OpenSearch Search Quality Implicit Judgments

This is a standalone Java application to generate implicit judgments from indexed UBI data. It runs outside OpenSearch and queries the UBI indexes to get the data for calculating the implicit judgments.

To run it, run the `org.opensearch.qef.App` class. This will connect to OpenSearch running on `localhost:9200`. It expects the `ubi_events` and `ubi_queries` indexes to exist and be populated.

## License

This code is licensed under the Apache 2.0 License. See [LICENSE.txt](LICENSE.txt).
