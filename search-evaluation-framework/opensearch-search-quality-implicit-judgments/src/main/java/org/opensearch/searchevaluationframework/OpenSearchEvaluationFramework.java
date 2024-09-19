package org.opensearch.searchevaluationframework;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.ClearScrollRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchScrollRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Requests;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.WrapperQueryBuilder;
import org.opensearch.search.Scroll;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.searchevaluationframework.model.ClickthroughRate;
import org.opensearch.searchevaluationframework.model.Judgment;
import org.opensearch.searchevaluationframework.model.UbiEvent;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class OpenSearchEvaluationFramework {

    // OpenSearch indexes.
    public static final String INDEX_UBI_EVENTS = "ubi_events";
    public static final String INDEX_UBI_QUERIES = "ubi_queries";
    public static final String INDEX_RANK_AGGREGATED_CTR = "rank_aggregated_ctr";
    public static final String INDEX_QUERY_DOC_CTR = "click_through_rates";
    public static final String INDEX_JUDGMENT = "judgments";

    public static final String EVENT_CLICK = "click";

    private final RestHighLevelClient client;

    public OpenSearchEvaluationFramework() {

        final RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
        this.client = new RestHighLevelClient(builder);

    }

    public Collection<Judgment> getJudgments(final Map<Integer, Double> rankAggregatedClickThrough, Map<String, Collection<ClickthroughRate>> clickthroughRates) throws IOException {

        final Collection<Judgment> judgments = new LinkedList<>();

        indexJudgments(judgments);

        return judgments;

    }

    public Map<String, Collection<ClickthroughRate>> getClickthroughRate() throws IOException {

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

        final Map<String, Collection<ClickthroughRate>> queriesToClickthroughRates = new HashMap<>();

        while (searchHits != null && searchHits.length > 0) {

            for (final SearchHit hit : searchHits) {

                final UbiEvent ubiEvent = new UbiEvent(hit);

                final Collection<ClickthroughRate> clickthroughRates = queriesToClickthroughRates.getOrDefault(ubiEvent.getQueryId(), new LinkedList<>());
                final ClickthroughRate clickthroughRate = clickthroughRates.stream().filter(p -> p.getObjectId().equals(ubiEvent.getObjectId())).findFirst().orElse(new ClickthroughRate(ubiEvent.getObjectId()));

                if (StringUtils.equalsIgnoreCase(ubiEvent.getActionName(), EVENT_CLICK)) {
                    clickthroughRate.logClick();
                } else {
                    clickthroughRate.logEvent();
                }

            }

            final SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);

            searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();

            searchHits = searchResponse.getHits().getHits();

        }

        indexClickthroughRates(queriesToClickthroughRates);

        return queriesToClickthroughRates;

    }

    /**
     * Calculate the rank-aggregated click through from the UBI events.
     * @return The rank-aggregated click through.
     * @throws IOException Thrown when a problem accessing OpenSearch.
     */
    public Map<Integer, Double> getRankAggregatedClickThrough() throws IOException {

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

            for (final SearchHit hit : searchHits) {

                final UbiEvent ubiEvent = new UbiEvent(hit);

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

        System.out.println("Rank-aggregated click through: " + rankAggregatedClickThrough);
        System.out.println("Number of total events: " + totalEvents);

        indexRankAggregatedClickthrough(rankAggregatedClickThrough);

        return rankAggregatedClickThrough;

    }

    private void indexRankAggregatedClickthrough(final Map<Integer, Double> rankAggregatedClickThrough) throws IOException {

        if(!rankAggregatedClickThrough.isEmpty()) {

            final BulkRequest request = new BulkRequest();

            for (final int position : rankAggregatedClickThrough.keySet()) {

                final Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("position", position);
                jsonMap.put("ctr", rankAggregatedClickThrough.get(position));

                final IndexRequest indexRequest = new IndexRequest(INDEX_RANK_AGGREGATED_CTR).id(UUID.randomUUID().toString()).source(jsonMap);

                request.add(indexRequest);

            }

            client.bulk(request, RequestOptions.DEFAULT);

        }

    }

    private void indexClickthroughRates(final Map<String, Collection<ClickthroughRate>> clickthroughRates) throws IOException {

        if(!clickthroughRates.isEmpty()) {

            final BulkRequest request = new BulkRequest();

            for(final String queryId : clickthroughRates.keySet()) {

                for(final ClickthroughRate clickthroughRate : clickthroughRates.get(queryId)) {

                    final Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put("query_id", queryId);
                    jsonMap.put("clicks", clickthroughRate.getClicks());
                    jsonMap.put("events", clickthroughRate.getEvents());
                    jsonMap.put("ctr", clickthroughRate.getClickthroughRate());

                    final IndexRequest indexRequest = new IndexRequest(INDEX_QUERY_DOC_CTR).id(UUID.randomUUID().toString()).source(jsonMap);

                    request.add(indexRequest);

                }

            }

            client.bulk(request, RequestOptions.DEFAULT);

        }

    }

    private void indexJudgments(final Collection<Judgment> judgments) throws IOException {

        if(!judgments.isEmpty()) {

            final BulkRequest request = new BulkRequest();

            for (final Judgment judgment : judgments) {

                final Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("timestamp", judgment.getTimestamp());
                jsonMap.put("query_id", judgment.getQueryId());
                jsonMap.put("query", judgment.getQuery());
                jsonMap.put("document", judgment.getDocument());
                jsonMap.put("judgment", judgment.getJudgment());

                final IndexRequest indexRequest = new IndexRequest(INDEX_JUDGMENT).id(UUID.randomUUID().toString()).source(jsonMap);

                request.add(indexRequest);

            }

            client.bulk(request, RequestOptions.DEFAULT);

        }

    }

}
