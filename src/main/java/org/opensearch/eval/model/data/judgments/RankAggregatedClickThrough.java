/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model.data.judgments;

import org.opensearch.eval.model.data.AbstractData;

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
