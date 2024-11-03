# Introduction

This file has an informal description of the metrics schema that supports the Dashboards.

We chose simplicity in the design of the metrics schema. 

The design of the metrics schema follows these principles:
 * Simplicity: a denormalized design that facilitates filtering and aggregation without the need
   of joining with additional data sources.
 * Pre-computation: we assume that metrics are pre-computed and stored in the metrics data source.
   The computation of metrics is not done dynamically when preparing the dashboard.
 * Query granularity: metrics are computed down at the query level. This allows the metrics data
   source to be used to investigate individual queries, while at the same time, it is easy to aggregate
   to report summarized results.

# The Schema

Each data point in the metrics source represents an evaluation under a given metric of a search configuration
on the results that it returns for a query. The fields are as follows:
 * **application**: the name of the search application being considered (for the sample data included in the dashboards "sample_data")
 * **datetime**: the date and time in which the evaluation took place in ISO 8601 format
 * **search_config**: the search configuration that this data point evaluates
 * **query_set_id**: unique string identifier that the query being evaluated belongs to
 * **query**: the query string on which the search configuration is evaluated
 * **metric**: the metric name that is used for evaluation (examples: "dcg", "ndcg")
 * **value**: a floating point number with the metric value
 * **evaluation_id**: a string that uniquely identifies the evaluation run

# Data samples

A data sample for metrics can be found in `sample_data.ndjson` and in `data_notebooks/metrics.csv`