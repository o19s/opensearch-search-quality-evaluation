package org.opensearch.qef.clickmodel.coec;

import org.opensearch.client.RestHighLevelClient;
import org.opensearch.qef.clickmodel.ClickModelParameters;

public class CoecClickModelParameters extends ClickModelParameters {

    private final RestHighLevelClient restHighLevelClient;
    private final boolean persist;
    private final int maxRank;
    private int roundingDigits = 3;

    public CoecClickModelParameters(final RestHighLevelClient restHighLevelClient, boolean persist, final int maxRank) {
        this.restHighLevelClient = restHighLevelClient;
        this.persist = persist;
        this.maxRank = maxRank;
    }

    public CoecClickModelParameters(final RestHighLevelClient restHighLevelClient, boolean persist, final int maxRank, final int roundingDigits) {
        this.restHighLevelClient = restHighLevelClient;
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
