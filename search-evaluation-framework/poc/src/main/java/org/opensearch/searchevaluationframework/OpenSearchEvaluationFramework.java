package org.opensearch.searchevaluationframework;

import org.apache.commons.lang3.StringUtils;
import org.opensearch.searchevaluationframework.model.UbiEvent;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class OpenSearchEvaluationFramework {

    private final OpenSearchHelper openSearchHelper;

    public OpenSearchEvaluationFramework() {

        this.openSearchHelper = new OpenSearchHelper();

    }

    public Map<Integer, Double> getRankAggregatedClickThrough() throws IOException {

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

        System.out.println("Rank-aggregated click through: " + rankAggregatedClickThrough);
        System.out.println("Number of total events: " + totalEvents);

        return rankAggregatedClickThrough;

    }

}
