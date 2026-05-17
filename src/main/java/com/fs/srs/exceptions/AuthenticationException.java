package com.fs.srs.exceptions;

/** Thrown when a login fails beacause of wrong login data */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
