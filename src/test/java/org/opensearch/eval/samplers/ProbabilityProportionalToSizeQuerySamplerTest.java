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

public class ProbabilityProportionalToSizeQuerySamplerTest {

    @Test
    public void simpleSample() throws Exception {

        final Collection<UbiQuery> ubiQueries = new ArrayList<>();

        final UbiQuery ubiQuery1 = new UbiQuery();
        ubiQuery1.setUserQuery("user1");
        ubiQueries.add(ubiQuery1);

        final UbiQuery ubiQuery2 = new UbiQuery();
        ubiQuery2.setUserQuery("user2");
        ubiQueries.add(ubiQuery2);

        final UbiQuery ubiQuery3 = new UbiQuery();
        ubiQuery3.setUserQuery("user3");
        ubiQueries.add(ubiQuery3);

        final UbiQuery ubiQuery4 = new UbiQuery();
        ubiQuery4.setUserQuery("user4");
        ubiQueries.add(ubiQuery4);

        final UbiQuery ubiQuery5 = new UbiQuery();
        ubiQuery5.setUserQuery("user5");
        ubiQueries.add(ubiQuery5);

        final ProbabilityProportionalToSizeParametersQuery parameters = new ProbabilityProportionalToSizeParametersQuery("name", "description", "sampling", 10);

        final ProbabilityProportionalToSizeQuerySampler sampler = new ProbabilityProportionalToSizeQuerySampler( parameters);
        final Map<String, Long> querySet = sampler.sample(ubiQueries);

        Assertions.assertTrue(ubiQueries.size() <= Math.max(ubiQueries.size(), 10));

    }

    @Test
    public void simpleSampleNoDuplicates() throws Exception {

        final Collection<UbiQuery> ubiQueries = new ArrayList<>();

        final UbiQuery ubiQuery1 = new UbiQuery();
        ubiQuery1.setUserQuery("user1");
        ubiQueries.add(ubiQuery1);

        final UbiQuery ubiQuery2 = new UbiQuery();
        ubiQuery2.setUserQuery("user2");
        ubiQueries.add(ubiQuery2);

        final UbiQuery ubiQuery3 = new UbiQuery();
        ubiQuery3.setUserQuery("user3");
        ubiQueries.add(ubiQuery3);

        final UbiQuery ubiQuery4 = new UbiQuery();
        ubiQuery4.setUserQuery("user4");
        ubiQueries.add(ubiQuery4);

        final UbiQuery ubiQuery5 = new UbiQuery();
        ubiQuery5.setUserQuery("user5");
        ubiQueries.add(ubiQuery5);

        final UbiQuery ubiQuery6 = new UbiQuery();
        ubiQuery6.setUserQuery("user5");
        ubiQueries.add(ubiQuery6);

        final ProbabilityProportionalToSizeParametersQuery parameters = new ProbabilityProportionalToSizeParametersQuery("name", "description", "sampling", 10);

        final ProbabilityProportionalToSizeQuerySampler sampler = new ProbabilityProportionalToSizeQuerySampler(parameters);
        final Map<String, Long> querySet = sampler.sample(ubiQueries);

        Assertions.assertTrue(ubiQueries.size() <= Math.max(ubiQueries.size(), 10));

    }

    @Test
    public void simpleSampleNoDuplicatesSame() throws Exception {

        final Collection<UbiQuery> ubiQueries = new ArrayList<>();

        final UbiQuery ubiQuery1 = new UbiQuery();
        ubiQuery1.setUserQuery("user1");
        ubiQueries.add(ubiQuery1);

        final UbiQuery ubiQuery2 = new UbiQuery();
        ubiQuery2.setUserQuery("user1");
        ubiQueries.add(ubiQuery2);

        final UbiQuery ubiQuery3 = new UbiQuery();
        ubiQuery3.setUserQuery("user1");
        ubiQueries.add(ubiQuery3);

        final UbiQuery ubiQuery4 = new UbiQuery();
        ubiQuery4.setUserQuery("user1");
        ubiQueries.add(ubiQuery4);

        final UbiQuery ubiQuery5 = new UbiQuery();
        ubiQuery5.setUserQuery("user1");
        ubiQueries.add(ubiQuery5);

        final ProbabilityProportionalToSizeParametersQuery parameters = new ProbabilityProportionalToSizeParametersQuery("name", "description", "sampling", 10);

        final ProbabilityProportionalToSizeQuerySampler sampler = new ProbabilityProportionalToSizeQuerySampler(parameters);
        final Map<String, Long> querySet = sampler.sample(ubiQueries);

        Assertions.assertEquals(1, querySet.size());

    }

    @Test
    public void simpleSampleFew() throws Exception {

        final Collection<UbiQuery> ubiQueries = new ArrayList<>();

        final UbiQuery ubiQuery1 = new UbiQuery();
        ubiQuery1.setUserQuery("user1");
        ubiQueries.add(ubiQuery1);

        final UbiQuery ubiQuery2 = new UbiQuery();
        ubiQuery2.setUserQuery("user2");
        ubiQueries.add(ubiQuery2);

        final UbiQuery ubiQuery3 = new UbiQuery();
        ubiQuery3.setUserQuery("user3");
        ubiQueries.add(ubiQuery3);

        final UbiQuery ubiQuery4 = new UbiQuery();
        ubiQuery4.setUserQuery("user4");
        ubiQueries.add(ubiQuery4);

        final UbiQuery ubiQuery5 = new UbiQuery();
        ubiQuery5.setUserQuery("user5");
        ubiQueries.add(ubiQuery5);

        final ProbabilityProportionalToSizeParametersQuery parameters = new ProbabilityProportionalToSizeParametersQuery("name", "description", "sampling", 3);

        final ProbabilityProportionalToSizeQuerySampler sampler = new ProbabilityProportionalToSizeQuerySampler(parameters);
        final Map<String, Long> querySet = sampler.sample(ubiQueries);

        Assertions.assertTrue(ubiQueries.size() <= Math.max(ubiQueries.size(), 3));

    }

}
