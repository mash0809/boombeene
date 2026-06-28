package com.tonem.boombeene.user.application;

import com.tonem.boombeene.user.api.DuplicateUserEmailException;
import com.tonem.boombeene.user.api.SignupCommand;
import com.tonem.boombeene.user.dto.SignupRequest;
import com.tonem.boombeene.user.dto.UserAuthDto;
import com.tonem.boombeene.user.dto.UserDto;
import com.tonem.boombeene.user.exception.DuplicateEmailException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFacadeImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserFacadeImpl userFacade;

    @Test
    void signupDelegatesToUserService() {
        var command = new SignupCommand("me@example.com", "password123", "nickname");

        userFacade.signup(command);

        verify(userService).signup(new SignupRequest("me@example.com", "password123", "nickname"));
    }

    @Test
    void signupTranslatesDuplicateEmailExceptionToFacadeException() {
        var command = new SignupCommand("me@example.com", "password123", "nickname");
        doThrow(new DuplicateEmailException())
                .when(userService)
                .signup(any(SignupRequest.class));

        assertThatThrownBy(() -> userFacade.signup(command))
                .isInstanceOf(DuplicateUserEmailException.class);
    }

    @Test
    void getAuthUserByEmailDelegatesToUserService() {
        var user = new UserAuthDto(1L, "me@example.com", "encoded-password");
        when(userService.getByEmail("me@example.com")).thenReturn(user);

        var result = userFacade.getAuthUserByEmail("me@example.com");

        assertThat(result.id()).isEqualTo(user.id());
        assertThat(result.email()).isEqualTo(user.email());
        assertThat(result.password()).isEqualTo(user.password());
    }

    @Test
    void getUserByIdDelegatesToUserService() {
        var user = new UserDto(1L, "me@example.com", "nickname", 0);
        when(userService.getById(1L)).thenReturn(user);

        var result = userFacade.getUserById(1L);

        assertThat(result.id()).isEqualTo(user.id());
        assertThat(result.email()).isEqualTo(user.email());
        assertThat(result.nickname()).isEqualTo(user.nickname());
    }
}
