package org.opensearch.qef.util;

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
