/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.eval.engine.OpenSearchEngine;
import org.opensearch.eval.engine.SearchEngine;
import org.opensearch.eval.judgments.clickmodel.ClickModel;
import org.opensearch.eval.judgments.clickmodel.coec.CoecClickModel;
import org.opensearch.eval.judgments.clickmodel.coec.CoecClickModelParameters;

public class SearchQualityEvaluationFrameworkApp {

    private static final Logger LOGGER = LogManager.getLogger(SearchQualityEvaluationFrameworkApp.class);

    public static void main(String[] args) throws Exception {

        System.out.println("Search Quality Evaluation Framework");

        final Options options = new Options();
        options.addOption("c", false, "create a click model");
        options.addOption("q", true, "run a query set");

        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, args);

        if(cmd.hasOption("c")) {

            //final String clickModel = cmd.getOptionValue("c");
            System.out.println("Creating click model...");

            final SearchEngine searchEngine = new OpenSearchEngine();
            final CoecClickModelParameters coecClickModelParameters = new CoecClickModelParameters(10);

            final ClickModel cm = new CoecClickModel(searchEngine, coecClickModelParameters);
            cm.calculateJudgments();

        } else {

        }

    }

//
//    /**
//     * The placeholder in the query that gets replaced by the query term when running a query set.
//     */
//    public static final String QUERY_PLACEHOLDER = "#$query##";
//
//    @Override
//    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
//
//        // Handle managing query sets.
//        if(QUERYSET_MANAGEMENT_URL.equalsIgnoreCase(request.path())) {
//
//            // Creating a new query set by sampling the UBI queries.
//            if (request.method().equals(RestRequest.Method.POST)) {
//
//                final String name = request.param("name");
//                final String description = request.param("description");
//                final String sampling = request.param("sampling", "pptss");
//                final int querySetSize = Integer.parseInt(request.param("query_set_size", "1000"));
//
//                // Create a query set by finding all the unique user_query terms.
//                if (AllQueriesQuerySampler.NAME.equalsIgnoreCase(sampling)) {
//
//                    // If we are not sampling queries, the query sets should just be directly
//                    // indexed into OpenSearch using the `ubi_queries` index directly.
//
//                    try {
//
//                        final AllQueriesQuerySamplerParameters parameters = new AllQueriesQuerySamplerParameters(name, description, sampling, querySetSize);
//                        final AllQueriesQuerySampler sampler = new AllQueriesQuerySampler(client, parameters);
//
//                        // Sample and index the queries.
//                        final String querySetId = sampler.sample();
//
//                        return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "{\"query_set\": \"" + querySetId + "\"}"));
//
//                    } catch(Exception ex) {
//                        return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, "{\"error\": \"" + ex.getMessage() + "\"}"));
//                    }
//
//
//                // Create a query set by using PPTSS sampling.
//                } else if (ProbabilityProportionalToSizeAbstractQuerySampler.NAME.equalsIgnoreCase(sampling)) {
//
//                    LOGGER.info("Creating query set using PPTSS");
//
//                    final ProbabilityProportionalToSizeParameters parameters = new ProbabilityProportionalToSizeParameters(name, description, sampling, querySetSize);
//                    final ProbabilityProportionalToSizeAbstractQuerySampler sampler = new ProbabilityProportionalToSizeAbstractQuerySampler(client, parameters);
//
//                    try {
//
//                        // Sample and index the queries.
//                        final String querySetId = sampler.sample();
//
//                        return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "{\"query_set\": \"" + querySetId + "\"}"));
//
//                    } catch(Exception ex) {
//                        return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, "{\"error\": \"" + ex.getMessage() + "\"}"));
//                    }
//
//                } else {
//                    // An Invalid sampling method was provided in the request.
//                    return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.BAD_REQUEST, "{\"error\": \"Invalid sampling method: " + sampling + "\"}"));
//                }
//
//            } else {
//                // Invalid HTTP method for this endpoint.
//                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "{\"error\": \"" + request.method() + " is not allowed.\"}"));
//            }
//
//        // Handle running query sets.
//        } else if(QUERYSET_RUN_URL.equalsIgnoreCase(request.path())) {
//
//            final String querySetId = request.param("id");
//            final String judgmentsId = request.param("judgments_id");
//            final String index = request.param("index");
//            final String searchPipeline = request.param("search_pipeline", null);
//            final String idField = request.param("id_field", "_id");
//            final int k = Integer.parseInt(request.param("k", "10"));
//            final double threshold = Double.parseDouble(request.param("threshold", "1.0"));
//
//            if(querySetId == null || querySetId.isEmpty() || judgmentsId == null || judgmentsId.isEmpty() || index == null || index.isEmpty()) {
//                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.BAD_REQUEST, "{\"error\": \"Missing required parameters.\"}"));
//            }
//
//            if(k < 1) {
//                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.BAD_REQUEST, "{\"error\": \"k must be a positive integer.\"}"));
//            }
//
//            if(!request.hasContent()) {
//                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.BAD_REQUEST, "{\"error\": \"Missing query in body.\"}"));
//            }
//
//            // Get the query JSON from the content.
//            final String query = new String(BytesReference.toBytes(request.content()), Charset.defaultCharset());
//
//            // Validate the query has a QUERY_PLACEHOLDER.
//            if(!query.contains(QUERY_PLACEHOLDER)) {
//                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.BAD_REQUEST, "{\"error\": \"Missing query placeholder in query.\"}"));
//            }
//
//            try {
//
//                final OpenSearchQuerySetRunner openSearchQuerySetRunner = new OpenSearchQuerySetRunner(client);
//                final QuerySetRunResult querySetRunResult = openSearchQuerySetRunner.run(querySetId, judgmentsId, index, searchPipeline, idField, query, k, threshold);
//                openSearchQuerySetRunner.save(querySetRunResult);
//
//            } catch (Exception ex) {
//                LOGGER.error("Unable to run query set. Verify query set and judgments exist.", ex);
//                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
//            }
//
//            return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "{\"message\": \"Run initiated for query set " + querySetId + "\"}"));
//
//        // Handle the on-demand creation of implicit judgments.
//        } else if(IMPLICIT_JUDGMENTS_URL.equalsIgnoreCase(request.path())) {
//
//            if (request.method().equals(RestRequest.Method.POST)) {
//
//                //final long startTime = System.currentTimeMillis();
//                final String clickModel = request.param("click_model", "coec");
//                final int maxRank = Integer.parseInt(request.param("max_rank", "20"));
//
//                if (CoecClickModel.CLICK_MODEL_NAME.equalsIgnoreCase(clickModel)) {
//
//                    final CoecClickModelParameters coecClickModelParameters = new CoecClickModelParameters(maxRank);
//                    final CoecClickModel coecClickModel = new CoecClickModel(client, coecClickModelParameters);
//
//                    final String judgmentsId;
//
//                    // TODO: Run this in a separate thread.
//                    try {
//
//                        // Create the judgments index.
//                        createJudgmentsIndex(client);
//
//                        judgmentsId = coecClickModel.calculateJudgments();
//
//                        // judgmentsId will be null if no judgments were created (and indexed).
//                        if(judgmentsId == null) {
//                            // TODO: Is Bad Request the appropriate error? Perhaps Conflict is more appropriate?
//                            return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.BAD_REQUEST, "{\"error\": \"No judgments were created. Check the queries and events data.\"}"));
//                        }
//
////                        final long elapsedTime = System.currentTimeMillis() - startTime;
////
////                        final Map<String, Object> job = new HashMap<>();
////                        job.put("name", "manual_generation");
////                        job.put("click_model", clickModel);
////                        job.put("started", startTime);
////                        job.put("duration", elapsedTime);
////                        job.put("invocation", "on_demand");
////                        job.put("judgments_id", judgmentsId);
////                        job.put("max_rank", maxRank);
////
////                        final String jobId = UUID.randomUUID().toString();
////
////                        final IndexRequest indexRequest = new IndexRequest()
////                                .index(SearchQualityEvaluationPlugin.COMPLETED_JOBS_INDEX_NAME)
////                                .id(jobId)
////                                .source(job)
////                                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
////
////                        client.index(indexRequest, new ActionListener<>() {
////                            @Override
////                            public void onResponse(final IndexResponse indexResponse) {
////                                LOGGER.debug("Click model job completed successfully: {}", jobId);
////                            }
////
////                            @Override
////                            public void onFailure(final Exception ex) {
////                                LOGGER.error("Unable to run job with ID {}", jobId, ex);
////                                throw new RuntimeException("Unable to run job", ex);
////                            }
////                        });
//
//                    } catch (Exception ex) {
//                        throw new RuntimeException("Unable to generate judgments.", ex);
//                    }
//
//                    return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.OK, "{\"judgments_id\": \"" + judgmentsId + "\"}"));
//
//                } else {
//                    return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.BAD_REQUEST, "{\"error\": \"Invalid click model.\"}"));
//                }
//
//            } else {
//                return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "{\"error\": \"" + request.method() + " is not allowed.\"}"));
//            }
//
//        } else {
//            return restChannel -> restChannel.sendResponse(new BytesRestResponse(RestStatus.NOT_FOUND, "{\"error\": \"" + request.path() + " was not found.\"}"));
//        }
//
//    }
//
//    private void createJudgmentsIndex(final NodeClient client) throws Exception {
//
//        // If the judgments index does not exist we need to create it.
//        final IndicesExistsRequest indicesExistsRequest = new IndicesExistsRequest(Constants.JUDGMENTS_INDEX_NAME);
//
//        final IndicesExistsResponse indicesExistsResponse = client.admin().indices().exists(indicesExistsRequest).get();
//
//        if(!indicesExistsResponse.isExists()) {
//
//            // TODO: Read this mapping from a resource file instead.
//            final String mapping = "{\n" +
//                    "                                                  \"properties\": {\n" +
//                    "                                                    \"judgments_id\": { \"type\": \"keyword\" },\n" +
//                    "                                                    \"query_id\": { \"type\": \"keyword\" },\n" +
//                    "                                                    \"query\": { \"type\": \"keyword\" },\n" +
//                    "                                                    \"document_id\": { \"type\": \"keyword\" },\n" +
//                    "                                                    \"judgment\": { \"type\": \"double\" },\n" +
//                    "                                                    \"timestamp\": { \"type\": \"date\", \"format\": \"strict_date_time\" }\n" +
//                    "                                                  }\n" +
//                    "                                              }";
//
//            // Create the judgments index.
//            final CreateIndexRequest createIndexRequest = new CreateIndexRequest(Constants.JUDGMENTS_INDEX_NAME).mapping(mapping);
//
//            // TODO: Don't use .get()
//            client.admin().indices().create(createIndexRequest).get();
//
//        }
//
//    }

}
