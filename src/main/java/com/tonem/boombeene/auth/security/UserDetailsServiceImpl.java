package com.tonem.boombeene.auth.security;

import com.tonem.boombeene.common.exception.EntityNotFoundException;
import com.tonem.boombeene.user.api.UserFacade;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserFacade userFacade;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) {
        try {
            var user = userFacade.getAuthUserByEmail(email);
            return new UserPrincipal(user.id(), user.email(), user.password());
        } catch (EntityNotFoundException e) {
            // Spring Security는 사용자 조회 실패를 UsernameNotFoundException으로 받아야
            // 인증 실패(AuthenticationException) 흐름에서 401 응답으로 처리할 수 있다.
            throw new UsernameNotFoundException("User not found: " + email, e);
        }
    }
}
