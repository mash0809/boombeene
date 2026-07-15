package com.tonem.boombeene.auth.internal.security;

import com.tonem.boombeene.auth.internal.security.UserDetailsServiceImpl;
import com.tonem.boombeene.auth.internal.security.UserPrincipal;
import com.tonem.boombeene.common.exception.EntityNotFoundException;
import com.tonem.boombeene.user.UserApi;
import com.tonem.boombeene.user.UserAuthInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserApi userApi;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("사용자명으로 조회 시 UserPrincipal을 반환한다")
    void loadUserByUsernameReturnsUserPrincipal() {
        var user = new UserAuthInfo(1L, "me@example.com", "encoded-password");
        when(userApi.getAuthUserByEmail("me@example.com")).thenReturn(user);

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
    @DisplayName("존재하지 않는 사용자를 조회하면 예외를 던진다")
    void loadUserByUsernameThrowsWhenUserDoesNotExist() {
        when(userApi.getAuthUserByEmail("missing@example.com"))
                .thenThrow(new EntityNotFoundException("User", "missing@example.com"));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
