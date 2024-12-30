/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.model.data;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class QuerySet extends AbstractData {

    private String name;
    private String description;
    private String sampling;
    private Collection<Map<String, Long>> querySetQueries;
    private String timestamp;

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

}
