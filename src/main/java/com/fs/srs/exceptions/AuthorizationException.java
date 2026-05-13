package com.fs.srs.exceptions;

/** Thrown when the logged-in user tries to perform an action their role does not permit. */
public class AuthorizationException extends RuntimeException {
    public AuthorizationException(String message) {
        super(message);
    }
}
