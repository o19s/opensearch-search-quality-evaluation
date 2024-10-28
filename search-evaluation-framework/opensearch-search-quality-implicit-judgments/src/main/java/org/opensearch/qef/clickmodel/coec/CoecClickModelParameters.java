package org.opensearch.qef.clickmodel.coec;

import org.opensearch.client.Client;
import org.opensearch.client.OpenSearchClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.qef.clickmodel.ClickModelParameters;

public class CoecClickModelParameters extends ClickModelParameters {

    private final Client client;
    private final boolean persist;
    private final int maxRank;
    private int roundingDigits = 3;

    public CoecClickModelParameters(final Client client, boolean persist, final int maxRank) {
        this.client = client;
        this.persist = persist;
        this.maxRank = maxRank;
    }

    public CoecClickModelParameters(final Client client, boolean persist, final int maxRank, final int roundingDigits) {
        this.client = client;
        this.persist = persist;
        this.maxRank = maxRank;
        this.roundingDigits = roundingDigits;
    }

    public Client getClient() {
        return client;
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
