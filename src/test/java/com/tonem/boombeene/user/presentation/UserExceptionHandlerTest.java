package com.tonem.boombeene.user.presentation;

import com.tonem.boombeene.user.exception.DuplicateEmailException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class UserExceptionHandlerTest {

    private final UserExceptionHandler exceptionHandler = new UserExceptionHandler();

    @Test
    void handleDuplicateEmailReturnsExceptionMessage() {
        var response = exceptionHandler.handleDuplicateEmail(new DuplicateEmailException());

        assertThat(response.message()).isEqualTo("Email already exists");
    }

    @Test
    void handleBadCredentialsReturnsExceptionMessage() {
        var response = exceptionHandler.handleBadCredentials(new BadCredentialsException("Bad credentials"));

        assertThat(response.message()).isEqualTo("Bad credentials");
    }

    @Test
    void handleAuthenticationFailedReturnsExceptionMessage() {
        var response = exceptionHandler.handleAuthenticationFailed(new AuthenticationException("Authentication failed") {
        });

        assertThat(response.message()).isEqualTo("Authentication failed");
    }
}
