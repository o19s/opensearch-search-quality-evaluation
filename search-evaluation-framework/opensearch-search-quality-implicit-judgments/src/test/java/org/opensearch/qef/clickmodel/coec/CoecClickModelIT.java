package org.opensearch.qef.clickmodel.coec;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.qef.engine.opensearch.OpenSearchHelper;
import org.opensearch.qef.model.ClickthroughRate;
import org.opensearch.qef.model.Judgment;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class CoecClickModelIT {

    private static final Logger LOGGER = LogManager.getLogger(CoecClickModelIT.class.getName());

    @Disabled
    @Test
    public void calculateJudgmentForDoc1() throws IOException {

        final RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
        final RestHighLevelClient restHighLevelClient = new RestHighLevelClient(builder);

        final int numberOfEvents = 250;
        final int numberOfClicks = 110;

        // Index the queries

//        for(int x = 1; x <= numberOfClicks; x++) {
//
//            final Map<String, String> source = new HashMap<>();
//            source.put("action_name", CoecClickModel.EVENT_CLICK);
//            source.put("query_id", UUID.randomUUID().toString());
//            source.put("user_query", "computer");
//
//            final IndexRequest indexRequest = new IndexRequest(CoecClickModel.INDEX_UBI_QUERIES)
//                    .id(String.valueOf(x))
//                    .source(source);
//
//            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
//
//        }
//
//        // Index the events.

        for(int x = 1; x <= numberOfEvents / 2; x++) {

            String action;
            if(x < numberOfClicks) {
                action = "click";
            } else {
                action = "viewed";
            }

            final String source1 = "{\n" +
                    "    \"action_name\" : \"" + action + "\",\n" +
                    "    \"client_id\" : \"2ce5eece-53cf-4b0c-9c55-bbeb57ad8642\",\n" +
                    "    \"query_id\" : \"efbeb66a-5b6b-48bd-89a9-33b171f95b2b\",\n" +
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
                    "    }";

            final String source2 = "{\n" +
                    "    \"action_name\" : \"" + action + "\",\n" +
                    "    \"client_id\" : \"2ce5eece-53cf-4b0c-9c55-bbeb57ad8642\",\n" +
                    "    \"query_id\" : \"efbeb66a-5b6b-48bd-89a9-33b171f95b2b\",\n" +
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
                    "    }";


            final IndexRequest indexRequest = new IndexRequest(CoecClickModel.INDEX_UBI_EVENTS)
                    .id(String.valueOf(x))
                    .source(source1, source2);

            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);



            final OpenSearchHelper openSearchHelper = new OpenSearchHelper(restHighLevelClient);

            final CoecClickModelParameters coecClickModelParameters = new CoecClickModelParameters(restHighLevelClient, true, 20);
            final CoecClickModel coecClickModel = new CoecClickModel(coecClickModelParameters, openSearchHelper);

            final Collection<Judgment> judgments = coecClickModel.calculateJudgments();
            Judgment.showJudgments(judgments);

        }

    }

}
