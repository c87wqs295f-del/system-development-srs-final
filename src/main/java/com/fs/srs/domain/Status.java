package com.fs.srs.domain;

/**
 * The lifecycle states a {@link Request} can be in.
 * <p>
 * The allowed transitions between states are enforced by
 * {@link Request#transitionTo(Status)} — this enum only names the states.
 */
public enum Status {
    NEW,
    ASSIGNED,
    IN_PROGRESS,
    WAITING_FOR_INFO,
    RESOLVED,
    CLOSED
}
