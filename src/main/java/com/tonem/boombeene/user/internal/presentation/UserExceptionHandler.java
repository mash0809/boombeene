package com.tonem.boombeene.user.internal.presentation;

import com.tonem.boombeene.common.exception.ErrorResponse;
import com.tonem.boombeene.user.internal.exception.DuplicateEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.tonem.boombeene.user")
public class UserExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse handleDuplicateEmail(DuplicateEmailException exception) {
        return new ErrorResponse(exception.getMessage());
    }
}
