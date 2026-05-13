package com.fs.srs.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriorityTest {

    @Test
    void slaHoursMatchDesign() {
        assertEquals(4,   Priority.URGENT.getSlaHours());
        assertEquals(24,  Priority.HIGH.getSlaHours());
        assertEquals(72,  Priority.MEDIUM.getSlaHours());
        assertEquals(168, Priority.LOW.getSlaHours());
    }
}
