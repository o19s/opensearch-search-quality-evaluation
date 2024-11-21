/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.model.ubi.event;

import com.google.gson.annotations.SerializedName;

/**
 * A position represents the location of a search result in an event.
 */
public class Position {

    @SerializedName("index")
    private int index;

    @Override
    public String toString() {
        return String.valueOf(index);
    }

    /**
     * Gets the index of the position.
     * @return The index of the position.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index of the position.
     * @param index The index of the position.
     */
    public void setIndex(int index) {
        this.index = index;
    }

}
