package org.opensearch.qef;

import java.math.BigDecimal;
import java.util.Locale;

public class Utils {

    private Utils() {

    }

    public static String toSignificantFiguresString(final double value) {

        final int significantFigures = 3;
        final BigDecimal bd = new BigDecimal(value);

        return String.format(Locale.US, "%." + significantFigures + "G", bd);

    }

}