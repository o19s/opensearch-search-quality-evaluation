package org.opensearch.searchevaluationframework.model;

public class UbiTransformed {

    private String queryId;
    private int position;
    private int numResults;
    private int clicked;
    private String documentId;

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getNumResults() {
        return numResults;
    }

    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }

    public int getClicked() {
        return clicked;
    }

    public void setClicked(int clicked) {
        this.clicked = clicked;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

}
