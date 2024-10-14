package org.opensearch.qef;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.qef.clickmodel.coec.CoecClickModel;
import org.opensearch.qef.clickmodel.coec.CoecClickModelParameters;
import org.opensearch.qef.model.Judgment;

import java.util.Collection;

/**
 * Entry point for the OpenSearch Evaluation Framework.
 */
public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {

        final RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
        final RestHighLevelClient restHighLevelClient = new RestHighLevelClient(builder);

        final CoecClickModelParameters coecClickModelParameters = new CoecClickModelParameters(restHighLevelClient, false, 20);
        final CoecClickModel coecClickModel = new CoecClickModel(coecClickModelParameters);

        final Collection<Judgment> judgments = coecClickModel.calculateJudgments();
        Judgment.showJudgments(judgments);

    }

}
