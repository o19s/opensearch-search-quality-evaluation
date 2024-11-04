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
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.node.NodeClient;
import org.opensearch.common.xcontent.json.JsonXContent;
import org.opensearch.core.action.ActionListener;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel;
import org.opensearch.eval.judgments.clickmodel.coec.CoecClickModelParameters;
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
import java.util.concurrent.ExecutionException;

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


    @Override
    public String getName() {
        return "Search Quality Evaluation Framework";
    }

    @Override
    public List<Route> routes() {
        return List.of(
                new Route(RestRequest.Method.POST, IMPLICIT_JUDGMENTS_URL),
                new Route(RestRequest.Method.POST, SCHEDULING_URL),
                new Route(RestRequest.Method.DELETE, SCHEDULING_URL));
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {

        if(StringUtils.equalsIgnoreCase(request.path(), IMPLICIT_JUDGMENTS_URL)) {

            if (request.method().equals(RestRequest.Method.POST)) {

                final long startTime = System.currentTimeMillis();
                final String clickModel = request.param("click_model");
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

                    return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "Implicit judgment generation initiated."));

                } else {
                    return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.BAD_REQUEST, "Invalid click_model."));
                }

            } else {

                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, request.method() + " is not allowed."));

            }

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

            } else if (request.method().equals(RestRequest.Method.DELETE)) {

                final String id = request.param("id");
                final DeleteRequest deleteRequest = new DeleteRequest().index(SearchQualityEvaluationPlugin.SCHEDULED_JOBS_INDEX_NAME).id(id);

                return restChannel -> {
                    client.delete(deleteRequest, new ActionListener<>() {
                        @Override
                        public void onResponse(final DeleteResponse deleteResponse) {
                            restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "Scheduled job deleted."));
                        }

                        @Override
                        public void onFailure(Exception e) {
                            restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                        }
                    });
                };

            } else {

                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, request.method() + " is not allowed."));

            }

        } else {

            return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.NOT_FOUND, request.path() + " is not found."));

        }

    }

}
