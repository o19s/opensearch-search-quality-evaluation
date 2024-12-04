/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.metrics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.judgments.model.Judgment;
import org.opensearch.eval.runners.QueryResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides the ability to calculate search metrics and stores them.
 */
public class SearchMetrics {

    private static final Logger LOGGER = LogManager.getLogger(SearchMetrics.class.getName());

    private final int k;
    private final double dcg;
    private final double ndcg;
    private final double precision;

    /**
     * Calculate the search metrics for an entire query set.
     * @param queryResults A list of {@link QueryResult}.
     * @param judgments A list of {@link Judgment judgments} used for metric calculation.
     * @param k The k used for metrics calculation, i.e. DCG@k.
     */
    public SearchMetrics(final List<QueryResult> queryResults, final List<Judgment> judgments, final int k) {
        this.k = k;

        // TODO: Calculate the metrics for the whole query set.
        this.dcg = 0.0;
        this.ndcg = 0.0;
        this.precision = 0.0;
    }

    /**
     * Calculate the search metrics for a single query.
     * @param query The user query.
     * @param orderedDocumentIds The documents returned for the user query in order.
     * @param judgments A list of {@link Judgment judgments} used for metric calculation.
     * @param k The k used for metrics calculation, i.e. DCG@k.
     */
    public SearchMetrics(final String query, final List<String> orderedDocumentIds, final List<Judgment> judgments, final int k) {
        this.k = k;

        // TODO: Calculate the metrics for the single query.
        final List<Double> scores = getRelevanceScores(query, orderedDocumentIds, judgments, k);

        this.dcg = calculateDCG(scores);
        this.ndcg = 0.0;
        this.precision = 0.0;
    }

    /**
     * Gets the metrics as a map for ease of indexing.
     * @return A map of the search metrics.
     */
    public Map<String, Double> getSearchMetricsAsMap() {

        final Map<String, Double> metrics = new HashMap<>();
        metrics.put("dcg_at_" + k, dcg);
        metrics.put("ndcg_at_" + k, ndcg);
        metrics.put("prec_at_" + k, precision);

        return metrics;

    }

    private List<Double> getRelevanceScores(final String query, final List<String> orderedDocumentIds, final List<Judgment> judgments, final int k) {

        // Ordered list of scores.
        final List<Double> scores = new ArrayList<>();

        // Go through each document up to k and get the score.
        for(int i = 0; i < k; i++) {

            final String documentId = orderedDocumentIds.get(i);

            // Get the score for this document for this query.
            final Judgment judgment = Judgment.findJudgment(judgments, query, documentId);

            if(judgment != null) {
                scores.add(judgment.getJudgment());
            }

            if(i == orderedDocumentIds.size()) {
                // k is greater than the actual length of documents.
                break;
            }

        }

        String listOfScores = scores.stream().map(Object::toString).collect(Collectors.joining(", "));
        LOGGER.info("Got relevance scores: {}", listOfScores);

        return scores;

    }

    private double calculateDCG(final List<Double> relevanceScores) {
        double dcg = 0.0;
        for(int i = 0; i < relevanceScores.size(); i++) {
            double relevance = relevanceScores.get(i);
            dcg += relevance / Math.log(i + 2); // Add 2 to avoid log(1) = 0
        }
        return dcg;
    }

    private double calculateNDCG(final List<Double> relevanceScores, final List<Double> idealRelevanceScores) {
        double dcg = calculateDCG(relevanceScores);
        double idcg = calculateDCG(idealRelevanceScores);

        if(idcg == 0) {
            return 0; // Avoid division by zero
        }

        return dcg / idcg;
    }

    public int getK() {
        return k;
    }

    public double getDcg() {
        return dcg;
    }

    public double getNdcg() {
        return ndcg;
    }

    public double getPrecision() {
        return precision;
    }

}
