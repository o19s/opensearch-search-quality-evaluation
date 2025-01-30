/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.runners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.Constants;
import org.opensearch.eval.engine.SearchEngine;
import org.opensearch.eval.metrics.DcgSearchMetric;
import org.opensearch.eval.metrics.NdcgSearchMetric;
import org.opensearch.eval.metrics.PrecisionSearchMetric;
import org.opensearch.eval.metrics.SearchMetric;
import org.opensearch.eval.model.data.QueryResultMetric;
import org.opensearch.eval.model.data.QuerySet;
import org.opensearch.eval.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.opensearch.eval.Constants.DASHBOARD_METRICS_INDEX_NAME;

/**
 * A {@link AbstractQuerySetRunner} for Amazon OpenSearch.
 */
public class OpenSearchQuerySetRunner extends AbstractQuerySetRunner {

    private static final Logger LOGGER = LogManager.getLogger(OpenSearchQuerySetRunner.class);

    public static final String QUERY_PLACEHOLDER = "#$query##";

    /**
     * Creates a new query set runner
     *
     * @param searchEngine An OpenSearch engine {@link SearchEngine}.
     */
    public OpenSearchQuerySetRunner(final SearchEngine searchEngine) {
        super(searchEngine);
    }

    @Override
    public QuerySetRunResult run(final RunQuerySetParameters querySetParameters) throws Exception {

        // Verify the given query set and judgment set exists prior to trying to run.
        if(!searchEngine.doesQuerySetExist(querySetParameters.getQuerySetId())) {
            LOGGER.error("The given query set {} does not exist", querySetParameters.getQuerySetId());
            throw new IllegalArgumentException("The given query set " + querySetParameters.getQuerySetId() + " does not exist");
        }

        final long judgmentCount = searchEngine.getJudgments(Constants.JUDGMENTS_INDEX_NAME, querySetParameters.getJudgmentsId());
        if(judgmentCount == 0) {
            LOGGER.error("There are no judgments with the judgment set ID {}", querySetParameters.getJudgmentsId());
            throw new IllegalArgumentException("There are no judgments with the judgment set ID " + querySetParameters.getJudgmentsId());
        }

        final QuerySet querySet = searchEngine.getQuerySet(querySetParameters.getQuerySetId());
        LOGGER.info("Found {} queries in query set {}", querySet.getQuerySetQueries().size(), querySetParameters.getQuerySetId());

        try {

            // The results of each query.
            final List<QueryResult> queryResults = new ArrayList<>();

            for (Map<String, Long> queryMap : querySet.getQuerySetQueries()) {

                // Loop over each query in the map and run each one.
                for (final String userQuery : queryMap.keySet()) {

                    // This is to keep OpenSearch from rejecting queries.
                    // TODO: Look at using the Workload Management in 2.18.0.
                    Thread.sleep(50);

                    final List<String> orderedDocumentIds = searchEngine.runQuery(
                            querySetParameters.getIndex(),
                            querySetParameters.getQuery(),
                            querySetParameters.getK(),
                            userQuery,
                            querySetParameters.getIdField(),
                            querySetParameters.getSearchPipeline());

                    try {

                        final int k = querySetParameters.getK();
                        final RelevanceScores relevanceScores = getRelevanceScores(querySetParameters.getJudgmentsId(), userQuery, orderedDocumentIds, k);

                        // Calculate the metrics for this query.
                        final SearchMetric dcgSearchMetric = new DcgSearchMetric(k, relevanceScores.getRelevanceScores());
                        final SearchMetric ndcgSearchmetric = new NdcgSearchMetric(k, relevanceScores.getRelevanceScores());
                        final SearchMetric precisionSearchMetric = new PrecisionSearchMetric(k, querySetParameters.getThreshold(), relevanceScores.getRelevanceScores());

                        final Collection<SearchMetric> searchMetrics = List.of(dcgSearchMetric, ndcgSearchmetric, precisionSearchMetric);

                        queryResults.add(new QueryResult(userQuery, orderedDocumentIds, k, searchMetrics, relevanceScores.getFrogs()));

                    } catch (Exception ex) {
                        LOGGER.error("Unable to get relevance scores for judgments {} and user query {}.", querySetParameters.getJudgmentsId(), userQuery, ex);
                    }

                }

            }

            // Calculate the search metrics for the entire query set given the individual query set metrics.
            // Sum up the metrics for each query per metric type.
            final int querySetSize = queryResults.size();
            final Map<String, Double> sumOfMetrics = new HashMap<>();
            for (final QueryResult queryResult : queryResults) {
                for (final SearchMetric searchMetric : queryResult.getSearchMetrics()) {
                    //LOGGER.info("Summing: {} - {}", searchMetric.getName(), searchMetric.getValue());
                    sumOfMetrics.merge(searchMetric.getName(), searchMetric.getValue(), Double::sum);
                }
            }

            // Now divide by the number of queries.
            final Map<String, Double> querySetMetrics = new HashMap<>();
            for (final String metric : sumOfMetrics.keySet()) {
                //LOGGER.info("Dividing by the query set size: {} / {}", sumOfMetrics.get(metric), querySetSize);
                querySetMetrics.put(metric, sumOfMetrics.get(metric) / querySetSize);
            }

            final String querySetRunId = UUID.randomUUID().toString();
            final QuerySetRunResult querySetRunResult = new QuerySetRunResult(querySetRunId, querySetParameters.getQuerySetId(), queryResults, querySetMetrics);

            save(querySetRunResult);

            LOGGER.info("Query set run complete: {}", querySetRunId);

            return querySetRunResult;

        } catch (Exception ex) {
            throw new RuntimeException("Unable to run query set. If using a search_pipeline make sure the pipeline exists.", ex);
        }

    }

