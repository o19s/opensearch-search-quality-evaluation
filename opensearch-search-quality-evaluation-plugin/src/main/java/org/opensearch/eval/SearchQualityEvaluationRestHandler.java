/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.node.NodeClient;
import org.opensearch.common.xcontent.json.JsonXContent;
import org.opensearch.core.action.ActionListener;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel;
import org.opensearch.eval.judgments.clickmodel.coec.CoecClickModelParameters;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.jobscheduler.spi.schedule.IntervalSchedule;
import org.opensearch.rest.BaseRestHandler;
import org.opensearch.rest.BytesRestResponse;
import org.opensearch.rest.RestRequest;
import org.opensearch.rest.RestResponse;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SearchQualityEvaluationRestHandler extends BaseRestHandler {

    private static final Logger LOGGER = LogManager.getLogger(SearchQualityEvaluationRestHandler.class);

    /**
     * URL for the implicit judgment scheduling.
     */
    public static final String SCHEDULING_URL = "/_plugins/search_quality_eval/schedule";

    /**
     * URL for on-demand implicit judgment generation.
     */
    public static final String IMPLICIT_JUDGMENTS_URL = "/_plugins/search_quality_eval/judgments";

    /**
     * URL for managing query sets.
     */
    public static final String QUERYSET_MANAGEMENT_URL = "/_plugins/search_quality_eval/queryset";

    /**
     * URL for initiating query sets to run on-demand.
     */
    public static final String QUERYSET_RUN_URL = "/_plugins/search_quality_eval/run";

    @Override
    public String getName() {
        return "Search Quality Evaluation Framework";
    }

    @Override
    public List<Route> routes() {
        return List.of(
                new Route(RestRequest.Method.POST, IMPLICIT_JUDGMENTS_URL),
                new Route(RestRequest.Method.POST, SCHEDULING_URL),
                new Route(RestRequest.Method.DELETE, SCHEDULING_URL),
                new Route(RestRequest.Method.POST, QUERYSET_MANAGEMENT_URL),
                new Route(RestRequest.Method.POST, QUERYSET_RUN_URL));
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {

        // Handle managing query sets.
        if(StringUtils.equalsIgnoreCase(request.path(), QUERYSET_MANAGEMENT_URL)) {

            // Creating a new query set by sampling the UBI queries.
            if (request.method().equals(RestRequest.Method.POST)) {

                final String name = request.param("name");
                final String description = request.param("description");
                final String sampling = request.param("sampling", "pptss");
                final int maxQueries = Integer.parseInt(request.param("max_queries", "1000"));

                // Create a query set by finding all the unique user_query terms.
                if (StringUtils.equalsIgnoreCase(sampling, "none")) {

                    // If we are not sampling queries, the query sets should just be directly
                    // indexed into OpenSearch using the `ubu_queries` index directly.

                    try {

                        // Get queries from the UBI queries index.
                        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
                        searchSourceBuilder.from(0);
                        searchSourceBuilder.size(maxQueries);

                        final SearchRequest searchRequest = new SearchRequest(SearchQualityEvaluationPlugin.UBI_QUERIES_INDEX_NAME);
                        searchRequest.source(searchSourceBuilder);

                        final SearchResponse searchResponse = client.search(searchRequest).get();

                        LOGGER.info("Found {} user queries from the ubi_queries index.", searchResponse.getHits().getTotalHits().toString());

                        final Set<String> queries = new HashSet<>();
                        for(final SearchHit hit : searchResponse.getHits().getHits()) {
                            final Map<String, Object> fields = hit.getSourceAsMap();
                            queries.add(fields.get("user_query").toString());
                        }

                        LOGGER.info("Found {} user queries from the ubi_queries index.", queries.size());

                        // Create the query set and return its ID.
                        final String querySetId = indexQuerySet(client, name, description, sampling, queries);
                        return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "{\"query_set\": \"" + querySetId + "\"}"));

                    } catch(Exception ex) {
                        return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, "{\"error\": \"" + ex.getMessage() + "\"}"));
                    }


                // Create a query set by using PPTSS sampling.
                } else if (StringUtils.equalsIgnoreCase(sampling, "pptss")) {

                    // TODO: Use the PPTSS sampling method - https://opensourceconnections.com/blog/2022/10/13/how-to-succeed-with-explicit-relevance-evaluation-using-probability-proportional-to-size-sampling/
                    final Collection<String> queries = List.of("computer", "desk", "table", "battery");

                    try {

                        // Create the query set and return its ID.
                        final String querySetId = indexQuerySet(client, name, description, sampling, queries);
                        return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "{\"query_set\": \"" + querySetId + "\"}"));

                    } catch(Exception ex) {
                        return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, "{\"error\": \"" + ex.getMessage() + "\"}"));
                    }

                } else {
                    // An Invalid sampling method was provided in the request.
                    return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.BAD_REQUEST, "{\"error\": \"Invalid sampling method: " + sampling + "\"}"));
                }

            } else {
                // Invalid HTTP method for this endpoint.
                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "{\"error\": \"" + request.method() + " is not allowed.\"}"));
            }

        // Handle running query sets.
        } else if(StringUtils.equalsIgnoreCase(request.path(), QUERYSET_RUN_URL)) {

            final String id = request.param("id");

            // Get the query set.
            final SearchSourceBuilder getQuerySetSearchSourceBuilder = new SearchSourceBuilder();
            getQuerySetSearchSourceBuilder.query(QueryBuilders.matchQuery("_id", id));

            final SearchRequest getQuerySetSearchRequest = new SearchRequest(SearchQualityEvaluationPlugin.QUERY_SETS_INDEX_NAME);
            getQuerySetSearchRequest.source(getQuerySetSearchSourceBuilder);

            try {

                final SearchResponse searchResponse = client.search(getQuerySetSearchRequest).get();

                // The queries from the query set that will be run.
                final Collection<String> queries = (Collection<String>) searchResponse.getHits().getAt(0).getSourceAsMap().get("queries");

                // TODO: Initiate the running of the query set.
                for(final String query : queries) {

                    // TODO: What should this query be?
                    final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                    searchSourceBuilder.query(QueryBuilders.matchQuery("_id", id));

                }

            } catch (Exception ex) {
                LOGGER.error("Unable to retrieve query set with ID {}", id);
                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
            }

            return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "{\"message\": \"Query set " + id + " run initiated.\"}"));

        // Handle the on-demand creation of implicit judgments.
        } else if(StringUtils.equalsIgnoreCase(request.path(), IMPLICIT_JUDGMENTS_URL)) {

            if (request.method().equals(RestRequest.Method.POST)) {

                final long startTime = System.currentTimeMillis();
                final String clickModel = request.param("click_model", "coec");
                final int maxRank = Integer.parseInt(request.param("max_rank", "20"));
                final long judgments;

                if (StringUtils.equalsIgnoreCase(clickModel, "coec")) {

                    final CoecClickModelParameters coecClickModelParameters = new CoecClickModelParameters(true, maxRank);
                    final CoecClickModel coecClickModel = new CoecClickModel(client, coecClickModelParameters);

                    // TODO: Run this in a separate thread.
                    try {
                        judgments = coecClickModel.calculateJudgments();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    final long elapsedTime = System.currentTimeMillis() - startTime;

                    final Map<String, Object> job = new HashMap<>();
                    job.put("name", "manual_generation");
                    job.put("click_model", clickModel);
                    job.put("started", startTime);
                    job.put("duration", elapsedTime);
                    job.put("judgments", judgments);
                    job.put("invocation", "on_demand");
                    job.put("max_rank", maxRank);

                    final IndexRequest indexRequest = new IndexRequest().index(SearchQualityEvaluationPlugin.COMPLETED_JOBS_INDEX_NAME)
                            .id(UUID.randomUUID().toString()).source(job).setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

                    try {
                        client.index(indexRequest).get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "{\"message\": \"Implicit judgment generation initiated.\"}"));

                } else {
                    return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.BAD_REQUEST, "{\"error\": \"Invalid click model.\"}"));
                }

            } else {
                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "{\"error\": \"" + request.method() + " is not allowed.\"}"));
            }

        // Handle the scheduling of creating implicit judgments.
        } else if(StringUtils.equalsIgnoreCase(request.path(), SCHEDULING_URL)) {

            if (request.method().equals(RestRequest.Method.POST)) {

                // Get the job parameters from the request.
                final String id = request.param("id");
                final String jobName = request.param("job_name", UUID.randomUUID().toString());
                final String lockDurationSecondsString = request.param("lock_duration_seconds", "600");
                final Long lockDurationSeconds = lockDurationSecondsString != null ? Long.parseLong(lockDurationSecondsString) : null;
                final String jitterString = request.param("jitter");
                final Double jitter = jitterString != null ? Double.parseDouble(jitterString) : null;
                final String clickModel = request.param("click_model");
                final int maxRank = Integer.parseInt(request.param("max_rank", "20"));

                // Validate the request parameters.
                if (id == null || clickModel == null) {
                    throw new IllegalArgumentException("The id and click_model parameters must be provided.");
                }

                // Read the start_time.
                final Instant startTime;
                if (StringUtils.isEmpty(request.param("start_time"))) {
                    startTime = Instant.now();
                } else {
                    startTime = Instant.ofEpochMilli(Long.parseLong(request.param("start_time")));
                }

                // Read the interval.
                final int interval;
                if (StringUtils.isEmpty(request.param("interval"))) {
                    // Default to every 24 hours.
                    interval = 1440;
                } else {
                    interval = Integer.parseInt(request.param("interval"));
                }

                final SearchQualityEvaluationJobParameter jobParameter = new SearchQualityEvaluationJobParameter(
                        jobName, new IntervalSchedule(startTime, interval, ChronoUnit.MINUTES), lockDurationSeconds,
                        jitter, clickModel, maxRank
                );

                final IndexRequest indexRequest = new IndexRequest().index(SearchQualityEvaluationPlugin.SCHEDULED_JOBS_INDEX_NAME)
                        .id(id)
                        .source(jobParameter.toXContent(JsonXContent.contentBuilder(), null))
                        .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

                return restChannel -> {

                    // index the job parameter
                    client.index(indexRequest, new ActionListener<>() {

                        @Override
                        public void onResponse(final IndexResponse indexResponse) {

                            try {

                                final RestResponse restResponse = new BytesRestResponse(
                                        RestStatus.OK,
                                        indexResponse.toXContent(JsonXContent.contentBuilder(), null)
                                );
                                LOGGER.info("Created implicit judgments schedule for click-model {}: Job name {}, running every {} minutes starting {}", clickModel, jobName, interval, startTime);

                                restChannel.sendResponse(restResponse);

                            } catch (IOException e) {
                                restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                            }

                        }

                        @Override
                        public void onFailure(Exception e) {
                            restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                        }
                    });

                };

            // Delete a scheduled job to make implicit judgments.
            } else if (request.method().equals(RestRequest.Method.DELETE)) {

                final String id = request.param("id");
                final DeleteRequest deleteRequest = new DeleteRequest().index(SearchQualityEvaluationPlugin.SCHEDULED_JOBS_INDEX_NAME).id(id);

                return restChannel -> client.delete(deleteRequest, new ActionListener<>() {
                    @Override
                    public void onResponse(final DeleteResponse deleteResponse) {
                        restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "{\"message\": \"Scheduled job deleted.\"}"));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                    }
                });

            } else {
                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "{\"error\": \"" + request.method() + " is not allowed.\"}"));
            }

        } else {

            return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.NOT_FOUND, "{\"error\": \"" + request.path() + " was not found.\"}"));

        }

    }

    /**
     * Index the query set.
     */
    private String indexQuerySet(final NodeClient client, final String name, final String description, final String sampling, Collection<String> queries) throws Exception {

        final Map<String, Object> querySet = new HashMap<>();
        querySet.put("name", name);
        querySet.put("description", description);
        querySet.put("sampling", sampling);
        querySet.put("queries", queries);
        querySet.put("created_at", Instant.now().toEpochMilli());

        final String querySetId = UUID.randomUUID().toString();

        final IndexRequest indexRequest = new IndexRequest().index(SearchQualityEvaluationPlugin.QUERY_SETS_INDEX_NAME)
                .id(querySetId)
                .source(querySet)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        client.index(indexRequest).get();

        return querySetId;

    }

}
