package org.opensearch.searchevaluationframework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.searchevaluationframework.model.UbiEvent;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {

        final OpenSearchHelper openSearchHelper = new OpenSearchHelper();

        final Map<Integer, Double> rankAggregatedClickThrough = getRankAggregatedClickThrough(openSearchHelper);


    }

    private static Map<Integer, Double> getRankAggregatedClickThrough(final OpenSearchHelper openSearchHelper) throws IOException {

        final Map<Integer, Double> rankAggregatedClickThrough = new HashMap<>();

        int from = 0;
        int totalEvents = 0;

        while(true) {

            final Collection<UbiEvent> ubiEvents = openSearchHelper.getUbiEvents(from);

            if(ubiEvents.isEmpty()) {
                break;
            }

            for (final UbiEvent ubiEvent : ubiEvents) {

                // Increment the number of clicks for the position.
                if (StringUtils.equalsIgnoreCase(ubiEvent.getActionName(), "click")) {
                    rankAggregatedClickThrough.merge(ubiEvent.getPosition(), 1.0, Double::sum);
                }

            }

            from += ubiEvents.size();
            totalEvents += ubiEvents.size();

        }

        // Now for each position, divide the value by the total number of events.
        for(final Integer i : rankAggregatedClickThrough.keySet()) {
            rankAggregatedClickThrough.put(i, rankAggregatedClickThrough.get(i) / totalEvents);
        }

        System.out.println(rankAggregatedClickThrough);
        System.out.println(totalEvents);

        return rankAggregatedClickThrough;

    }

}
