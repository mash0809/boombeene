package com.tonem.boombeene.user.api;

public interface UserFacade {

    void signup(SignupCommand command);

    UserAuthInfo getAuthUserByEmail(String email);

    UserInfo getUserById(Long userId);
}
