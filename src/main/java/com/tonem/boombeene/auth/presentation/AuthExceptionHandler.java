package com.tonem.boombeene.auth.presentation;

import com.tonem.boombeene.global.common.ErrorResponse;
import com.tonem.boombeene.user.api.DuplicateUserEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.tonem.boombeene.auth")
public class AuthExceptionHandler {

    @ExceptionHandler(DuplicateUserEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse handleDuplicateEmail(DuplicateUserEmailException exception) {
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
