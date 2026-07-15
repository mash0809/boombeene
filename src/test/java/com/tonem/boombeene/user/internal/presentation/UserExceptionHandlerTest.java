package com.tonem.boombeene.user.internal.presentation;

import com.tonem.boombeene.user.internal.exception.DuplicateEmailException;
import com.tonem.boombeene.user.internal.presentation.UserExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserExceptionHandlerTest {

    private final UserExceptionHandler exceptionHandler = new UserExceptionHandler();

    @Test
    @DisplayName("DuplicateEmailException 발생 시 예외 메시지를 응답으로 반환한다")
    void handleDuplicateEmailReturnsExceptionMessage() {
        var response = exceptionHandler.handleDuplicateEmail(new DuplicateEmailException());

        assertThat(response.message()).isEqualTo("Email already exists");
    }
}
