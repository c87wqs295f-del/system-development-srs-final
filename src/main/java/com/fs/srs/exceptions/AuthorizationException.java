package com.fs.srs.exceptions;

/** Used when the user tries to do something its role should not be ale to */
public class AuthorizationException extends RuntimeException {
    public AuthorizationException(String message) {
        super(message);
    }
}
