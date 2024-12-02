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
import org.opensearch.client.Client;
import org.opensearch.cluster.metadata.IndexNameExpressionResolver;
import org.opensearch.cluster.node.DiscoveryNodes;
import org.opensearch.cluster.service.ClusterService;
import org.opensearch.common.settings.ClusterSettings;
import org.opensearch.common.settings.IndexScopedSettings;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.settings.SettingsFilter;
import org.opensearch.core.common.io.stream.NamedWriteableRegistry;
import org.opensearch.core.xcontent.NamedXContentRegistry;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.core.xcontent.XContentParserUtils;
import org.opensearch.env.Environment;
import org.opensearch.env.NodeEnvironment;
import org.opensearch.jobscheduler.spi.JobSchedulerExtension;
import org.opensearch.jobscheduler.spi.ScheduledJobParser;
import org.opensearch.jobscheduler.spi.ScheduledJobRunner;
import org.opensearch.jobscheduler.spi.schedule.ScheduleParser;
import org.opensearch.plugins.ActionPlugin;
import org.opensearch.plugins.Plugin;
import org.opensearch.repositories.RepositoriesService;
import org.opensearch.rest.RestController;
import org.opensearch.rest.RestHandler;
import org.opensearch.script.ScriptService;
import org.opensearch.threadpool.ThreadPool;
import org.opensearch.watcher.ResourceWatcherService;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Main class for the Search Quality Evaluation plugin.
 */
public class SearchQualityEvaluationPlugin extends Plugin implements ActionPlugin, JobSchedulerExtension {

    private static final Logger LOGGER = LogManager.getLogger(SearchQualityEvaluationPlugin.class);

    /**
     * The name of the UBI index containing the queries. This should not be changed.
     */
    public static final String UBI_QUERIES_INDEX_NAME = "ubi_queries";

    /**
     * The name of the UBI index containing the events. This should not be changed.
     */
    public static final String UBI_EVENTS_INDEX_NAME = "ubi_events";

    /**
     * The name of the index to store the scheduled jobs to create implicit judgments.
     */
    public static final String SCHEDULED_JOBS_INDEX_NAME = "search_quality_eval_scheduled_jobs";

    /**
     * The name of the index to store the completed jobs to create implicit judgments.
     */
    public static final String COMPLETED_JOBS_INDEX_NAME = "search_quality_eval_completed_jobs";

    /**
     * The name of the index that stores the query sets.
     */
    public static final String QUERY_SETS_INDEX_NAME = "search_quality_eval_query_sets";

    /**
     * The name of the index that stores the query set run results.
     */
    public static final String QUERY_SETS_RUN_RESULTS = "search_quality_eval_query_sets_run_results";

    @Override
    public Collection<Object> createComponents(
            final Client client,
            final ClusterService clusterService,
            final ThreadPool threadPool,
            final ResourceWatcherService resourceWatcherService,
            final ScriptService scriptService,
            final NamedXContentRegistry xContentRegistry,
            final Environment environment,
            final NodeEnvironment nodeEnvironment,
            final NamedWriteableRegistry namedWriteableRegistry,
            final IndexNameExpressionResolver indexNameExpressionResolver,
            final Supplier<RepositoriesService> repositoriesServiceSupplier
    ) {

        LOGGER.info("Creating search evaluation framework components");
        final SearchQualityEvaluationJobRunner jobRunner = SearchQualityEvaluationJobRunner.getJobRunnerInstance();
        jobRunner.setClusterService(clusterService);
        jobRunner.setThreadPool(threadPool);
        jobRunner.setClient(client);

        return Collections.emptyList();

    }

    @Override
    public String getJobType() {
        return "scheduler_search_quality_eval";
    }

    @Override
    public String getJobIndex() {
        LOGGER.info("Getting job index name");
        return SCHEDULED_JOBS_INDEX_NAME;
    }

    @Override
    public ScheduledJobRunner getJobRunner() {
        LOGGER.info("Creating job runner");
        return SearchQualityEvaluationJobRunner.getJobRunnerInstance();
    }

    @Override
    public ScheduledJobParser getJobParser() {

        return (parser, id, jobDocVersion) -> {

            final SearchQualityEvaluationJobParameter jobParameter = new SearchQualityEvaluationJobParameter();
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.nextToken(), parser);

            while (!parser.nextToken().equals(XContentParser.Token.END_OBJECT)) {

                final String fieldName = parser.currentName();

                parser.nextToken();

                switch (fieldName) {
                    case SearchQualityEvaluationJobParameter.NAME_FIELD:
                        jobParameter.setJobName(parser.text());
                        break;
                    case SearchQualityEvaluationJobParameter.ENABLED_FILED:
                        jobParameter.setEnabled(parser.booleanValue());
                        break;
                    case SearchQualityEvaluationJobParameter.ENABLED_TIME_FILED:
                        jobParameter.setEnabledTime(parseInstantValue(parser));
                        break;
                    case SearchQualityEvaluationJobParameter.LAST_UPDATE_TIME_FIELD:
                        jobParameter.setLastUpdateTime(parseInstantValue(parser));
                        break;
                    case SearchQualityEvaluationJobParameter.SCHEDULE_FIELD:
                        jobParameter.setSchedule(ScheduleParser.parse(parser));
                        break;
                    case SearchQualityEvaluationJobParameter.LOCK_DURATION_SECONDS:
                        jobParameter.setLockDurationSeconds(parser.longValue());
                        break;
                    case SearchQualityEvaluationJobParameter.JITTER:
                        jobParameter.setJitter(parser.doubleValue());
                        break;
                    case SearchQualityEvaluationJobParameter.CLICK_MODEL:
                        jobParameter.setClickModel(parser.text());
                        break;
                    case SearchQualityEvaluationJobParameter.MAX_RANK:
                        jobParameter.setMaxRank(parser.intValue());
                        break;
                    default:
                        XContentParserUtils.throwUnknownToken(parser.currentToken(), parser.getTokenLocation());
                }

            }

            return jobParameter;

        };

    }

    private Instant parseInstantValue(final XContentParser parser) throws IOException {

        if (XContentParser.Token.VALUE_NULL.equals(parser.currentToken())) {
            return null;
        }

        if (parser.currentToken().isValue()) {
            return Instant.ofEpochMilli(parser.longValue());
        }

        XContentParserUtils.throwUnknownToken(parser.currentToken(), parser.getTokenLocation());
        return null;

    }

    @Override
    public List<RestHandler> getRestHandlers(
        final Settings settings,
        final RestController restController,
        final ClusterSettings clusterSettings,
        final IndexScopedSettings indexScopedSettings,
        final SettingsFilter settingsFilter,
        final IndexNameExpressionResolver indexNameExpressionResolver,
        final Supplier<DiscoveryNodes> nodesInCluster
    ) {
        return Collections.singletonList(new SearchQualityEvaluationRestHandler());
    }

}
