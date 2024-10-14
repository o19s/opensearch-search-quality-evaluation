package org.opensearch.qef.clickmodel.coec;

import org.opensearch.qef.clickmodel.ClickModelParameters;

public class CoecClickModelParameters extends ClickModelParameters {

    final private boolean persist;
    final private int maxRank;

    public CoecClickModelParameters(final boolean persist, final int maxRank) {
        this.persist = persist;
        this.maxRank = maxRank;
    }

    public boolean isPersist() {
        return persist;
    }

    public int getMaxRank() {
        return maxRank;
    }

}
