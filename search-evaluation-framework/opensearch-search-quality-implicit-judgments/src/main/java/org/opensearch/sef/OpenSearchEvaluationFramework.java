package org.opensearch.sef;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.search.ClearScrollRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchScrollRequest;
import org.opensearch.client.*;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.WrapperQueryBuilder;
import org.opensearch.search.Scroll;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.sef.model.ClickthroughRate;
import org.opensearch.sef.model.Judgment;
import org.opensearch.sef.model.ubi.UbiEvent;

import java.io.IOException;
import java.io.PushbackReader;
import java.util.*;

public class OpenSearchEvaluationFramework {

    // OpenSearch indexes.
    public static final String INDEX_UBI_EVENTS = "ubi_events";
    public static final String INDEX_UBI_QUERIES = "ubi_queries";
    public static final String INDEX_RANK_AGGREGATED_CTR = "rank_aggregated_ctr";
    public static final String INDEX_QUERY_DOC_CTR = "click_through_rates";
    public static final String INDEX_JUDGMENT = "judgments";

    // UBI event names.
    public static final String EVENT_CLICK = "click";

    private final RestHighLevelClient client;
    private final OpenSearchHelper openSearchHelper;

    private static final Logger LOGGER = LogManager.getLogger(OpenSearchEvaluationFramework.class.getName());

    public OpenSearchEvaluationFramework() {

        final RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
        this.client = new RestHighLevelClient(builder);
        this.openSearchHelper = new OpenSearchHelper(client);

    }

    /**
     * Gets the clickthrough rates for each query and its results.
     * @return A map of query_id to the clickthrough rate for each query result.
     * @throws IOException Thrown when a problem accessing OpenSearch.
     */
    public Map<String, Set<ClickthroughRate>> getClickthroughRate(final boolean persist) throws IOException {

        // For each query:
        // - Get each document returned in that query (in the QueryResponse object).
        // - Calculate the click-through rate for the document. (clicks/impressions)

        final String query = "{\"match_all\":{}}";
        final BoolQueryBuilder queryBuilder = new BoolQueryBuilder().must(new WrapperQueryBuilder(query));
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(queryBuilder).size(1000);
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(10L));

        final SearchRequest searchRequest = Requests
                .searchRequest(INDEX_UBI_EVENTS)
                .source(searchSourceBuilder)
                .scroll(scroll);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        final Map<String, Set<ClickthroughRate>> queriesToClickthroughRates = new HashMap<>();

        while (searchHits != null && searchHits.length > 0) {

            for (final SearchHit hit : searchHits) {

                final UbiEvent ubiEvent = new UbiEvent(hit);

                // We need to the hash of the query_id because two users can both search
                // for "computer" and those searches will have different query IDs, but they are the same search.
                final String userQuery = openSearchHelper.getUserQuery(ubiEvent.getQueryId());
                // LOGGER.debug("user_query = {}", userQuery);

                // Get the clicks for this queryId from the map, or an empty list if this is a new query.
                final Set<ClickthroughRate> clickthroughRates = queriesToClickthroughRates.getOrDefault(userQuery, new LinkedHashSet<>());

                // Get the ClickthroughRate object for the object that was interacted with.
                final ClickthroughRate clickthroughRate = clickthroughRates.stream().filter(p -> p.getObjectId().equals(ubiEvent.getObjectId())).findFirst().orElse(new ClickthroughRate(ubiEvent.getObjectId()));

                if (StringUtils.equalsIgnoreCase(ubiEvent.getActionName(), EVENT_CLICK)) {
                    clickthroughRate.logClick();
                } else {
                    clickthroughRate.logEvent();
                }

                clickthroughRates.add(clickthroughRate);
                queriesToClickthroughRates.put(userQuery, clickthroughRates);
                // LOGGER.debug("clickthroughRate = {}", queriesToClickthroughRates.size());

            }

            final SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);

            searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();

