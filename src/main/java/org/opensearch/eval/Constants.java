/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
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
     * The name of the COEC rank-aggregated click through index.
     */
    public static final String COEC_RANK_AGGREGATED_CTR_INDEX_NAME = "srw_coec_rank_aggregated_ctr";

    /**
     * The COEC rank-aggregated index mapping.
     */
    public final static String COEC_RANK_AGGREGATED_CTR_INDEX_MAPPING = "{\n" +
            "              \"properties\": {\n" +
            "                \"position\": { \"type\": \"keyword\" },\n" +
            "                \"ctr\": { \"type\": \"keyword\" }\n" +
            "              }\n" +
            "          }";

    /**
     * The name of the COEC clickthrough index.
     */
    public static final String COEC_CTR_INDEX_NAME = "srw_coec_ctr";

    /**
     * The COEC clickthrough index mapping.
     */
    public final static String COEC_CTR_INDEX_MAPPING = "{\n" +
            "              \"properties\": {\n" +
            "                \"user_query\": { \"type\": \"keyword\" },\n" +
            "                \"clicks\": { \"type\": \"keyword\" },\n" +
            "                \"events\": { \"type\": \"keyword\" },\n" +
            "                \"ctr\": { \"type\": \"keyword\" },\n" +
            "                \"object_id\": { \"type\": \"keyword\" }\n" +
            "              }\n" +
            "          }";

    /**
     * The name of the index that stores the judgments.
     */
    public static final String JUDGMENTS_INDEX_NAME = "srw_judgments";

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
            "                \"user_query\": { \"type\": \"keyword\" },\n" +
            "                \"query_id\": { \"type\": \"keyword\" },\n" +
            "                \"document\": { \"type\": \"keyword\" },\n" +
            "                \"judgment\": { \"type\": \"float\" }\n" +
            "              }\n" +
            "          }";

    /**
     * The name of the index that stores the query sets.
     */
    public final static String QUERY_SETS_INDEX_NAME = "srw_query_sets";

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
     * THe name of the index that stores the results from each query in a query set run.
     */
    public static final String QUERY_RESULTS_INDEX_NAME = "srw_query_results";

    /**
     * The query sets index mapping.
     */
    public final static String QUERY_RESULTS_MAPPING = "{\n" +
            "              \"properties\": {\n" +
            "                \"id\": { \"type\": \"keyword\" },\n" +
            "                \"timestamp\": { \"type\": \"date\", \"format\": \"strict_date_time\" },\n" +
            "                \"query_set_id\": { \"type\": \"keyword\" },\n" +
            "                \"user_query\": { \"type\": \"keyword\" },\n" +
            "                \"result_set\": { \"type\": \"keyword\" },\n" +
            "                \"number_of_results\": { \"type\": \"integer\" },\n" +
            "                \"evaluation_id\": { \"type\": \"keyword\" }\n" +
            "              }\n" +
            "          }";

    /**
     * The name of the index that stores the metrics for the dashboard.
     */
    public final static String METRICS_INDEX_NAME = "srw_metrics";

    /**
     * The query results index mapping.
     */
    public static final String METRICS_INDEX_MAPPING = "{\n" +
            "              \"properties\": {\n" +
            "                \"timestamp\": { \"type\": \"date\", \"format\": \"strict_date_time\" },\n" +
            "                \"search_config\": { \"type\": \"keyword\" },\n" +
            "                \"query_set_id\": { \"type\": \"keyword\" },\n" +
            "                \"user_query\": { \"type\": \"keyword\" },\n" +
            "                \"metric\": { \"type\": \"keyword\" },\n" +
            "                \"value\": { \"type\": \"double\" },\n" +
            "                \"application\": { \"type\": \"keyword\" },\n" +
            "                \"evaluation_id\": { \"type\": \"keyword\" },\n" +
            "                \"frogs_percent\": { \"type\": \"double\" }\n" +
            "              }\n" +
            "          }";

    private Constants() {
        // Utility class.
    }

}
