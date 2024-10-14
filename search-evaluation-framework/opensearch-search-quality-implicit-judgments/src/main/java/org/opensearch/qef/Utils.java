package org.opensearch.qef;

public class Utils {

    private Utils() {

    }

    public static double round(final double value) {
        return (double) Math.round(value * 1000) / 1000;
    }

}
