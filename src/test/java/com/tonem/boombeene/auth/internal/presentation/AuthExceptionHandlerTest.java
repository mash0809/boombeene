package com.tonem.boombeene.auth.internal.presentation;

import com.tonem.boombeene.auth.internal.presentation.AuthExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class AuthExceptionHandlerTest {

    private final AuthExceptionHandler exceptionHandler = new AuthExceptionHandler();

    @Test
    @DisplayName("BadCredentialsException 발생 시 예외 메시지를 응답으로 반환한다")
    void handleBadCredentialsReturnsExceptionMessage() {
        var response = exceptionHandler.handleBadCredentials(new BadCredentialsException("Bad credentials"));

        assertThat(response.message()).isEqualTo("Bad credentials");
    }

    @Test
    @DisplayName("인증 실패 예외 발생 시 예외 메시지를 응답으로 반환한다")
    void handleAuthenticationFailedReturnsExceptionMessage() {
        var response = exceptionHandler.handleAuthenticationFailed(new AuthenticationException("Authentication failed") {
        });

        assertThat(response.message()).isEqualTo("Authentication failed");
    }
}
