/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.samplers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.eval.model.ubi.query.UbiQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class RandomQuerySamplerTest extends AbstractSamplerTest {

    @Test
    public void simpleSample() {

        final Collection<UbiQuery> ubiQueries = new ArrayList<>();

        final UbiQuery ubiQuery1 = new UbiQuery();
        ubiQuery1.setUserQuery("user1");
        ubiQueries.add(ubiQuery1);

        final UbiQuery ubiQuery2 = new UbiQuery();
        ubiQuery2.setUserQuery("user2");
        ubiQueries.add(ubiQuery2);

        final UbiQuery ubiQuery3 = new UbiQuery();
        ubiQuery3.setUserQuery("user2");
        ubiQueries.add(ubiQuery3);

        final UbiQuery ubiQuery4 = new UbiQuery();
        ubiQuery4.setUserQuery("user2");
        ubiQueries.add(ubiQuery4);

        final UbiQuery ubiQuery5 = new UbiQuery();
        ubiQuery5.setUserQuery("user1");
        ubiQueries.add(ubiQuery5);

        final UbiQuery ubiQuery6 = new UbiQuery();
        ubiQuery6.setUserQuery("user6");
        ubiQueries.add(ubiQuery6);

        final UbiQuery ubiQuery7 = new UbiQuery();
        ubiQuery7.setUserQuery("user7");
        ubiQueries.add(ubiQuery7);

        final UbiQuery ubiQuery8 = new UbiQuery();
        ubiQuery8.setUserQuery("user8");
        ubiQueries.add(ubiQuery8);

        final RandomQuerySamplerParameters parameters = new RandomQuerySamplerParameters("name", "description", "sampling", 2);

        final RandomQuerySampler sampler = new RandomQuerySampler(parameters);
        final Map<String, Long> querySet = sampler.sample(ubiQueries);

        showQueries(querySet);

        Assertions.assertEquals(2, querySet.size());

    }

}
