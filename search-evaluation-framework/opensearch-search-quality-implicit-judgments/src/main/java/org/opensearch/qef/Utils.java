package org.opensearch.qef;

public class Utils {

    private Utils() {

    }

    public static String round(final double value, final int decimalPlaces) {
        return String.valueOf(Math.round(value * 100.0) / 100.0);
    }

    public static String round(final double value) {
        return round(value, 3);
    }

}
