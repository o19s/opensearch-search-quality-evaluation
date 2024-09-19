package org.opensearch.searchevaluationframework.model;

public class ClickthroughRate {

    private final String objectId;
    private int clicks;
    private int events;

    public ClickthroughRate(final String objectId) {
        this.objectId = objectId;
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
        return "clicks: " + clicks + ", events: " + events + ", ctr: " + getClickthroughRate();
    }

    public int getClicks() {
        return clicks;
    }

    public int getEvents() {
        return events;
    }

    public String getObjectId() {
        return objectId;
    }

}
