
# UBI tracking data generation

The `generate_data.py` script generates UBI tracking events that can be used to test
UBI related infrastructure such as dashboards and evaluation frameworks.

The script requires input judgments for query and document pairs.
At the moment it supports the Amazon ESCI dataset as an input.

## Usage

Generate query, view and click events for the Amazon ESCI dataset for the top 1000 queries, altogether
generate 100k query events and populate into OpenSearch.

```
python generate_data.py --esci-dataset ../../../../esci-data/shopping_queries_dataset --num-unique-queries 1000 --num-query-events 100000 --generate-open-search
```

Alternatively you can save the generated events into an ndjson file for later ingestion through the bulk endpoint:

```
python generate_data.py --esci-dataset ../../../../esci-data/shopping_queries_dataset --num-unique-queries 1000 --num-query-events 100000 --generate-ndjson
```

## Interpreting the output

### Expected CTR per rank
This is the CTR per rank that is expected from the generated events.
This quantity is relevant for the calculation of COEC.

### Expected judgment under COEC for 5 documents over top 3 queries
Here the script shows the documents that it has selected as the top 5 search results for the top 3 queries.
The `p_click` column shows the click probability with which clicks are generated.
The script attempts to have the expected COEC score, that is `p_click` divided by the CTR at that rank,
to be the same as the original rating (column `rating`), but this does not always work.

## Goal and design choices

The goal of the script is to generate events that can be used for judgment
calculation based on implicit feedback. The calculated judgments are designed
to match the input judgments.

Goals:
 * Test calculation of judgments based on implicit feedback.
 * Test calculation of judgments at scale.
 * End to end testing of the Search Quality Evaluation Framework.

Non-goals:
 * At the moment the script does not aim to generate realistic events.

The design choices are:
 * Events are only generated for query document pairs that have a judgment.
 * The query frequency distribution is simplistic and non-realistic (proportional to the number of judgments).
 * The current click generation assumes that judgments are calculated using COEC
   (clicks over expected clicks) and as such the click generation aims to reproduce the input
   judgments. This in particular implies:
   * The expected judgment for a query document is near the original judgment value. That is
     `ctr(q, d) / ctr_at_pos(p) = orig_judgment(q, d)` where p is the position of the document.
   * To achieve this, the script assigns products as query results such that the average expected
     judgment is 1.0 at that rank. This simplifies giving a click rate to a query document pair
     where the equation above holds.

# Future Work

 * Enable other data sources besides Amazon ESCI, probably soon Quepid.
 * More realistic data generation:
   * More realistic query frequency distribution
   * Top results of a query have a higher relevance
   * More realistic click generation