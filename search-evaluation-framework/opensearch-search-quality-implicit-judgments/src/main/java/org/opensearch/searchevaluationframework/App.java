package org.opensearch.searchevaluationframework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.searchevaluationframework.model.ClickthroughRate;
import org.opensearch.searchevaluationframework.model.Judgment;

import java.util.Collection;
import java.util.Map;

public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {

        final OpenSearchEvaluationFramework openSearchEvaluationFramework = new OpenSearchEvaluationFramework();

        // Calculate the rank-aggregated click-through.
        final Map<Integer, Double> rankAggregatedClickThrough = openSearchEvaluationFramework.getRankAggregatedClickThrough();

        // Calculate the click-through rate for query/doc pairs.
        final Map<String, Collection<ClickthroughRate>> clickthroughRates = openSearchEvaluationFramework.getClickthroughRate();

        // TODO: Generate the implicit judgments.
        // Format: datetime, query_id, query, document, judgment
        final Collection<Judgment> judgments = openSearchEvaluationFramework.getJudgments(rankAggregatedClickThrough, clickthroughRates);

    }

}
