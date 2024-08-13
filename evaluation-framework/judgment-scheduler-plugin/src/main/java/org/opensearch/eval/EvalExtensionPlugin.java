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
 * Sample JobScheduler extension plugin.
 *
 * It use ".scheduler_sample_extension" index to manage its scheduled jobs, and exposes a REST API
 * endpoint using {@link EvalExtensionRestHandler}.
 *
 */
public class EvalExtensionPlugin extends Plugin implements ActionPlugin, JobSchedulerExtension {
    private static final Logger log = LogManager.getLogger(EvalExtensionPlugin.class);

    public static final String JOB_INDEX_NAME = ".scheduler_sample_extension";

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
        final EvalJobRunner jobRunner = EvalJobRunner.getJobRunnerInstance();
        jobRunner.setClusterService(clusterService);
        jobRunner.setThreadPool(threadPool);
        jobRunner.setClient(client);

        return Collections.emptyList();
    }

    @Override
    public String getJobType() {
        return "scheduler_sample_extension";
    }

    @Override
    public String getJobIndex() {
        return JOB_INDEX_NAME;
    }

    @Override
    public ScheduledJobRunner getJobRunner() {
        return EvalJobRunner.getJobRunnerInstance();
    }

    @Override
    public ScheduledJobParser getJobParser() {

        return (parser, id, jobDocVersion) -> {
            EvalJobParameter jobParameter = new EvalJobParameter();
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.nextToken(), parser);

            while (!parser.nextToken().equals(XContentParser.Token.END_OBJECT)) {
                String fieldName = parser.currentName();
                parser.nextToken();
                switch (fieldName) {
                    case EvalJobParameter.NAME_FIELD:
                        jobParameter.setJobName(parser.text());
                        break;
                    case EvalJobParameter.ENABLED_FILED:
                        jobParameter.setEnabled(parser.booleanValue());
                        break;
                    case EvalJobParameter.ENABLED_TIME_FILED:
                        jobParameter.setEnabledTime(parseInstantValue(parser));
                        break;
                    case EvalJobParameter.LAST_UPDATE_TIME_FIELD:
                        jobParameter.setLastUpdateTime(parseInstantValue(parser));
                        break;
                    case EvalJobParameter.SCHEDULE_FIELD:
                        jobParameter.setSchedule(ScheduleParser.parse(parser));
                        break;
                    case EvalJobParameter.INDEX_NAME_FIELD:
                        jobParameter.setIndexToWatch(parser.text());
                        break;
                    case EvalJobParameter.LOCK_DURATION_SECONDS:
                        jobParameter.setLockDurationSeconds(parser.longValue());
                        break;
                    case EvalJobParameter.JITTER:
                        jobParameter.setJitter(parser.doubleValue());
                        break;
                    default:
                        XContentParserUtils.throwUnknownToken(parser.currentToken(), parser.getTokenLocation());
                }
            }
            return jobParameter;
        };

    }

    private Instant parseInstantValue(XContentParser parser) throws IOException {

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
        return Collections.singletonList(new EvalExtensionRestHandler());
    }

}
