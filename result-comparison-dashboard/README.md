# Result Comparison Dashboards

The result comparison dashboards intend to quantify change between the result sets of two search configurations.

`sample_data.ndjson` contains 40 documents:

20 that simulate the results from comparing two search configurations with different title boosts, 20 that simulate the results from comparing a baseline to a hybrid search configuration.

`result_comparison_dashboard.ndjson` contains a dashboard draft that allows picking the search configurations and results to compare to each other and visualize change-based metrics (Jaccard, RBO and a custom metric factoring in business importance) and also search quality metrics (DCG@10, NDCG@10, Precision@10) and how the inidividual queries performed for the search configurations.

# How to load the result comparison dashboards into your OpenSearch installation

Run the following shell script: `./install_dashboards.sh http://localhost:9200 http://localhost:5601`

To deploy in the Chorus for OpenSearch environment it would be: `./install_dashboards.sh http://chorus-opensearch-edition.dev.o19s.com:9200 http://chorus-opensearch-edition.dev.o19s.com:5601`