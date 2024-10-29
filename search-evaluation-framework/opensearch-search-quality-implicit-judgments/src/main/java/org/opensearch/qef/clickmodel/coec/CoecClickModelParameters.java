package org.opensearch.qef.clickmodel.coec;

import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.qef.clickmodel.ClickModelParameters;

public class CoecClickModelParameters extends ClickModelParameters {

    private final RestHighLevelClient restHighLevelClient;
    private final boolean persist;
    private final int maxRank;
    private int roundingDigits = 3;

    public CoecClickModelParameters(boolean persist, final int maxRank) {

        final RestClientBuilder builder = RestClient.builder("http://localhost:9200");
        this.restHighLevelClient = new RestHighLevelClient(builder);

        this.persist = persist;
        this.maxRank = maxRank;
    }

    public CoecClickModelParameters(boolean persist, final int maxRank, final int roundingDigits) {

        final RestClientBuilder builder = RestClient.builder("http://localhost:9200");
        this.restHighLevelClient = new RestHighLevelClient(builder);

        this.persist = persist;
        this.maxRank = maxRank;
        this.roundingDigits = roundingDigits;
    }

    public RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
    }

    public boolean isPersist() {
        return persist;
    }

    public int getMaxRank() {
        return maxRank;
    }

    public int getRoundingDigits() {
        return roundingDigits;
    }

}
