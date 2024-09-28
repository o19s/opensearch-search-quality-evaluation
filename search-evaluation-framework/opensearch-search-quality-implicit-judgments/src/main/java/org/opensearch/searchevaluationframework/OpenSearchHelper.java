package org.opensearch.searchevaluationframework;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.QueryBuilder;
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
import org.opensearch.searchevaluationframework.model.ClickthroughRate;
import org.opensearch.searchevaluationframework.model.Judgment;
import org.opensearch.searchevaluationframework.model.UbiSearch;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.opensearch.searchevaluationframework.OpenSearchEvaluationFramework.*;

public class OpenSearchHelper {

    // Used to cache the query ID->hash to avoid unnecessary lookups to OpenSearch.
    private static final Map<String, String> queryHashCache = new HashMap<>();

    public static String getQueryHash(final RestHighLevelClient client, final String queryId) throws IOException {

        // If it's in the cache just get it and return it.
        if(queryHashCache.containsKey(queryId)) {
            return queryHashCache.get(queryId);
        }

        final UbiSearch ubiSearch = getQueryFromQueryId(client, queryId);
        final String hash = String.valueOf(ubiSearch.hashCode());

        // Cache it and return it.
        queryHashCache.put(queryId, hash);

        return hash;

    }

    /**
     * Gets the query object for a given query ID.
     * @param queryId The query ID.
     * @return A {@link UbiSearch} object for the given query ID.
     */
    public static UbiSearch getQueryFromQueryId(final RestHighLevelClient client, final String queryId) throws IOException {

        final String query = "{\"match\": {\"query_id\": \"" + queryId + "\" }}";
        final WrapperQueryBuilder qb = QueryBuilders.wrapperQuery(query);

        // The query_id should be unique anyway but we are limiting it to a single result anyway.
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(qb);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);

        final String[] indexes = {INDEX_UBI_QUERIES};

        final SearchRequest searchRequest = new SearchRequest(indexes, searchSourceBuilder);
        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        // Will only be a single result.
        final SearchHit hit = response.getHits().getHits()[0];

        return new UbiSearch(hit);

    }

    public static void indexRankAggregatedClickthrough(final RestHighLevelClient client, final Map<Integer, Double> rankAggregatedClickThrough) throws IOException {

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

    public static void indexClickthroughRates(final RestHighLevelClient client, final Map<String, Collection<ClickthroughRate>> clickthroughRates) throws IOException {

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

    public static void indexJudgments(final RestHighLevelClient client, final Collection<Judgment> judgments) throws IOException {

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