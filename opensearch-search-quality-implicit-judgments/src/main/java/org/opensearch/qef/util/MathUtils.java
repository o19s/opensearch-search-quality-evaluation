package org.opensearch.qef.util;

public class MathUtils {

    private MathUtils() {

    }

    public static String round(final double value, final int decimalPlaces) {
        double factor = Math.pow(10, decimalPlaces);
        return String.valueOf(Math.round(value * factor) / factor);
    }

    public static String round(final double value) {
        return round(value, 3);
    }

}
