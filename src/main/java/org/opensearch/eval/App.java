/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.engine.OpenSearchEngine;
import org.opensearch.eval.engine.SearchEngine;
import org.opensearch.eval.judgments.clickmodel.ClickModel;
import org.opensearch.eval.judgments.clickmodel.JudgmentParameters;
import org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel;
import org.opensearch.eval.judgments.clickmodel.coec.CoecClickModelParameters;
import org.opensearch.eval.runners.OpenSearchQuerySetRunner;
import org.opensearch.eval.runners.QuerySetRunResult;
import org.opensearch.eval.model.data.querysets.QuerySetRunParameters;
import org.opensearch.eval.samplers.AllQueriesQuerySampler;
import org.opensearch.eval.samplers.AllQueriesQuerySamplerParameters;
import org.opensearch.eval.samplers.ProbabilityProportionalToSizeQuerySampler;
import org.opensearch.eval.samplers.ProbabilityProportionalToSizeSamplerParameters;
import org.opensearch.eval.samplers.RandomQuerySampler;
import org.opensearch.eval.samplers.RandomQuerySamplerParameters;
import org.opensearch.eval.samplers.TopNQuerySampler;
import org.opensearch.eval.samplers.TopNQuerySamplerParameters;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class);

    public static void main(String[] args) throws Exception {

        final String title = """
                 #####                                        ######                                                             #     #                                                        \s
                #     # ######   ##   #####   ####  #    #    #     # ###### #      ###### #    #   ##   #    #  ####  ######    #  #  #  ####  #####  #    # #####  ###### #    #  ####  #    #\s
                #       #       #  #  #    # #    # #    #    #     # #      #      #      #    #  #  #  ##   # #    # #         #  #  # #    # #    # #   #  #    # #      ##   # #    # #    #\s
                 #####  #####  #    # #    # #      ######    ######  #####  #      #####  #    # #    # # #  # #      #####     #  #  # #    # #    # ####   #####  #####  # #  # #      ######\s
                      # #      ###### #####  #      #    #    #   #   #      #      #      #    # ###### #  # # #      #         #  #  # #    # #####  #  #   #    # #      #  # # #      #    #\s
                #     # #      #    # #   #  #    # #    #    #    #  #      #      #       #  #  #    # #   ## #    # #         #  #  # #    # #   #  #   #  #    # #      #   ## #    # #    #\s
                 #####  ###### #    # #    #  ####  #    #    #     # ###### ###### ######   ##   #    # #    #  ####  ######     ## ##   ####  #    # #    # #####  ###### #    #  ####  #    #\s
                                                                                                                                                                                                \s""";

        System.out.println(title);
        System.out.println("Search Quality Evaluation Framework\n");


        final Gson gson = new Gson();

        final Options options = new Options();
        options.addOption("c", "create-click-model", true, "create a click model");
        options.addOption("s", "create-query-set", true, "create a query set using sampling");
        options.addOption("r", "run-query-set", true, "run a query set");
        options.addOption("o", "opensearch", true, "OpenSearch URL, e.g. http://localhost:9200");

        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, args);

        final URI uri;
        if(cmd.hasOption("o")) {
            uri = URI.create(cmd.getOptionValue("o"));
        } else {
            System.out.println("No OpenSearch host given so defaulting to http://localhost:9200");
            uri = URI.create("http://localhost:9200");
        }

        // OpenSearch is currently the only supported search engine.
        final SearchEngine searchEngine = new OpenSearchEngine(uri);

        if(cmd.hasOption("c")) {

            final String querySetOptionsFile = cmd.getOptionValue("c");
            final File file = new File(querySetOptionsFile);

            if(file.exists()) {

                final JudgmentParameters judgmentParameters = gson.fromJson(Files.readString(file.toPath(), StandardCharsets.UTF_8), JudgmentParameters.class);

                // Create the judgments index if it does not already exist.
                searchEngine.createIndex(Constants.JUDGMENTS_INDEX_NAME, Constants.JUDGMENTS_INDEX_MAPPING);

                if (CoecClickModel.CLICK_MODEL_NAME.equalsIgnoreCase(judgmentParameters.getJudgmentSetGenerator())) {

                    System.out.println("Creating click model...");

                    final ClickModel cm = new CoecClickModel(searchEngine, new CoecClickModelParameters(10, judgmentParameters));
                    final String judgmentSetId = cm.calculateJudgments();

                    System.out.println("Created judgment set: " + judgmentSetId);

                } else {
                    System.err.println("Invalid click model type. Valid models are 'coec'.");
                }

            } else {
                System.err.println("The judgments parameters file does not exist.");
            }

        } else if (cmd.hasOption("r")) {

            System.out.println("Running query set...");

            final String querySetOptionsFile = cmd.getOptionValue("r");
            final File file = new File(querySetOptionsFile);

            if(file.exists()) {

                final QuerySetRunParameters querySetRunParameters = gson.fromJson(Files.readString(file.toPath(), StandardCharsets.UTF_8), QuerySetRunParameters.class);

                final OpenSearchQuerySetRunner openSearchQuerySetRunner = new OpenSearchQuerySetRunner(searchEngine);
                final QuerySetRunResult querySetRunResult = openSearchQuerySetRunner.run(querySetRunParameters);
                final long indexedCount = searchEngine.indexQueryRunResult(querySetRunResult);

                System.out.println("Indexed " + indexedCount + " query run results.");

            } else {
                System.err.println("The query set run parameters file does not exist.");
            }

        } else if (cmd.hasOption("s")) {

            final String samplerOptionsFile = cmd.getOptionValue("s");
            final File file = new File(samplerOptionsFile);

            if(file.exists()) {

                // Create the query set index if it does not already exist.
                searchEngine.createIndex(Constants.QUERY_SETS_INDEX_NAME, Constants.QUERY_SETS_INDEX_MAPPING);

                final String jsonString = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                final JsonElement jsonElement = JsonParser.parseString(jsonString);
                final JsonObject jsonObject = jsonElement.getAsJsonObject();
                final String samplerType = jsonObject.get("sampler").getAsString();
                String querySetId = null;

                if(AllQueriesQuerySampler.NAME.equalsIgnoreCase(samplerType)) {

                    final AllQueriesQuerySamplerParameters parameters = gson.fromJson(jsonString, AllQueriesQuerySamplerParameters.class);
                    final AllQueriesQuerySampler sampler = new AllQueriesQuerySampler(searchEngine, parameters);

                    // TODO: Allow for selecting the queries by date.
                    final Map<String, Long> querySet = sampler.sample();
                    if(!querySet.isEmpty()) {
                        querySetId = sampler.indexQuerySet(searchEngine, parameters.getName(), parameters.getDescription(), parameters.getSampling(), querySet);
                    } else {
                        System.err.println("The query set was empty.");
                    }

                } else if(ProbabilityProportionalToSizeQuerySampler.NAME.equalsIgnoreCase(samplerType)) {

                    final ProbabilityProportionalToSizeSamplerParameters parameters = gson.fromJson(jsonString, ProbabilityProportionalToSizeSamplerParameters.class);
                    final ProbabilityProportionalToSizeQuerySampler sampler = new ProbabilityProportionalToSizeQuerySampler(searchEngine, parameters);

                    // TODO: Allow for selecting the queries by date.
                    final Map<String, Long> querySet = sampler.sample();
                    if(!querySet.isEmpty()) {
                        querySetId = sampler.indexQuerySet(searchEngine, parameters.getName(), parameters.getDescription(), parameters.getSampling(), querySet);
                    } else {
                        System.err.println("The query set was empty.");
                    }

                } else if(RandomQuerySampler.NAME.equalsIgnoreCase(samplerType)) {

                    final RandomQuerySamplerParameters parameters = gson.fromJson(jsonString, RandomQuerySamplerParameters.class);
                    final RandomQuerySampler sampler = new RandomQuerySampler(searchEngine, parameters);

                    // TODO: Allow for selecting the queries by date.
                    final Map<String, Long> querySet = sampler.sample();
                    if(!querySet.isEmpty()) {
                        querySetId = sampler.indexQuerySet(searchEngine, parameters.getName(), parameters.getDescription(), parameters.getSampling(), querySet);
                    } else {
                        System.err.println("The query set was empty.");
                    }

                } else if(TopNQuerySampler.NAME.equalsIgnoreCase(samplerType)) {

                    final TopNQuerySamplerParameters parameters = gson.fromJson(jsonString, TopNQuerySamplerParameters.class);
                    final TopNQuerySampler sampler = new TopNQuerySampler(searchEngine, parameters);

                    // TODO: Allow for selecting the queries by date.
                    final Map<String, Long> querySet = sampler.sample();
                    if(!querySet.isEmpty()) {
                        querySetId = sampler.indexQuerySet(searchEngine, parameters.getName(), parameters.getDescription(), parameters.getSampling(), querySet);
                    } else {
                        System.err.println("The query set was empty.");
                    }

                } else {

                    System.err.println("Invalid sampler: " + samplerType);
                    throw new IllegalArgumentException("Invalid sampler: " + samplerType);

                }

                if (querySetId != null) {
                    System.out.println("Query set created: " + querySetId);
                } else {
                    System.err.println("No queries found for query set.");
                }

            } else {
                System.err.println("The query set run parameters file does not exist.");
            }


        } else {

            System.err.println("Invalid options.");

        }

    }

}
