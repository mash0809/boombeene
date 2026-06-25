package com.tonem.boombeene.user.presentation;

import com.tonem.boombeene.user.application.UserService;
import com.tonem.boombeene.user.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void meReturnsAuthenticatedUser() {
        when(userService.getById(1L)).thenReturn(new UserDto(1L, "me@example.com", "nickname"));

        var response = userController.me(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("me@example.com");
        assertThat(response.nickname()).isEqualTo("nickname");
    }
}
