package com.tonem.boombeene.user.presentation;

import com.tonem.boombeene.global.common.ErrorResponse;
import com.tonem.boombeene.user.exception.DuplicateEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
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

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorResponse handleBadCredentials(BadCredentialsException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorResponse handleAuthenticationFailed(AuthenticationException exception) {
        return new ErrorResponse(exception.getMessage());
    }
}
