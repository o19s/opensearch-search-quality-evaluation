/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchScrollRequest;
import org.opensearch.client.node.NodeClient;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.eval.SearchQualityEvaluationPlugin;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.Scroll;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of {@link AbstractQuerySampler} that uses PPTSS sampling.
 * See https://opensourceconnections.com/blog/2022/10/13/how-to-succeed-with-explicit-relevance-evaluation-using-probability-proportional-to-size-sampling/
 * for more information on PPTSS.
 */
public class ProbabilityProportionalToSizeAbstractQuerySampler extends AbstractQuerySampler {

    public static final String NAME = "pptss";

    private static final Logger LOGGER = LogManager.getLogger(ProbabilityProportionalToSizeAbstractQuerySampler.class);

    private final NodeClient client;
    private final ProbabilityProportionalToSizeParameters parameters;

    /**
     * Creates a new PPTSS sampler.
     * @param client The OpenSearch {@link NodeClient client}.
     * @param parameters The {@link ProbabilityProportionalToSizeParameters parameters} for the sampling.
     */
    public ProbabilityProportionalToSizeAbstractQuerySampler(final NodeClient client, final ProbabilityProportionalToSizeParameters parameters) {
        this.client = client;
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String sample() throws Exception {

        // TODO: Can this be changed to an aggregation?
        // An aggregation is limited (?) to 10,000 which could miss some queries.

        // Get queries from the UBI queries index.
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(1000);
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(10L));

        final SearchRequest searchRequest = new SearchRequest(SearchQualityEvaluationPlugin.UBI_QUERIES_INDEX_NAME);
        searchRequest.scroll(scroll);
        searchRequest.source(searchSourceBuilder);

        // TODO: Don't use .get()
        SearchResponse searchResponse = client.search(searchRequest).get();

        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        final Collection<String> userQueries = new ArrayList<>();

        while (searchHits != null && searchHits.length > 0) {

            LOGGER.info("search hits size = {}", searchHits.length);

            for(final SearchHit hit : searchHits) {
                final Map<String, Object> fields = hit.getSourceAsMap();
                userQueries.add(fields.get("user_query").toString());
              //  LOGGER.info("user queries count: {} user query: {}", userQueries.size(), fields.get("user_query").toString());
            }

            final SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);

            // TODO: Don't use .get()
            searchResponse = client.searchScroll(scrollRequest).get();

            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();

        }

        // LOGGER.info("User queries found: {}", userQueries);

        final Map<String, Long> weights = new HashMap<>();

        // Increment the weight for each user query.
        for(final String userQuery : userQueries) {
            weights.merge(userQuery, 1L, Long::sum);
        }

        // The total number of queries will be used to normalize the weights.
        final long countOfQueries = userQueries.size();

        // Calculate the normalized weights by dividing by the total number of queries.
        final Map<String, Double> normalizedWeights = new HashMap<>();
        for(final String userQuery : weights.keySet()) {
            normalizedWeights.put(userQuery, weights.get(userQuery) / (double) countOfQueries);
            //LOGGER.info("{}: {}/{} = {}", userQuery, weights.get(userQuery), countOfQueries, normalizedWeights.get(userQuery));
        }

        // Ensure all normalized weights sum to 1.
        final double sumOfNormalizedWeights = normalizedWeights.values().stream().reduce(0.0, Double::sum);
        if(!compare(1.0, sumOfNormalizedWeights)) {
            throw new RuntimeException("Summed normalized weights do not equal 1.0: Actual value: " + sumOfNormalizedWeights);
        } else {
            LOGGER.info("Summed normalized weights sum to {}", sumOfNormalizedWeights);
        }

        final Map<String, Long> querySet = new HashMap<>();
        final Set<Double> randomNumbers = new HashSet<>();

        // Generate random numbers between 0 and 1 for the size of the query set.
        // Do this until our query set has reached the requested maximum size.
        // This may require generating more random numbers than what was requested
        // because removing duplicate user queries will require randomly picking more queries.
        int count = 1;
        while(querySet.size() < parameters.getQuerySetSize() && count < userQueries.size()) {

            // Make a random number not yet used.
            double random;
            do {
                random = Math.random();
            } while (randomNumbers.contains(random));
            randomNumbers.add(random);

            // Find the weight closest to the random weight in the map of deltas.
            double smallestDelta = Integer.MAX_VALUE;
            String closestQuery = null;
            for(final String query : normalizedWeights.keySet()) {
                final double delta = Math.abs(normalizedWeights.get(query) - random);
                if(delta < smallestDelta) {
                    smallestDelta = delta;
                    closestQuery = query;
                }
            }

            querySet.put(closestQuery, weights.get(closestQuery));
            count++;

            // LOGGER.info("Generated random value: {}; Smallest delta = {}; Closest query = {}", random, smallestDelta, closestQuery);

        }

        return indexQuerySet(client, parameters.getName(), parameters.getDescription(), parameters.getSampling(), querySet);

    }

    public static boolean compare(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }

}
