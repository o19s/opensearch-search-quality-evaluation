# Search Evaluation Framework

Implementing the search quality evaluation framework as described in the [RFC](https://github.com/opensearch-project/OpenSearch/issues/15354).

## Notes

%sql
select a.query_id, b.event_attributes.position, a.action_name from ubi_events a join ubi_events b on a.query_id = b.query_id where a.action_name = "click"