package org.opensearch.searchevaluationframework;

import org.apache.http.HttpHost;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Requests;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.WrapperQueryBuilder;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.searchevaluationframework.model.UbiEvent;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

public class OpenSearchHelper {

    public static final String UBI_EVENTS_INDEX = "ubi_events";
    public static final String UBI_QUERIES_INDEX = "ubi_queries";

    private final RestHighLevelClient client;

    public OpenSearchHelper() {

        final RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
        this.client = new RestHighLevelClient(builder);

    }

    public Collection<UbiEvent> getUbiEvents(int from) throws IOException {

        final SearchHit[] hits = getDocuments(UBI_EVENTS_INDEX, from);
        final Collection<UbiEvent> ubiEvents = new LinkedList<>();

        for (final SearchHit hit : hits) {
            final UbiEvent ubiEvent = new UbiEvent(hit);
            ubiEvents.add(ubiEvent);

        }

        return ubiEvents;

    }

    public SearchHit[] getUbiQueries(int from) throws IOException {
        return getDocuments(UBI_QUERIES_INDEX, from);
    }

    private SearchHit[] getDocuments(final String indexName, final int from) throws IOException {

        String query = "{\"match_all\":{}}";
        final BoolQueryBuilder queryBuilder = new BoolQueryBuilder().must(new WrapperQueryBuilder(query));
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(queryBuilder).from(from).size(10);

        final SearchRequest searchRequest = Requests.searchRequest(indexName).source(searchSourceBuilder);

        final SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse.getHits().getHits();

    }

}
