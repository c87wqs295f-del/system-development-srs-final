package com.fs.srs.exceptions;

/** Messsage when users want to change to status our rules forbid
 */
public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
