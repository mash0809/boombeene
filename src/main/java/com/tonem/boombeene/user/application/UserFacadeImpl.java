package com.tonem.boombeene.user.application;

import com.tonem.boombeene.user.api.DuplicateUserEmailException;
import com.tonem.boombeene.user.api.SignupCommand;
import com.tonem.boombeene.user.api.UserAuthInfo;
import com.tonem.boombeene.user.api.UserFacade;
import com.tonem.boombeene.user.api.UserInfo;
import com.tonem.boombeene.user.dto.SignupRequest;
import com.tonem.boombeene.user.exception.DuplicateEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class UserFacadeImpl implements UserFacade {

    private final UserService userService;

    @Override
    @Transactional
    public void signup(SignupCommand command) {
        try {
            userService.signup(new SignupRequest(command.email(), command.password(), command.nickname()));
        } catch (DuplicateEmailException e) {
            throw new DuplicateUserEmailException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserAuthInfo getAuthUserByEmail(String email) {
        var user = userService.getByEmail(email);
        return new UserAuthInfo(user.id(), user.email(), user.password());
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfo getUserById(Long userId) {
        var user = userService.getById(userId);
        return new UserInfo(user.id(), user.email(), user.nickname());
    }
}
