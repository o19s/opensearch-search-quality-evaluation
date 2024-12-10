/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.eval.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This is a utility class.
 */
public class TimeUtils {

    /**
     * Generate a timestamp in the <code>yyyyMMdd'T'HHmmss.SSS'Z'</code> format.
     * @return A timestamp in the <code>yyyyMMdd'T'HHmmss.SSS'Z'</code> format.
     */
    public static String getTimestamp() {

        final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS'Z'", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        final Date date = new Date();
        return formatter.format(date);

    }

}