            searchHits = searchResponse.getHits().getHits();

        }

        if(persist) {
            openSearchHelper.indexClickthroughRates(queriesToClickthroughRates);
        }

        return queriesToClickthroughRates;

    }

    /**
     * Calculate the rank-aggregated click through from the UBI events.
     * @return A map of positions to clickthrough rates.
     * @throws IOException Thrown when a problem accessing OpenSearch.
     */
    public Map<Integer, Double> getRankAggregatedClickThrough(final boolean persist) throws IOException {

        final Map<Integer, Double> rankAggregatedClickThrough = new HashMap<>();

        final String query = "{\"match_all\":{}}";
        final BoolQueryBuilder queryBuilder = new BoolQueryBuilder().must(new WrapperQueryBuilder(query));
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(queryBuilder).size(1000);
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(10L));

        final SearchRequest searchRequest = Requests
                .searchRequest(INDEX_UBI_EVENTS)
                .source(searchSourceBuilder)
                .scroll(scroll);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        long totalEvents = 0;

        while (searchHits != null && searchHits.length > 0) {

            for (final SearchHit searchHit : searchHits) {

                final UbiEvent ubiEvent = new UbiEvent(searchHit);

                // Increment the number of clicks for the position.
                if (StringUtils.equalsIgnoreCase(ubiEvent.getActionName(), EVENT_CLICK)) {
                    rankAggregatedClickThrough.merge(ubiEvent.getPosition(), 1.0, Double::sum);
                }

            }

            // Sum up the total number of events.
            totalEvents += searchHits.length;

            final SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);

            searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();

            searchHits = searchResponse.getHits().getHits();

        }

        // Now for each position, divide its value by the total number of events.
        // This is the click-through rate.
        for(final Integer i : rankAggregatedClickThrough.keySet()) {
            rankAggregatedClickThrough.put(i, rankAggregatedClickThrough.get(i) / totalEvents);
        }

        final ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

        LOGGER.info("Rank-aggregated click through: {}", rankAggregatedClickThrough);
        LOGGER.info("Number of total events: {}", totalEvents);

        if(persist) {
            openSearchHelper.indexRankAggregatedClickthrough(rankAggregatedClickThrough);
        }

        return rankAggregatedClickThrough;

    }

    public Collection<Judgment> getJudgments(final Map<Integer, Double> rankAggregatedClickThrough,
                                             final Map<String, Set<ClickthroughRate>> clickthroughRates,
                                             final boolean persist) throws IOException {

        // Calculate the COEC.
        // Numerator is the total number of clicks received by a query/result pair.
        // Denominator is the expected clicks (EC) that an average result would receive after being impressed i times at rank r,
        // and CTR is the average CTR for each position in the results page (up to R) computed over all queries and results.

        // Format: datetime, query_id, query, document, judgment
        final Collection<Judgment> judgments = new LinkedList<>();

        // Up to Rank R
        final int rank = 1;

        for(final String queryId : clickthroughRates.keySet()) {

            // The clickthrough rates for this query.
            final Collection<ClickthroughRate> ctrs = clickthroughRates.get(queryId);

            for(final ClickthroughRate ctr : ctrs) {

                /*
                    number of clicks
                    ----------------
                    v * mean_ctr

                    v = number of times shown for query q at rank r
                 */

                // The numerator is the total number of clicks received by a query/result pair.
                final int totalNumberClicksForQueryResult = ctr.getClicks();

                // The denominator is the number of times shown as a result of query q at rank r
                rankAggregatedClickThrough.get(rank);



            }

        }

        if(persist) {
            openSearchHelper.indexJudgments(judgments);
        }

        return judgments;

    }

    public void showClickthroughRates(final Map<String, Set<ClickthroughRate>> clickthroughRates) {

        for(final String userQuery : clickthroughRates.keySet()) {

            LOGGER.info("user_query: {}", userQuery);

            for(final ClickthroughRate clickthroughRate : clickthroughRates.get(userQuery)) {

                LOGGER.info("\t - {}", clickthroughRate.toString());

            }

        }

    }

    public void showRankAggregatedClickThrough(final Map<Integer, Double> rankAggregatedClickThrough) {

        for(final int position : rankAggregatedClickThrough.keySet()) {

            LOGGER.info("Position: {}, # clicks: {}", position, rankAggregatedClickThrough.get(position));

        }

    }

    public void showJudgments(final Collection<Judgment> judgments) {

        for(final Judgment judgment : judgments) {

            LOGGER.info(judgment.toString());

        }

    }

}
