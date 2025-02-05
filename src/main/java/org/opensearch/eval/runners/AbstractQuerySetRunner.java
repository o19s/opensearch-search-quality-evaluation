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

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for query set runners. Classes that extend this class
 * should be specific to a search engine. See the {@link OpenSearchQuerySetRunner} for an example.
 */
public abstract class AbstractQuerySetRunner {

    private static final Logger LOGGER = LogManager.getLogger(AbstractQuerySetRunner.class);

    protected final SearchEngine searchEngine;

    public AbstractQuerySetRunner(final SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    /**
     * Runs the query set.
     * @param querySetParameters A {@link RunQuerySetParameters parameters}.
     * @return The query set {@link QuerySetRunResult results} and calculated metrics.
     */
    abstract QuerySetRunResult run(RunQuerySetParameters querySetParameters) throws Exception;

    /**
     * Gets the judgments for a query / document pairs.
     * @param judgmentsId The judgments collection for which the judgment to retrieve belongs.
     * @param query The user query.
     * @param orderedDocumentIds A list of document IDs returned for the user query.
     * @param k The k used for metrics calculation, i.e. DCG@k.
     * @return An ordered list of relevance scores for the query / document pairs.
     * @throws Exception Thrown if a judgment cannot be retrieved.
     */
    protected RelevanceScores getRelevanceScores(final String judgmentsId, final String query, final List<String> orderedDocumentIds, final int k) throws Exception {

        // An ordered list of scores.
        final List<Double> scores = new ArrayList<>();

        // Count the number of documents without judgments.
        int documentsWithoutJudgmentsCount = 0;

        // For each document (up to k), get the judgment for the document.
        for (int i = 0; i < k && i < orderedDocumentIds.size(); i++) {

            final String documentId = orderedDocumentIds.get(i);

            // Find the judgment value for this combination of query and documentId from the index.
            final Double judgmentValue = searchEngine.getJudgmentValue(judgmentsId, query, documentId);

            // If a judgment for this query/doc pair is not found, Double.NaN will be returned.
            if(!Double.isNaN(judgmentValue)) {
                LOGGER.info("Score found for document ID {} with judgments {} and query {} = {}", documentId, judgmentsId, query, judgmentValue);
                scores.add(judgmentValue);
            } else {
                //LOGGER.info("No score found for document ID {} with judgments {} and query {}", documentId, judgmentsId, query);
                documentsWithoutJudgmentsCount++;
            }

        }

        double frogs = ((double) documentsWithoutJudgmentsCount) / orderedDocumentIds.size();

        if(Double.isNaN(frogs)) {
            frogs = 1.0;
        }

        // Multiply by 100 to be a percentage.
        frogs *= 100;

        LOGGER.info("frogs for query {} = {} ------- {} / {}", query, frogs, documentsWithoutJudgmentsCount, orderedDocumentIds.size());

        return new RelevanceScores(scores, frogs);

    }

}
