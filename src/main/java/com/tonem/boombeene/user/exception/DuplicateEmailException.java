package com.tonem.boombeene.user.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("Email already exists");
    }
}
