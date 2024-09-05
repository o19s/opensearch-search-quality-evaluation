package org.opensearch.searchevaluationframework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {

        final OpenSearchEvaluationFramework openSearchEvaluationFramework = new OpenSearchEvaluationFramework();
        final Map<Integer, Double> rankAggregatedClickThrough = openSearchEvaluationFramework.getRankAggregatedClickThrough();

    }

}
