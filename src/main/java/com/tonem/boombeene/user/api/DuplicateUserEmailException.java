package com.tonem.boombeene.user.api;

import com.tonem.boombeene.user.exception.DuplicateEmailException;

public class DuplicateUserEmailException extends RuntimeException {

    public DuplicateUserEmailException(DuplicateEmailException cause) {
        super(cause.getMessage(), cause);
    }
}