    @Override
    public void save(final QuerySetRunResult result) throws Exception {

        LOGGER.info("Indexing query run results.");

        // Now, index the metrics as expected by the dashboards.

        // See https://github.com/o19s/opensearch-search-quality-evaluation/blob/main/opensearch-dashboard-prototyping/METRICS_SCHEMA.md
        // See https://github.com/o19s/opensearch-search-quality-evaluation/blob/main/opensearch-dashboard-prototyping/sample_data.ndjson

        final boolean dashboardMetricsIndexExists = searchEngine.doesIndexExist(DASHBOARD_METRICS_INDEX_NAME);

        if (!dashboardMetricsIndexExists) {

            // Create the index.
            // TODO: Read this mapping from a resource file instead.
            final String mapping = "{\n" +
                    "              \"properties\": {\n" +
                    "                \"datetime\": { \"type\": \"date\", \"format\": \"strict_date_time\" },\n" +
                    "                \"search_config\": { \"type\": \"keyword\" },\n" +
                    "                \"query_set_id\": { \"type\": \"keyword\" },\n" +
                    "                \"query\": { \"type\": \"keyword\" },\n" +
                    "                \"metric\": { \"type\": \"keyword\" },\n" +
                    "                \"value\": { \"type\": \"double\" },\n" +
                    "                \"application\": { \"type\": \"keyword\" },\n" +
                    "                \"evaluation_id\": { \"type\": \"keyword\" },\n" +
                    "                \"frogs_percent\": { \"type\": \"double\" }\n" +
                    "              }\n" +
                    "          }";

            // TODO: Make sure the index gets created successfully.
            searchEngine.createIndex(DASHBOARD_METRICS_INDEX_NAME, mapping);

        }

        final String timestamp = TimeUtils.getTimestamp();

        for(final QueryResult queryResult : result.getQueryResults()) {

            for(final SearchMetric searchMetric : queryResult.getSearchMetrics()) {

                final QueryResultMetric queryResultMetric = new QueryResultMetric();
                queryResultMetric.setDatetime(timestamp);
                queryResultMetric.setSearchConfig("research_1");
                queryResultMetric.setQuerySetId(result.getQuerySetId());
                queryResultMetric.setQuery(queryResult.getQuery());
                queryResultMetric.setMetric(searchMetric.getName());
                queryResultMetric.setValue(searchMetric.getValue());
                queryResultMetric.setApplication("sample_data");
                queryResultMetric.setEvaluationId(result.getRunId());
                queryResultMetric.setFrogsPercent(queryResult.getFrogs());

                searchEngine.indexQueryResultMetric(queryResultMetric);

            }

        }

    }

}
