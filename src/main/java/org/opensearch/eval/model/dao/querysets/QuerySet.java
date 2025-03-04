/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model.dao.querysets;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensearch.eval.model.dao.AbstractData;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * A query set.
 */
public class QuerySet extends AbstractData {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("sampling")
    private String sampling;

    @JsonProperty("query_set_queries")
    private Collection<Map<String, Long>> querySetQueries;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("application")
    private String application;

    @JsonProperty("search_config")
    private String searchConfig;

    public QuerySet() {
        super(UUID.randomUUID().toString());
    }

    public QuerySet(String id) {
        super(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSampling() {
        return sampling;
    }

    public void setSampling(String sampling) {
        this.sampling = sampling;
    }

    public Collection<Map<String, Long>> getQuerySetQueries() {
        return querySetQueries;
    }

    public void setQuerySetQueries(Collection<Map<String, Long>> querySetQueries) {
        this.querySetQueries = querySetQueries;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getSearchConfig() {
        return searchConfig;
    }

    public void setSearchConfig(String searchConfig) {
        this.searchConfig = searchConfig;
    }

}
