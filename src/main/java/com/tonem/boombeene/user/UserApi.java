package com.tonem.boombeene.user;

import com.tonem.boombeene.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserApi {

    private final UserService userService;

    public UserAuthInfo getAuthUserByEmail(String email) {
        var user = userService.getByEmail(email);
        return new UserAuthInfo(user.id(), user.email(), user.password());
    }
}
