package com.fs.srs.domain;

/**
 * Priority level with an associated SLA (Service Level Agreement) in hours.
 * <p>
 * Demonstrates that Java enums are real classes that can carry state and
 * behavior (see Session 2: "a class bundles state and behavior").
 */
public enum Priority {
    URGENT(4),
    HIGH(24),
    MEDIUM(72),
    LOW(168);

    private final int slaHours;

    Priority(int slaHours) {
        this.slaHours = slaHours;
    }

    /** @return the SLA target for a request with this priority, in hours */
    public int getSlaHours() {
        return slaHours;
    }
}
