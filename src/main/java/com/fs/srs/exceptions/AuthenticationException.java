package com.fs.srs.exceptions;

/** Thrown when a login attempt fails (bad username or password). */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
