package com.tonem.boombeene.auth.presentation;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class AuthExceptionHandlerTest {

    private final AuthExceptionHandler exceptionHandler = new AuthExceptionHandler();

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
