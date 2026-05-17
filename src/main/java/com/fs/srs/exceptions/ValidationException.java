package com.fs.srs.exceptions;

/** message for wrong inputs for example not filled in fields or too many characters */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
