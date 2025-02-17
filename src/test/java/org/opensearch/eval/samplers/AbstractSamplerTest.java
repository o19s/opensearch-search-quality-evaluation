package org.opensearch.eval.samplers;

import java.util.Map;

public abstract class AbstractSamplerTest {

    protected void showQueries(final Map<String, Long> querySet) {

        for(final String query : querySet.keySet()) {
            System.out.println("Query: " + query + ", Frequency: " + querySet.get(query));
        }

    }

}
