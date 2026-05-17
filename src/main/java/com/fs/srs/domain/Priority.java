package com.fs.srs.domain;

/**
 * Priority level with an associated Service Level Agreement in hours.
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

    /** @return the Service Level Agreement target for a request with this priority, in hours */
    public int getSlaHours() {
        return slaHours;
    }
}
