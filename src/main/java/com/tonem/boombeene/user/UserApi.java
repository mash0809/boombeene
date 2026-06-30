package com.tonem.boombeene.user;

import com.tonem.boombeene.user.application.UserService;
import com.tonem.boombeene.user.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserApi {

    private final UserService userService;

    public void signup(SignupCommand command) {
        try {
            userService.signup(new SignupRequest(command.email(), command.password(), command.nickname()));
        } catch (DuplicateEmailException e) {
            throw new DuplicateEmailException();
        }
    }

    public UserAuthInfo getAuthUserByEmail(String email) {
        var user = userService.getByEmail(email);
        return new UserAuthInfo(user.id(), user.email(), user.password());
    }

    public UserInfo getUserById(Long userId) {
        var user = userService.getById(userId);
        return new UserInfo(user.id(), user.email(), user.nickname());
    }
}
