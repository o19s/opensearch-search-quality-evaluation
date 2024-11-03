/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.judgments.util;

import java.util.HashMap;
import java.util.Map;

public class UserQueryHash {

    private final Map<String, Integer> userQueries;
    private int count = 1;

    public UserQueryHash() {
        this.userQueries = new HashMap<>();
    }

    public int getHash(String userQuery) {

        final int hash;

        if(userQueries.containsKey(userQuery)) {

            return userQueries.get(userQuery);

        } else {

            userQueries.put(userQuery, count);
            hash = count;
            count++;


        }

        return hash;

    }

}
