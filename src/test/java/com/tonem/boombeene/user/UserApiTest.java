package com.tonem.boombeene.user;

import com.tonem.boombeene.user.internal.application.UserService;
import com.tonem.boombeene.user.internal.dto.UserAuthDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApiTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserApi userApi;

    @Test
    void getAuthUserByEmailDelegatesToUserService() {
        var user = new UserAuthDto(1L, "me@example.com", "encoded-password");
        when(userService.getByEmail("me@example.com")).thenReturn(user);

        var result = userApi.getAuthUserByEmail("me@example.com");

        assertThat(result.id()).isEqualTo(user.id());
        assertThat(result.email()).isEqualTo(user.email());
        assertThat(result.password()).isEqualTo(user.password());
    }
}
