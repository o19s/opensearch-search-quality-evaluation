package org.opensearch.qef.clickmodel.coec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.qef.engine.opensearch.OpenSearchHelper;
import org.opensearch.qef.model.ClickthroughRate;
import org.opensearch.qef.model.Judgment;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class CoecClickModelTest {

    private static final Logger LOGGER = LogManager.getLogger(CoecClickModelTest.class.getName());

    @Disabled
    @Test
    public void calculateJudgmentForDoc1() throws IOException {

        final RestHighLevelClient restHighLevelClient = Mockito.mock(RestHighLevelClient.class);
        final OpenSearchHelper openSearchHelper = Mockito.mock(OpenSearchHelper.class);

        when(openSearchHelper.getCountOfQueriesForUserQueryHavingResultInRankR(anyString(), anyString(), anyInt())).thenReturn(250);

        final CoecClickModelParameters parameters = new CoecClickModelParameters(false, 20);
        final CoecClickModel coecClickModel = new CoecClickModel(parameters);

        final Map<Integer, Double> rankAggregatedClickThrough = new HashMap<>();
        rankAggregatedClickThrough.put(1, 0.450);

        final Set<ClickthroughRate> ctrs = new HashSet<>();
        ctrs.add(new ClickthroughRate("object_id_1", 110, 250));

        final Map<String, Set<ClickthroughRate>> clickthroughRates = new HashMap<>();
        clickthroughRates.put("computer", ctrs);

        final Collection< Judgment> judgments = coecClickModel.calculateCoec(rankAggregatedClickThrough, clickthroughRates);

        Judgment.showJudgments(judgments);

        Assertions.assertEquals(1, judgments.size());
        Assertions.assertEquals(0.9777777777777777, judgments.iterator().next().getJudgment(), 0.01);

    }

    @Disabled
    @Test
    public void calculateJudgmentForDoc2() throws IOException {

        final RestHighLevelClient restHighLevelClient = Mockito.mock(RestHighLevelClient.class);
        final OpenSearchHelper openSearchHelper = Mockito.mock(OpenSearchHelper.class);

        when(openSearchHelper.getCountOfQueriesForUserQueryHavingResultInRankR(anyString(), anyString(), anyInt())).thenReturn(124);

        final CoecClickModelParameters parameters = new CoecClickModelParameters(false, 20);
        final CoecClickModel coecClickModel = new CoecClickModel(parameters);

        final Map<Integer, Double> rankAggregatedClickThrough = new HashMap<>();
        rankAggregatedClickThrough.put(2, 0.175);

        final Set<ClickthroughRate> ctrs = new HashSet<>();
        ctrs.add(new ClickthroughRate("object_id_2", 31, 124));

        final Map<String, Set<ClickthroughRate>> clickthroughRates = new HashMap<>();
        clickthroughRates.put("computer", ctrs);

        final Collection< Judgment> judgments = coecClickModel.calculateCoec(rankAggregatedClickThrough, clickthroughRates);

        Judgment.showJudgments(judgments);

        Assertions.assertEquals(1, judgments.size());
        Assertions.assertEquals(1.4285714285714286, judgments.iterator().next().getJudgment(), 0.01);

    }

    @Disabled
    @Test
    public void calculateJudgmentForDoc3() throws IOException {

        final RestHighLevelClient restHighLevelClient = Mockito.mock(RestHighLevelClient.class);
        final OpenSearchHelper openSearchHelper = Mockito.mock(OpenSearchHelper.class);

        when(openSearchHelper.getCountOfQueriesForUserQueryHavingResultInRankR(anyString(), anyString(), anyInt())).thenReturn(240);

        final CoecClickModelParameters parameters = new CoecClickModelParameters(false, 20);
        final CoecClickModel coecClickModel = new CoecClickModel(parameters);

        final Map<Integer, Double> rankAggregatedClickThrough = new HashMap<>();
        rankAggregatedClickThrough.put(3, 0.075);

        final Set<ClickthroughRate> ctrs = new HashSet<>();
        ctrs.add(new ClickthroughRate("object_id_3", 30, 240));

        final Map<String, Set<ClickthroughRate>> clickthroughRates = new HashMap<>();
        clickthroughRates.put("computer", ctrs);

        final Collection< Judgment> judgments = coecClickModel.calculateCoec(rankAggregatedClickThrough, clickthroughRates);

        Judgment.showJudgments(judgments);

        Assertions.assertEquals(1, judgments.size());
        Assertions.assertEquals(1.6666666666666667, judgments.iterator().next().getJudgment(), 0.01);

    }

}
