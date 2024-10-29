package org.opensearch.qef.clickmodel.coec;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.qef.engine.opensearch.OpenSearchHelper;
import org.opensearch.qef.model.Judgment;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public class CoecClickModelIT {

    private static final Logger LOGGER = LogManager.getLogger(CoecClickModelIT.class.getName());

    @Disabled
    @Test
    public void calculateJudgmentForDoc1() throws IOException {

        final RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
        final RestHighLevelClient restHighLevelClient = new RestHighLevelClient(builder);

//        // Remove any existing indexes.
//        final boolean exists = restHighLevelClient.indices().exists(new GetIndexRequest("ubi_events"), RequestOptions.DEFAULT);
//        if(exists) {
//            restHighLevelClient.indices().delete(new DeleteIndexRequest("ubi_events"), RequestOptions.DEFAULT);
//        }
//
//        // Create the ubi_events index.
//        final CreateIndexRequest createIndexRequest = new CreateIndexRequest("ubi_events").mapping(getResourceFile("/events-mapping.json"));
//        restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);

        final int numberOfViews = 250;
        final int numberOfClicks = 110;

        final BulkRequest bulkRequest = new BulkRequest();

        // Index the view.
        for(int x = 1; x <= numberOfViews; x++) {

            final String event = "{\n" +
                    "    \"action_name\" : \"view\",\n" +
                    "    \"client_id\" : \"" + UUID.randomUUID() + "\",\n" +
                    "    \"query_id\" : \"" + UUID.randomUUID() + "\",\n" +
                    "    \"message_type\" : null,\n" +
                    "    \"message\" : null,\n" +
                    "    \"timestamp\" : 1.7276472197111678E9,\n" +
                    "    \"event_attributes\" : {\n" +
                    "      \"object\" : {\n" +
                    "        \"object_id_field\" : \"primary_ean\",\n" +
                    "        \"object_id\" : \"0731304258193\",\n" +
                    "        \"description\" : \"APC IT Power Distribution Module 3 Pole 5 Wire 32A IEC309 620cm power distribution unit (PDU)\"\n" +
                    "      },\n" +
                    "      \"position\" : {\n" +
                    "        \"index\" : 7\n" +
                    "      },\n" +
                    "      \"session_id\" : \"d4ed2513-aaa9-48c1-bcb9-e936a4e903a9\"\n" +
                    "    }" +
                    "}";

            final IndexRequest indexRequest = new IndexRequest(CoecClickModel.INDEX_UBI_EVENTS)
                    .id(String.valueOf(x))
                    .source(event, XContentType.JSON);

            bulkRequest.add(indexRequest);

        }

        // Index the clicks.
        for(int x = 1; x <= numberOfClicks; x++) {

//            final String query = "{\n" +
//                    "    \"user_query\" : \"computer\",\n" +
//                    "    \"query_id\" : \"" + UUID.randomUUID() + "\",\n" +
//                    "    \"message_type\" : null,\n" +
//                    "    \"message\" : null,\n" +
//                    "    \"timestamp\" : 1.7276472197111678E9,\n" +
//                    "    \"event_attributes\" : {\n" +
//                    "      \"object\" : {\n" +
//                    "        \"object_id_field\" : \"primary_ean\",\n" +
//                    "        \"object_id\" : \"0731304258193\",\n" +
//                    "        \"description\" : \"APC IT Power Distribution Module 3 Pole 5 Wire 32A IEC309 620cm power distribution unit (PDU)\"\n" +
//                    "      },\n" +
//                    "      \"position\" : {\n" +
//                    "        \"index\" : 7\n" +
//                    "      },\n" +
//                    "      \"session_id\" : \"" + UUID.randomUUID() + "\"\n" +
//                    "    }" +
//                    "}";
//
//            final IndexRequest queryIndexRequest = new IndexRequest(CoecClickModel.INDEX_UBI_QUERIES)
//                    .id(String.valueOf(x))
//                    .source(query, XContentType.JSON);
//            bulkRequest.add(queryIndexRequest);

            final String event = "{\n" +
                    "    \"action_name\" : \"click\",\n" +
                    "    \"client_id\" : \"" + UUID.randomUUID() + "\",\n" +
                    "    \"query_id\" : \"" + UUID.randomUUID() + "\",\n" +
                    "    \"message_type\" : null,\n" +
                    "    \"message\" : null,\n" +
                    "    \"timestamp\" : 1.7276472197111678E9,\n" +
                    "    \"event_attributes\" : {\n" +
                    "      \"object\" : {\n" +
                    "        \"object_id_field\" : \"primary_ean\",\n" +
                    "        \"object_id\" : \"0731304258193\",\n" +
                    "        \"description\" : \"APC IT Power Distribution Module 3 Pole 5 Wire 32A IEC309 620cm power distribution unit (PDU)\"\n" +
                    "      },\n" +
                    "      \"position\" : {\n" +
                    "        \"index\" : 7\n" +
                    "      },\n" +
                    "      \"session_id\" : \"" + UUID.randomUUID() + "\"\n" +
                    "    }" +
                    "}";

            final IndexRequest eventIndexRequest = new IndexRequest(CoecClickModel.INDEX_UBI_EVENTS)
                    .id(String.valueOf(x))
                    .source(event, XContentType.JSON);
            bulkRequest.add(eventIndexRequest);

        }

        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        final OpenSearchHelper openSearchHelper = new OpenSearchHelper(restHighLevelClient);

        final CoecClickModelParameters coecClickModelParameters = new CoecClickModelParameters(restHighLevelClient, true, 20);
        final CoecClickModel coecClickModel = new CoecClickModel(coecClickModelParameters, openSearchHelper);

        final Collection<Judgment> judgments = coecClickModel.calculateJudgments();
        Judgment.showJudgments(judgments);

    }
//
//    private XContentBuilder getResourceFile(final String fileName) {
//        try (InputStream is = CoecClickModelIT.class.getResourceAsStream(fileName)) {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            Streams.copy(is.readAllBytes(), out);
//
//            final String message = out.toString(StandardCharsets.UTF_8);
//
//            return XContentFactory.jsonBuilder().value(message);
//
//        } catch (IOException e) {
//            throw new IllegalStateException("Unable to get mapping from resource [" + fileName + "]", e);
//        }
//    }

}
