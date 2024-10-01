package org.opensearch.qef;

import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.WrapperQueryBuilder;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.qef.model.ClickthroughRate;
import org.opensearch.qef.model.Judgment;
import org.opensearch.qef.model.ubi.UbiQuery;

import java.io.IOException;
import java.util.*;

import static org.opensearch.qef.OpenSearchEvaluationFramework.*;

public class OpenSearchHelper {

    private final RestHighLevelClient client;

    // Used to cache the query ID->user_query to avoid unnecessary lookups to OpenSearch.
    private static final Map<String, String> userQueryCache = new HashMap<>();

    public OpenSearchHelper(final RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * Gets the user query for a given query ID.
     * @param queryId The query ID.
     * @return The user query.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    public String getUserQuery(final String queryId) throws IOException {

        // If it's in the cache just get it and return it.
        if(userQueryCache.containsKey(queryId)) {
            return userQueryCache.get(queryId);
        }

        // Cache it and return it.
        final UbiQuery ubiQuery = getQueryFromQueryId(queryId);
        userQueryCache.put(queryId, ubiQuery.getUserQuery());

        return ubiQuery.getUserQuery();

    }

    /**
     * Gets the query object for a given query ID.
     * @param queryId The query ID.
     * @return A {@link UbiQuery} object for the given query ID.
     */
    public UbiQuery getQueryFromQueryId(final String queryId) throws IOException {

        final String query = "{\"match\": {\"query_id\": \"" + queryId + "\" }}";
        final WrapperQueryBuilder qb = QueryBuilders.wrapperQuery(query);

        // The query_id should be unique anyway, but we are limiting it to a single result anyway.
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(qb);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);

        final String[] indexes = {INDEX_UBI_QUERIES};

        final SearchRequest searchRequest = new SearchRequest(indexes, searchSourceBuilder);
        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        // Will only be a single result.
        final SearchHit hit = response.getHits().getHits()[0];

        return new UbiQuery(hit);

    }

    public int getCountOfQueriesForUserQueryHavingResultInRankR(final String userQuery, final String objectId, final int rank) throws IOException {

        int countOfTimesShownAtRank = 0;

        final String query = "{\"match\": {\"user_query\": \"" + userQuery + "\" }}";
        final WrapperQueryBuilder qb = QueryBuilders.wrapperQuery(query);

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(qb);

        final String[] indexes = {INDEX_UBI_QUERIES};

        final SearchRequest searchRequest = new SearchRequest(indexes, searchSourceBuilder);
        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        for(final SearchHit searchHit : response.getHits().getHits()) {

            //System.out.println(searchHit.getSourceAsMap().get("query_response_object_ids"));

            final List<String> queryResponseObjectIds = (List<String>) searchHit.getSourceAsMap().get("query_response_object_ids");

            //System.out.println(queryResponseObjectIds.get(rank) + " =?= " + objectId);

            if(queryResponseObjectIds.get(rank).equals(objectId)) {
                countOfTimesShownAtRank++;
            }

        }

        return countOfTimesShownAtRank;

    }

    /**
     * Index the rank-aggregated clickthrough values.
     * @param rankAggregatedClickThrough A map of position to clickthrough values.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    public void indexRankAggregatedClickthrough(final Map<Integer, Double> rankAggregatedClickThrough) throws IOException {

        if(!rankAggregatedClickThrough.isEmpty()) {

            // TODO: Split this into multiple bulk insert requests.

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

    /**
     * Index the clickthrough rates.
     * @param clickthroughRates A map of query IDs to a collection of {@link ClickthroughRate} objects.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    public void indexClickthroughRates(final Map<String, Set<ClickthroughRate>> clickthroughRates) throws IOException {

        if(!clickthroughRates.isEmpty()) {

            // TODO: Split this into multiple bulk insert requests.

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

    /**
     * Index the judgments.
     * @param judgments A collection of {@link Judgment judgments}.
     * @throws IOException Thrown when there is a problem accessing OpenSearch.
     */
    public void indexJudgments(final Collection<Judgment> judgments) throws IOException {

        if(!judgments.isEmpty()) {

            // TODO: Split this into multiple bulk insert requests.

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