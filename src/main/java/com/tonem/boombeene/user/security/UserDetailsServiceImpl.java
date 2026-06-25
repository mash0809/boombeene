package com.tonem.boombeene.user.security;

import com.tonem.boombeene.global.common.UserPrincipal;
import com.tonem.boombeene.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) {
        var user = userService.getByEmail(email);
        return new UserPrincipal(user.id(), user.email(), user.password());
    }
}
