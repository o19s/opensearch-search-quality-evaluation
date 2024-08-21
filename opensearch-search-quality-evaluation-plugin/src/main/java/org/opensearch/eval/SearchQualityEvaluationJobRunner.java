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
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.Client;
import org.opensearch.cluster.service.ClusterService;
import org.opensearch.common.xcontent.json.JsonXContent;
import org.opensearch.core.action.ActionListener;
import org.opensearch.jobscheduler.spi.JobExecutionContext;
import org.opensearch.jobscheduler.spi.ScheduledJobParameter;
import org.opensearch.jobscheduler.spi.ScheduledJobRunner;
import org.opensearch.jobscheduler.spi.utils.LockService;
import org.opensearch.threadpool.ThreadPool;

public class SearchQualityEvaluationJobRunner implements ScheduledJobRunner {

    private static final Logger LOGGER = LogManager.getLogger(SearchQualityEvaluationJobRunner.class);

//    private static SearchQualityEvaluationJobRunner INSTANCE;
//
//    public static SearchQualityEvaluationJobRunner getJobRunnerInstance() {
//
//        if (INSTANCE != null) {
//            return INSTANCE;
//        }
//
//        synchronized (SearchQualityEvaluationJobRunner.class) {
//            if (INSTANCE == null) {
//                INSTANCE = new SearchQualityEvaluationJobRunner();
//            }
//            return INSTANCE;
//        }
//
//    }

    private final ClusterService clusterService;
    private final ThreadPool threadPool;
    private final Client client;

    public SearchQualityEvaluationJobRunner(ClusterService clusterService, ThreadPool threadPool, Client client) {
        this.clusterService = clusterService;
        this.threadPool = threadPool;
        this.client = client;
    }

//    public void setClusterService(ClusterService clusterService) {
//        this.clusterService = clusterService;
//    }
//
//    public void setThreadPool(ThreadPool threadPool) {
//        this.threadPool = threadPool;
//    }
//
//    public void setClient(Client client) {
//        this.client = client;
//    }

    @Override
    public void runJob(final ScheduledJobParameter jobParameter, final JobExecutionContext context) {

        LOGGER.info("Running custom job! = {}", jobParameter.getName());

        if (!(jobParameter instanceof SearchQualityEvaluationJobParameter)) {
            throw new IllegalStateException(
                "Job parameter is not instance of SampleJobParameter, type: " + jobParameter.getClass().getCanonicalName()
            );
        }

        if (this.clusterService == null) {
            throw new IllegalStateException("ClusterService is not initialized.");
        }

        if (this.threadPool == null) {
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

                    LOGGER.info("Message from inside the job.");

                    final IndexRequest indexRequest = new IndexRequest().index(SearchQualityEvaluationPlugin.SCHEDULED_JOBS_INDEX_NAME)
                            .id("100")
                            .source(searchQualityEvaluationJobParameter.toXContent(JsonXContent.contentBuilder(), null))
                            .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

                    client.index(indexRequest);

                    lockService.release(
                        lock,
                        ActionListener.wrap(released -> LOGGER.info("Released lock for job {}", jobParameter.getName()), exception -> {
                            throw new IllegalStateException("Failed to release lock.");
                        })
                    );

                }, exception -> { throw new IllegalStateException("Failed to acquire lock."); }));
            }

        };

        threadPool.generic().submit(runnable);

    }

}
