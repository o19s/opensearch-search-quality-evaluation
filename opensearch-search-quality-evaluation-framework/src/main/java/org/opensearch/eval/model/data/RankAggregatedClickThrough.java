package org.opensearch.eval.model.data;

import java.util.UUID;

public class RankAggregatedClickThrough extends AbstractData {

    private int position;
    private double ctr;

    public RankAggregatedClickThrough(String id) {
        super(id);
    }

    public RankAggregatedClickThrough() {
        super(UUID.randomUUID().toString());
    }


    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public double getCtr() {
        return ctr;
    }

    public void setCtr(double ctr) {
        this.ctr = ctr;
    }

}
