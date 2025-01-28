package org.opensearch.eval;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Constants {

    private static final Logger LOGGER = LogManager.getLogger(Constants.class);

    /**
     * The name of the UBI index containing the queries. This should not be changed.
     */
    public static final String UBI_QUERIES_INDEX_NAME = "ubi_queries";

    /**
     * The name of the UBI index containing the events. This should not be changed.
     */
    public static final String UBI_EVENTS_INDEX_NAME = "ubi_events";

    /**
     * The name of the index that stores the query sets.
     */
    public static final String QUERY_SETS_INDEX_NAME = "search_quality_eval_query_sets";

    /**
     * The name of the index that stores the metrics for the dashboard.
     */
    public static final String DASHBOARD_METRICS_INDEX_NAME = "sqe_metrics_sample_data";

    /**
     * The name of the index that stores the implicit judgments.
     */
    public static final String JUDGMENTS_INDEX_NAME = "judgments";

}
