package com.fs.srs.exceptions;

/**
 * Thrown by {@link com.fs.srs.domain.Request#transitionTo} when the caller
 * tries to move a request into a state the lifecycle rules forbid
 * (e.g. CLOSED -> IN_PROGRESS).
 */
public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
