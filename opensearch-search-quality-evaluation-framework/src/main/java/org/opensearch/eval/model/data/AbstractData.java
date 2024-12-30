package org.opensearch.eval.model.data;

public abstract class AbstractData {

    private String id;

    public AbstractData(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

}
