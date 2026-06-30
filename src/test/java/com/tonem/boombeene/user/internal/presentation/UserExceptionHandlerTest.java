package com.tonem.boombeene.user.internal.presentation;

import com.tonem.boombeene.user.internal.exception.DuplicateEmailException;
import com.tonem.boombeene.user.internal.presentation.UserExceptionHandler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserExceptionHandlerTest {

    private final UserExceptionHandler exceptionHandler = new UserExceptionHandler();

    @Test
    void handleDuplicateEmailReturnsExceptionMessage() {
        var response = exceptionHandler.handleDuplicateEmail(new DuplicateEmailException());

        assertThat(response.message()).isEqualTo("Email already exists");
    }
}
