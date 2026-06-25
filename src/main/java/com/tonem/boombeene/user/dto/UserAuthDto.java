package com.tonem.boombeene.user.dto;

import com.tonem.boombeene.user.entity.User;

public record UserAuthDto(Long id, String email, String password) {

    public static UserAuthDto from(User user) {
        return new UserAuthDto(user.getId(), user.getEmail(), user.getPassword());
    }
}
