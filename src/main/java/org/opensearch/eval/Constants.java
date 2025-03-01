package org.opensearch.eval;

public class Constants {

    /**
     * The name of the UBI index containing the queries. This should not be changed.
     */
    public static final String UBI_QUERIES_INDEX_NAME = "ubi_queries";

    /**
     * The name of the UBI index containing the events. This should not be changed.
     */
    public static final String UBI_EVENTS_INDEX_NAME = "ubi_events";

    /**
     * The name of the index that stores the judgments.
     */
    public static final String JUDGMENTS_INDEX_NAME = "srw_judgments";

    /**
     * The name of the index that stores the query sets.
     */
    public final static String QUERY_SETS_INDEX_NAME = "srw_query_sets";

    /**
     * THe name of the index that stores the results from each query in a query set run.
     */
    public static final String QUERY_RESULTS_INDEX_NAME = "srw_query_results";

    /**
     * The name of the index that stores the metrics for the dashboard.
     */
    public final static String METRICS_INDEX_NAME = "srw_metrics";

    /**
     * The judgments index mapping.
     */
    public final static String JUDGMENTS_INDEX_MAPPING = "{\n" +
            "              \"properties\": {\n" +
            "                \"timestamp\": { \"type\": \"date\", \"format\": \"strict_date_time\" },\n" +
            "                \"judgment_set_id\": { \"type\": \"keyword\" },\n" +
            "                \"judgment_set_type\": { \"type\": \"keyword\" },\n" +
            "                \"judgment_set_generator\": { \"type\": \"keyword\" },\n" +
            "                \"judgment_set_name\": { \"type\": \"keyword\" },\n" +
            "                \"judgment_set_description\": { \"type\": \"keyword\" },\n" +
            "                \"judgment_set_parameters\": { \"type\": \"object\" },\n" +
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
     * The query sets index mapping.
     */
    public final static String QUERY_RESULTS_MAPPING = "{\n" +
            "              \"properties\": {\n" +
            "                \"id\": { \"type\": \"keyword\" },\n" +
            "                \"query_set_id\": { \"type\": \"keyword\" },\n" +
            "                \"result_set\": { \"type\": \"object\" },\n" +
            "              }\n" +
            "          }";

    /**
     * The query results index mapping.
     */
    public static final String METRICS_INDEX_MAPPING = "{\n" +
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
