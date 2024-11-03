package org.opensearch.qef.clickmodel;

import org.opensearch.qef.model.Judgment;

import java.io.IOException;
import java.util.Collection;

public abstract class ClickModel<T extends ClickModelParameters> {

    public static final String INDEX_UBI_EVENTS = "ubi_events";
    public static final String INDEX_UBI_QUERIES = "ubi_queries";

    public abstract Collection<Judgment> calculateJudgments() throws IOException;

}
