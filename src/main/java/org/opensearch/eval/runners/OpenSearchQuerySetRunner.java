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
import org.opensearch.eval.engine.SearchEngine;
import org.opensearch.eval.metrics.DcgSearchMetric;
import org.opensearch.eval.metrics.NdcgSearchMetric;
import org.opensearch.eval.metrics.PrecisionSearchMetric;
import org.opensearch.eval.metrics.SearchMetric;
import org.opensearch.eval.model.QueryRun;
import org.opensearch.eval.model.dao.querysets.QuerySet;
import org.opensearch.eval.model.dao.querysets.QuerySetRunParameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A {@link AbstractQuerySetRunner} for Amazon OpenSearch.
 */
public class OpenSearchQuerySetRunner extends AbstractQuerySetRunner {

    private static final Logger LOGGER = LogManager.getLogger(OpenSearchQuerySetRunner.class);

    public static final String QUERY_PLACEHOLDER = "%SearchText%";

    /**
     * Creates a new query set runner
     * @param searchEngine An OpenSearch engine {@link SearchEngine}.
     */
    public OpenSearchQuerySetRunner(final SearchEngine searchEngine) {
        super(searchEngine);
    }

    @Override
    public QuerySetRunResult run(final QuerySetRunParameters querySetParameters) throws Exception {

        // Verify the given query set and judgment set exists before trying to run.
        if(!searchEngine.doesQuerySetExist(querySetParameters.getQuerySetId())) {
            LOGGER.error("The given query set {} does not exist", querySetParameters.getQuerySetId());
            throw new IllegalArgumentException("The given query set " + querySetParameters.getQuerySetId() + " does not exist");
        }

        final long judgmentCount = searchEngine.getJudgmentsCount(querySetParameters.getJudgmentsId());
        if(judgmentCount == 0) {
            LOGGER.error("There are no judgments with the judgment set ID {}", querySetParameters.getJudgmentsId());
            throw new IllegalArgumentException("There are no judgments with the judgment set ID " + querySetParameters.getJudgmentsId());
        }

        final QuerySet querySet = searchEngine.getQuerySet(querySetParameters.getQuerySetId());
        LOGGER.info("Found {} queries in query set {}", querySet.getQuerySetQueries().size(), querySetParameters.getQuerySetId());

        try {

            // The results of each query.
            final List<QueryResult> queryResults = new ArrayList<>();

            // Loop over each query in the map and run each one.
            for (Map<String, Long> queryMap : querySet.getQuerySetQueries()) {

                for (final String userQuery : queryMap.keySet()) {

                    // This is to keep OpenSearch from rejecting queries.
                    // TODO: Look at using the Workload Management new in 2.18.0.
                    Thread.sleep(50);

                    // These are the documents returned for the query.
                    final QueryRun queryRun = searchEngine.runQuery(
                            querySetParameters.getIndex(),
                            querySetParameters.getQuery(),
                            querySetParameters.getK(),
                            userQuery,
                            querySetParameters.getIdField(),
                            querySetParameters.getSearchPipeline());

                    // Calculate the metrics given the documents returned for the user_query.
                    final int k = querySetParameters.getK();
                    final RelevanceScores relevanceScores = getRelevanceScores(querySetParameters.getJudgmentsId(), userQuery, queryRun.getDocumentIds(), k);
                    final Collection<SearchMetric> searchMetrics = calculateSearchMetrics(querySetParameters, k, relevanceScores);

                    queryResults.add(new QueryResult(userQuery, queryRun.getDocumentIds(), k, searchMetrics, relevanceScores.getFrogs(), queryRun.getNumberOfResults()));

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
            final QuerySetRunResult querySetRunResult = new QuerySetRunResult(querySetRunId, querySetParameters.getQuerySetId(),
                    queryResults, querySetMetrics, querySetParameters.getApplication(), querySetParameters.getSearchConfig());

            LOGGER.info("Query set run complete: {}", querySetRunId);

            return querySetRunResult;

        } catch (Exception ex) {
            throw new RuntimeException("Unable to run query set. If using a search_pipeline make sure the pipeline exists.", ex);
        }

    }

    private Collection<SearchMetric> calculateSearchMetrics(QuerySetRunParameters querySetParameters, int k, RelevanceScores relevanceScores) {

        final SearchMetric dcgSearchMetric = new DcgSearchMetric(k, relevanceScores.getRelevanceScores());
        final SearchMetric ndcgSearchmetric = new NdcgSearchMetric(k, relevanceScores.getRelevanceScores());
        final SearchMetric precisionSearchMetric = new PrecisionSearchMetric(k, querySetParameters.getThreshold(), relevanceScores.getRelevanceScores());

        return List.of(dcgSearchMetric, ndcgSearchmetric, precisionSearchMetric);

    }

}
