package org.opensearch.searchevaluationframework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.searchevaluationframework.model.ClickthroughRate;

import java.util.Collection;
import java.util.Map;

public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {

        final OpenSearchEvaluationFramework openSearchEvaluationFramework = new OpenSearchEvaluationFramework();

        // Calculate the rank-aggregated click-through.
        final Map<Integer, Double> rankAggregatedClickThrough = openSearchEvaluationFramework.getRankAggregatedClickThrough();

        // Calculate the click-through rate for query/doc pairs.
        final Collection<ClickthroughRate> clickthroughRates = openSearchEvaluationFramework.getClickthroughRate();

    }

}
