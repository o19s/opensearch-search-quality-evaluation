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
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.Client;
import org.opensearch.cluster.service.ClusterService;
import org.opensearch.core.action.ActionListener;
import org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel;
import org.opensearch.eval.judgments.clickmodel.coec.CoecClickModelParameters;
import org.opensearch.jobscheduler.spi.JobExecutionContext;
import org.opensearch.jobscheduler.spi.ScheduledJobParameter;
import org.opensearch.jobscheduler.spi.ScheduledJobRunner;
import org.opensearch.jobscheduler.spi.utils.LockService;
import org.opensearch.threadpool.ThreadPool;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Job runner for scheduled implicit judgments jobs.
 */
public class SearchQualityEvaluationJobRunner implements ScheduledJobRunner {

    private static final Logger LOGGER = LogManager.getLogger(SearchQualityEvaluationJobRunner.class);

    private static SearchQualityEvaluationJobRunner INSTANCE;

    /**
     * Gets a singleton instance of this class.
     * @return A {@link SearchQualityEvaluationJobRunner}.
     */
    public static SearchQualityEvaluationJobRunner getJobRunnerInstance() {

        LOGGER.info("Getting job runner instance");

        if (INSTANCE != null) {
            return INSTANCE;
        }

        synchronized (SearchQualityEvaluationJobRunner.class) {
            if (INSTANCE == null) {
                INSTANCE = new SearchQualityEvaluationJobRunner();
            }
            return INSTANCE;
        }

    }

    private ClusterService clusterService;
    private ThreadPool threadPool;
    private Client client;

    private SearchQualityEvaluationJobRunner() {

    }

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void runJob(final ScheduledJobParameter jobParameter, final JobExecutionContext context) {

        if(!(jobParameter instanceof SearchQualityEvaluationJobParameter)) {
            throw new IllegalStateException(
                "Job parameter is not instance of SampleJobParameter, type: " + jobParameter.getClass().getCanonicalName()
            );
        }

        if(this.clusterService == null) {
            throw new IllegalStateException("ClusterService is not initialized.");
        }

        if(this.threadPool == null) {
            throw new IllegalStateException("ThreadPool is not initialized.");
        }

        final LockService lockService = context.getLockService();

        final Runnable runnable = () -> {

            if (jobParameter.getLockDurationSeconds() != null) {

                lockService.acquireLock(jobParameter, context, ActionListener.wrap(lock -> {

                    if (lock == null) {
                        return;
                    }

                    final SearchQualityEvaluationJobParameter searchQualityEvaluationJobParameter = (SearchQualityEvaluationJobParameter) jobParameter;

                    final long startTime = System.currentTimeMillis();
                    final long judgments;

                    if(StringUtils.equalsIgnoreCase(searchQualityEvaluationJobParameter.getClickModel(), "coec")) {

                        LOGGER.info("Beginning implicit judgment generation using clicks-over-expected-clicks.");
                        final CoecClickModelParameters coecClickModelParameters = new CoecClickModelParameters(true, searchQualityEvaluationJobParameter.getMaxRank());
                        final CoecClickModel coecClickModel = new CoecClickModel(client, coecClickModelParameters);

                        judgments = coecClickModel.calculateJudgments();

                    } else {

                        // Invalid click model.
                        throw new IllegalArgumentException("Invalid click model: " + searchQualityEvaluationJobParameter.getClickModel());

                    }

                    final long elapsedTime = System.currentTimeMillis() - startTime;
                    LOGGER.info("Implicit judgment generation completed in {} ms", elapsedTime);

                    final Map<String, Object> job = new HashMap<>();
                    job.put("name", searchQualityEvaluationJobParameter.getName());
                    job.put("click_model", searchQualityEvaluationJobParameter.getClickModel());
                    job.put("started", startTime);
                    job.put("duration", elapsedTime);
                    job.put("judgments", judgments);
                    job.put("invocation", "scheduled");
                    job.put("max_rank", searchQualityEvaluationJobParameter.getMaxRank());

                    final String judgmentsId = UUID.randomUUID().toString();

                    final IndexRequest indexRequest = new IndexRequest()
                            .index(SearchQualityEvaluationPlugin.COMPLETED_JOBS_INDEX_NAME)
                            .id(judgmentsId)
                            .source(job)
                            .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

                    client.index(indexRequest, new ActionListener<>() {
                        @Override
                        public void onResponse(IndexResponse indexResponse) {
                            LOGGER.info("Successfully indexed implicit judgments {}", judgmentsId);
                        }

                        @Override
                        public void onFailure(Exception ex) {
                            LOGGER.error("Unable to index implicit judgments", ex);
                        }
                    });

                }, exception -> { throw new IllegalStateException("Failed to acquire lock."); }));
            }

        };

        threadPool.generic().submit(runnable);

    }

}
