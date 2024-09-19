package org.opensearch.searchevaluationframework.model;

public class ClickthroughRate {

    private final String queryId;
    private int clicks;
    private int events;

    public ClickthroughRate(String queryId) {
        this.queryId = queryId;
    }

    public void logClick() {
        clicks++;
        events++;
    }

    public void logEvent() {
        events++;
    }

    public double getClickthroughRate() {
        return (double) clicks / events;
    }

    @Override
    public String toString() {
        return "queryId: " + queryId + ", clicks: " + clicks + ", events: " + events + ", ctr: " + getClickthroughRate();
    }

    public String getQueryId() {
        return queryId;
    }

    public int getClicks() {
        return clicks;
    }

    public int getEvents() {
        return events;
    }

}
