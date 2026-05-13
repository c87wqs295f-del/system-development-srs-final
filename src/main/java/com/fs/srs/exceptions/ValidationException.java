package com.fs.srs.exceptions;

/** Thrown when user-provided input fails validation (empty title, missing fields, etc.). */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
