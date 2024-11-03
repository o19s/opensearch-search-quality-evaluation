package org.opensearch.qef.model.ubi.event;

import com.google.gson.annotations.SerializedName;

public class Position {

    @SerializedName("index")
    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
