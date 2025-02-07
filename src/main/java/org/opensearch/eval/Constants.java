package org.opensearch.eval;

public class Constants {

    /**
     * The name of the index that stores the implicit judgments.
     */
    public static final String JUDGMENTS_INDEX_NAME = "judgments";

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
    public final static String QUERY_SETS_INDEX_NAME = "search_quality_eval_query_sets";

    /**
     * The name of the index that stores the metrics for the dashboard.
     */
    public final static String DASHBOARD_METRICS_INDEX_NAME = "sqe_metrics_sample_data";

    /**
     * The judgments index mapping.
     */
    public final static String JUDGMENTS_INDEX_MAPPING = "{\n" +
            "              \"properties\": {\n" +
            "                \"timestamp\": { \"type\": \"date\", \"format\": \"strict_date_time\" },\n" +
            "                \"judgment_set_id\": { \"type\": \"keyword\" },\n" +
            "                \"query\": { \"type\": \"keyword\" },\n" +
            "                \"query_id\": { \"type\": \"keyword\" },\n" +
            "                \"document\": { \"type\": \"keyword\" },\n" +
            "                \"judgment\": { \"type\": \"float\" }\n" +
            "              }\n" +
            "          }";

    /**
     * The query sets index mapping.
     */
    public final static String QUERY_SETS_INDEX_MAPPING = "{\n" +
            "              \"properties\": {\n" +
            "                \"timestamp\": { \"type\": \"date\", \"format\": \"strict_date_time\" },\n" +
            "                \"description\": { \"type\": \"text\" },\n" +
            "                \"id\": { \"type\": \"keyword\" },\n" +
            "                \"name\": { \"type\": \"keyword\" },\n" +
            "                \"query_set_queries\": { \"type\": \"object\" },\n" +
            "                \"sampling\": { \"type\": \"keyword\" }\n" +
            "              }\n" +
            "          }";

    /**
     * The metrics index mapping.
     */
    public static final String METRICS_MAPPING_INDEX_MAPPING = "{\n" +
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

}
