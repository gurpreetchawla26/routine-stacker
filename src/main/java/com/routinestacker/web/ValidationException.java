package com.routinestacker.web;

/** Input failed validation. Maps to 400. */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
