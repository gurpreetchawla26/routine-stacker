package com.routinestacker.web;

/** The request conflicts with existing state, such as a duplicate habit name. Maps to 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
