package org.opensearch.sef;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.sef.model.ClickthroughRate;
import org.opensearch.sef.model.Judgment;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Entry point for the OpenSearch Evaluation Framework.
 */
public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {

        final OpenSearchEvaluationFramework openSearchEvaluationFramework = new OpenSearchEvaluationFramework();

        final boolean persist = false;

        // Calculate and index the rank-aggregated click-through.
        final Map<Integer, Double> rankAggregatedClickThrough = openSearchEvaluationFramework.getRankAggregatedClickThrough(persist);
        LOGGER.info("Rank-aggregated clickthrough positions: {}", rankAggregatedClickThrough.size());
        openSearchEvaluationFramework.showRankAggregatedClickThrough(rankAggregatedClickThrough);

        // Calculate and index the click-through rate for query/doc pairs.
        final Map<String, Set<ClickthroughRate>> clickthroughRates = openSearchEvaluationFramework.getClickthroughRate(persist);
        LOGGER.info("Clickthrough rates for number of queries: {}", clickthroughRates.size());
        openSearchEvaluationFramework.showClickthroughRates(clickthroughRates);

        // Generate and index the implicit judgments.
        final Collection<Judgment> judgments = openSearchEvaluationFramework.getJudgments(rankAggregatedClickThrough, clickthroughRates, persist);
        LOGGER.info("Number of judgments: {}", judgments.size());
        openSearchEvaluationFramework.showJudgments(judgments);

    }

}
