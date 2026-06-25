package com.tonem.boombeene.user.security;

import com.tonem.boombeene.global.common.EntityNotFoundException;
import com.tonem.boombeene.global.common.UserPrincipal;
import com.tonem.boombeene.user.application.UserService;
import com.tonem.boombeene.user.dto.UserAuthDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsernameReturnsUserPrincipal() {
        var user = new UserAuthDto(1L, "me@example.com", "encoded-password");
        when(userService.getByEmail("me@example.com")).thenReturn(user);

        var userDetails = userDetailsService.loadUserByUsername("me@example.com");

        assertThat(userDetails).isInstanceOf(UserPrincipal.class);
        assertThat(((UserPrincipal) userDetails).getUserId()).isEqualTo(1L);
        assertThat(((UserPrincipal) userDetails).getEmail()).isEqualTo("me@example.com");
        assertThat(userDetails.getUsername()).isEqualTo("me@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encoded-password");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsernameThrowsWhenUserDoesNotExist() {
        when(userService.getByEmail("missing@example.com"))
                .thenThrow(new EntityNotFoundException("User", "missing@example.com"));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
