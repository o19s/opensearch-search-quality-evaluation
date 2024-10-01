package org.opensearch.qef.clickmodel.coec;

import org.opensearch.qef.clickmodel.ClickModelParameters;

public class CoecClickModelParameters extends ClickModelParameters {

    final private boolean persist;

    public CoecClickModelParameters(final boolean persist) {
        this.persist = persist;
    }

    public boolean isPersist() {
        return persist;
    }

}
