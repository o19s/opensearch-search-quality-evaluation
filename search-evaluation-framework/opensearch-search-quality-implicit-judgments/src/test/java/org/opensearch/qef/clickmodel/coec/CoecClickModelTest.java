package org.opensearch.qef.clickmodel.coec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.qef.model.ClickthroughRate;
import org.opensearch.qef.model.Judgment;

import java.io.IOException;
import java.util.*;

public class CoecClickModelTest {

    private static final Logger LOGGER = LogManager.getLogger(CoecClickModelTest.class.getName());

    @Test
    public void calculateJudgments() throws IOException {

        final CoecClickModelParameters parameters = new CoecClickModelParameters(false, 20);
        final CoecClickModel coecClickModel = new CoecClickModel(parameters);

        final Map<Integer, Double> rankAggregatedClickThrough = new HashMap<>();
        rankAggregatedClickThrough.put(1, 0.450);

        final Set<ClickthroughRate> ctrs = new HashSet<>();
        ctrs.add(new ClickthroughRate("object_id_1", 110, 250));

        final Map<String, Set<ClickthroughRate>> clickthroughRates = new HashMap<>();
        clickthroughRates.put("query_id", ctrs);

        final Collection< Judgment> judgments = coecClickModel.calculateJudgments(rankAggregatedClickThrough, clickthroughRates);

        Judgment.showJudgments(judgments);

        Assertions.assertEquals(1, judgments.size());
        Assertions.assertEquals(judgments.iterator().next().getJudgment(), 0.9777777777777777);

    }

}
