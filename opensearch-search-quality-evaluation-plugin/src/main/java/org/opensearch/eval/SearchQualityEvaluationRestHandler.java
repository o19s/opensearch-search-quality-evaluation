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
import org.opensearch.jobscheduler.spi.schedule.IntervalSchedule;
import org.opensearch.rest.BaseRestHandler;
import org.opensearch.rest.BytesRestResponse;
import org.opensearch.rest.RestRequest;
import org.opensearch.rest.RestResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class SearchQualityEvaluationRestHandler extends BaseRestHandler {

    private static final Logger LOGGER = LogManager.getLogger(SearchQualityEvaluationRestHandler.class);

    public static final String WATCH_INDEX_URI = "/_plugins/search_quality_eval/watch";

    @Override
    public String getName() {
        return "Search Quality Evaluation";
    }

    @Override
    public List<Route> routes() {
        return List.of(
                new Route(RestRequest.Method.POST, WATCH_INDEX_URI),
                new Route(RestRequest.Method.DELETE, WATCH_INDEX_URI));
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {

        if (request.method().equals(RestRequest.Method.POST)) {

            // compose SampleJobParameter object from request
            final String id = request.param("id");
            final String indexName = request.param("index");
            final String jobName = request.param("job_name");
            final String interval = request.param("interval");
            final String lockDurationSecondsString = request.param("lock_duration_seconds", "30");
            final Long lockDurationSeconds = lockDurationSecondsString != null ? Long.parseLong(lockDurationSecondsString) : null;
            final String jitterString = request.param("jitter");
            final Double jitter = jitterString != null ? Double.parseDouble(jitterString) : null;

            if (id == null || indexName == null) {
                throw new IllegalArgumentException("Must specify id and index parameter!");
            }

            final SearchQualityEvaluationJobParameter jobParameter = new SearchQualityEvaluationJobParameter(
                jobName,
                indexName,
                new IntervalSchedule(Instant.now(), Integer.parseInt(interval), ChronoUnit.MINUTES),
                lockDurationSeconds,
                jitter
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

            // delete job parameter doc from index
            final String id = request.param("id");
            final DeleteRequest deleteRequest = new DeleteRequest().index(SearchQualityEvaluationPlugin.SCHEDULED_JOBS_INDEX_NAME).id(id);

            return restChannel -> {
                client.delete(deleteRequest, new ActionListener<DeleteResponse>() {
                    @Override
                    public void onResponse(final DeleteResponse deleteResponse) {
                        restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "Job deleted."));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                    }
                });
            };
        } else {
            return restChannel -> {
                restChannel.sendResponse(new BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, request.method() + " is not allowed."));
            };
        }

    }

}
