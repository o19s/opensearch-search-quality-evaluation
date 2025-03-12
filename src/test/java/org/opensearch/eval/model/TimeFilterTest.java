package org.opensearch.eval.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimeFilterTest {

    @Test()
    public void testTimeFilter() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> TimeFilter.validateTimetampFormat("2025-02-19 10:30:00"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> TimeFilter.validateTimetampFormat("2025-02-19 10:30:00Z"));

        Assertions.assertTrue(TimeFilter.validateTimetampFormat("2024-07-26T10:30:15.123Z"));
        Assertions.assertTrue(TimeFilter.validateTimetampFormat("2019-03-23T21:34:46.123Z-4:00"));

    }

}
