/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.clickmodel.coec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.engine.SearchEngine;
import org.opensearch.eval.judgments.clickmodel.ClickModel;
import org.opensearch.eval.judgments.queryhash.IncrementalUserQueryHash;
import org.opensearch.eval.model.ClickthroughRate;
import org.opensearch.eval.model.dao.judgments.Judgment;
import org.opensearch.eval.utils.MathUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class CoecClickModel extends ClickModel {

    public static final String CLICK_MODEL_NAME = "coec";

    // UBI event names.
    public static final String EVENT_CLICK = "click";
    public static final String EVENT_IMPRESSION = "impression";

    private final CoecClickModelParameters parameters;

    private final IncrementalUserQueryHash incrementalUserQueryHash = new IncrementalUserQueryHash();
    private final SearchEngine searchEngine;

    private static final Logger LOGGER = LogManager.getLogger(CoecClickModel.class.getName());

    public CoecClickModel(final SearchEngine searchEngine, final CoecClickModelParameters parameters) {

        this.parameters = parameters;
        this.searchEngine = searchEngine;

    }

    @Override
    public String calculateJudgments() throws Exception {

        final int maxRank = parameters.getMaxRank();

        // Calculate and index the rank-aggregated click-through.
        LOGGER.info("Beginning calculation of rank-aggregated click-through.");
        final Map<Integer, Double> rankAggregatedClickThrough = searchEngine.getRankAggregatedClickThrough(maxRank);
        LOGGER.info("Rank-aggregated clickthrough positions: {}", rankAggregatedClickThrough.size());
        showRankAggregatedClickThrough(rankAggregatedClickThrough);

        // Calculate and index the click-through rate for query/doc pairs.
        LOGGER.info("Beginning calculation of clickthrough rates.");
        final Map<String, Set<ClickthroughRate>> clickthroughRates = searchEngine.getClickthroughRate(maxRank);
        LOGGER.info("Clickthrough rates for number of queries: {}", clickthroughRates.size());
        showClickthroughRates(clickthroughRates);

        // Generate and index the implicit judgments.
        LOGGER.info("Beginning calculation of implicit judgments.");
        return calculateCoec(rankAggregatedClickThrough, clickthroughRates);

    }

    public String calculateCoec(final Map<Integer, Double> rankAggregatedClickThrough,
                                final Map<String, Set<ClickthroughRate>> clickthroughRates) throws Exception {

        // Calculate the COEC.
        // Numerator is the total number of clicks received by a query/result pair.
        // Denominator is the expected clicks (EC) that an average result would receive after being impressed i times at rank r,
        // and CTR is the average CTR for each position in the results page (up to R) computed over all queries and results.

        // Format: query_id, query, document, judgment
        final Collection<Judgment> judgments = new LinkedList<>();

        LOGGER.info("Count of queries: {}", clickthroughRates.size());

        for(final String userQuery : clickthroughRates.keySet()) {

            // The clickthrough rates for this one query.
            // A ClickthroughRate is a document with counts of impressions and clicks.
            final Collection<ClickthroughRate> ctrs = clickthroughRates.get(userQuery);

            // Go through each clickthrough rate for this query.
            for(final ClickthroughRate ctr : ctrs) {

                double denominatorSum = 0;

                for(int rank = 0; rank < parameters.getMaxRank(); rank++) {

                    // The document's mean CTR at the rank.
                    final double meanCtrAtRank = rankAggregatedClickThrough.getOrDefault(rank, 0.0);

                    // The number of times this document was shown as this rank.
                    final long countOfTimesShownAtRank = searchEngine.getCountOfQueriesForUserQueryHavingResultInRankR(userQuery, ctr.getObjectId(), rank);

                    denominatorSum += (meanCtrAtRank * countOfTimesShownAtRank);

                }

                // Numerator is the sum of clicks at all ranks up to the maxRank.
                final long totalNumberClicksForQueryResult = ctr.getClicks();

                // Divide the numerator by the denominator (value).
                final double judgmentValue;

                if(denominatorSum == 0) {
                    judgmentValue = 0.0;
                } else {
                    judgmentValue = totalNumberClicksForQueryResult / denominatorSum;
                }

                // Hash the user query to get a query ID.
                final int queryId = incrementalUserQueryHash.getHash(userQuery);

                // Add the judgment to the list.
                // TODO: What to do for query ID when the values are per user_query instead?
                final Judgment judgment = new Judgment();
                judgment.setQueryId(String.valueOf(queryId));
                judgment.setQuery(userQuery);
                judgment.setDocument(ctr.getObjectId());
                judgment.setJudgment(judgmentValue);
                judgment.setJudgmentSetType(parameters.getJudgmentParameters().getJudgmentSetType());
                judgment.setJudgmentSetGenerator(parameters.getJudgmentParameters().getJudgmentSetGenerator());
                judgment.setJudgmentSetName(parameters.getJudgmentParameters().getJudgmentSetName());
                judgment.setJudgmentSetParameters(parameters.getJudgmentParameters().getJudgmentSetParameters());

                judgments.add(judgment);

            }

        }

        LOGGER.info("Count of user queries: {}", clickthroughRates.size());
        LOGGER.info("Count of judgments: {}", judgments.size());

        showJudgments(judgments);

        if(!(judgments.isEmpty())) {
            return searchEngine.indexJudgments(judgments);
        } else {
            return null;
        }

    }

    private void showJudgments(final Collection<Judgment> judgments) {

        for(final Judgment judgment : judgments) {
            LOGGER.debug(judgment.toJudgmentString());
        }

    }

    private void showClickthroughRates(final Map<String, Set<ClickthroughRate>> clickthroughRates) {

        for(final String userQuery : clickthroughRates.keySet()) {

            LOGGER.debug("user_query: {}", userQuery);

            for(final ClickthroughRate clickthroughRate : clickthroughRates.get(userQuery)) {
                LOGGER.debug("\t - {}", clickthroughRate.toString());
            }

        }

    }

    private void showRankAggregatedClickThrough(final Map<Integer, Double> rankAggregatedClickThrough) {

        for(final int position : rankAggregatedClickThrough.keySet()) {
            LOGGER.info("Position: {}, # ctr: {}", position, MathUtils.round(rankAggregatedClickThrough.get(position), parameters.getRoundingDigits()));
        }

    }

}
