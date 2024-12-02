/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.node.NodeClient;
import org.opensearch.common.xcontent.json.JsonXContent;
import org.opensearch.core.action.ActionListener;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel;
import org.opensearch.eval.judgments.clickmodel.coec.CoecClickModelParameters;
import org.opensearch.eval.runners.OpenSearchQuerySetRunner;
import org.opensearch.eval.runners.QuerySetRunResult;
import org.opensearch.eval.samplers.AllQueriesQuerySampler;
import org.opensearch.eval.samplers.AllQueriesQuerySamplerParameters;
import org.opensearch.eval.samplers.ProbabilityProportionalToSizeAbstractQuerySampler;
import org.opensearch.eval.samplers.ProbabilityProportionalToSizeParameters;
import org.opensearch.jobscheduler.spi.schedule.IntervalSchedule;
import org.opensearch.rest.BaseRestHandler;
import org.opensearch.rest.BytesRestResponse;
import org.opensearch.rest.RestRequest;
import org.opensearch.rest.RestResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if(QUERYSET_MANAGEMENT_URL.equalsIgnoreCase(request.path())) {

            // Creating a new query set by sampling the UBI queries.
            if (request.method().equals(RestRequest.Method.POST)) {

                final String name = request.param("name");
                final String description = request.param("description");
                final String sampling = request.param("sampling", "pptss");
                final int querySetSize = Integer.parseInt(request.param("query_set_size", "1000"));

                // Create a query set by finding all the unique user_query terms.
                if (AllQueriesQuerySampler.NAME.equalsIgnoreCase(sampling)) {

                    // If we are not sampling queries, the query sets should just be directly
                    // indexed into OpenSearch using the `ubi_queries` index directly.

                    try {

                        final AllQueriesQuerySamplerParameters parameters = new AllQueriesQuerySamplerParameters(name, description, sampling, querySetSize);
                        final AllQueriesQuerySampler sampler = new AllQueriesQuerySampler(client, parameters);

                        // Sample and index the queries.
                        final String querySetId = sampler.sample();

                        return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "{\"query_set\": \"" + querySetId + "\"}"));

                    } catch(Exception ex) {
                        return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, "{\"error\": \"" + ex.getMessage() + "\"}"));
                    }


                // Create a query set by using PPTSS sampling.
                } else if (ProbabilityProportionalToSizeAbstractQuerySampler.NAME.equalsIgnoreCase(sampling)) {

                    LOGGER.info("Creating query set using PPTSS");

                    final ProbabilityProportionalToSizeParameters parameters = new ProbabilityProportionalToSizeParameters(name, description, sampling, querySetSize);
                    final ProbabilityProportionalToSizeAbstractQuerySampler sampler = new ProbabilityProportionalToSizeAbstractQuerySampler(client, parameters);

                    try {

                        // Sample and index the queries.
                        final String querySetId = sampler.sample();

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
        } else if(QUERYSET_RUN_URL.equalsIgnoreCase(request.path())) {

            final String querySetId = request.param("id");

            try {

                final OpenSearchQuerySetRunner openSearchQuerySetRunner = new OpenSearchQuerySetRunner(client);
                final QuerySetRunResult querySetRunResult = openSearchQuerySetRunner.run(querySetId);
                openSearchQuerySetRunner.save(querySetRunResult);

            } catch (Exception ex) {
                LOGGER.error("Unable to run query set with ID {}: ", querySetId, ex);
                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
            }

            return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "{\"message\": \"Run initiated for query set " + querySetId + "\"}"));

        // Handle the on-demand creation of implicit judgments.
        } else if(IMPLICIT_JUDGMENTS_URL.equalsIgnoreCase(request.path())) {

            if (request.method().equals(RestRequest.Method.POST)) {

                final long startTime = System.currentTimeMillis();
                final String clickModel = request.param("click_model", "coec");
                final int maxRank = Integer.parseInt(request.param("max_rank", "20"));
                final long judgments;

                if (CoecClickModel.CLICK_MODEL_NAME.equalsIgnoreCase(clickModel)) {

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
        } else if(SCHEDULING_URL.equalsIgnoreCase(request.path())) {

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
                if (request.param("start_time") == null) {
                    startTime = Instant.now();
                } else {
                    startTime = Instant.ofEpochMilli(Long.parseLong(request.param("start_time")));
                }

                // Read the interval.
                final int interval;
                if (request.param("interval") == null) {
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

}
