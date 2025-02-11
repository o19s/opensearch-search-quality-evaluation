# PR/FAQ

## WHO? Announces the Search Evaluation Framework as Part of the Search Relevance Workbench

Today, WHO?, announced the first release of the Search Evaluation Framework for OpenSearch. As part of the Search Relevance Workbench, the Search Evaluation Framework   seamlessly integrates with OpenSearch, providing a comprehensive solution for evaluating and improving search relevance.

The Search Evaluation Framework empowers users to:

* Create Judgments: Easily define and manage relevance judgments for search results, establishing ground truth for evaluation.
* Build Query Sets: Efficiently create and organize query sets to represent diverse search intents and user needs.
* Run Query Sets: Execute query sets against OpenSearch, retrieving search results for analysis.
* Compare Search Relevance Metrics: Visualize and compare key search relevance metrics within the OpenSearch Dashboards, gaining actionable insights into search performance.

Key Features and Benefits:

* Seamless OpenSearch Integration: Works seamlessly with OpenSearch, leveraging its capabilities for search and data analysis.
* Comprehensive Metrics: Provides a wide range of search relevance metrics to assess search performance from various perspectives.
* Visualizations in OpenSearch Dashboards: Leverages the power of OpenSearch Dashboards to visualize and compare metrics, facilitating data-driven insights.
  
Availability:

The Search Evaluation Framework is available now as part of the Search Relevance Workbench. To learn more and get started, please visit https://github.com/o19s/opensearch-search-quality-evaluation.

## Internal FAQ

### What's the core purpose of the Search Evaluation Framework?

It's designed to streamline and standardize how we evaluate search relevance within OpenSearch. This gives us a consistent, data-driven approach to measure and improve our search experience. Think of it as the tool that helps us answer "How good is our search?" with actual data.

### Who is the target user for this framework?

Primarily, search engineers, relevance engineers, product managers, and anyone involved in improving the search experience. We want it to be accessible to anyone who needs to understand or influence search performance.

### How does this fit into the broader Search Relevance Workbench?

The Search Evaluation Framework is a key component within the Search Relevance Workbench. It's the part that specifically handles creating judgments, running query sets, and analyzing metrics. The Workbench provides the overall environment, and this framework provides the specific tools for evaluation.

### What's the underlying technology stack?

It's built to work directly with OpenSearch and leverages OpenSearch Dashboards for visualization.

### How are judgments stored and managed?

TODO

### How scalable is the framework? Can it handle large query sets and massive datasets?

TODO

### What's the process for adding new metrics to the framework?

TODO

### What's the recommended workflow for using the framework to improve search relevance?

TODO

### How do we ensure the quality and consistency of our judgments?

TODO

### Where can I find documentation and examples for using the framework?

TODO

## External FAQ

### What is the Search Evaluation Framework?

The Search Evaluation Framework is a powerful tool within the Search Relevance Workbench designed to simplify and enhance the process of evaluating and improving search relevance in OpenSearch.  It allows users to create judgments, build query sets, run those sets against OpenSearch, and then analyze and compare search relevance metrics directly within OpenSearch Dashboards.

### How does the Search Evaluation Framework integrate with OpenSearch?

The framework seamlessly integrates with OpenSearch. It leverages OpenSearch's search capabilities to retrieve results for your query sets and uses OpenSearch Dashboards for visualizing and comparing the resulting search relevance metrics. This tight integration streamlines the entire evaluation workflow.

### What are "judgments" and why are they important?

Judgments are essentially your ground truth. They represent human assessments of the relevance of search results for specific queries.  You define how relevant a particular result is to a given query (e.g., highly relevant, relevant, irrelevant). These judgments serve as the benchmark against which the performance of your search engine is measured.

### What are "query sets"?

Query sets are collections of search queries that represent the types of searches users might perform. By running these query sets against your OpenSearch instance, you can evaluate how well your search engine performs across a range of realistic search scenarios.

### What kind of metrics does the Search Evaluation Framework provide?

The framework provides a variety of search relevance metrics, allowing you to assess search performance from multiple angles. These metrics might include things like Normalized Discounted Cumulative Gain (NDCG), and others.  The specific metrics available may be configurable.

### Where can I view the search relevance metrics?

All metrics are visualized within OpenSearch Dashboards. This allows you to leverage the powerful visualization capabilities of Dashboards to easily compare metrics, identify trends, and gain actionable insights into search performance.

### Do I need any special technical skills to use the Search Evaluation Framework?

While familiarity with OpenSearch and search concepts is helpful, the framework is designed to be user-friendly. The intuitive interface simplifies the process of creating judgments, building query sets, and interpreting the results.

### How can I get started with the Search Evaluation Framework?

The Search Evaluation Framework is a component of the Search Relevance Workbench.  You can visit https://github.com/o19s/opensearch-search-quality-evaluation to learn more about the Workbench and how to get started.

### What support is available for the Search Evaluation Framework?

Find us in the OpenSearch Slack!
