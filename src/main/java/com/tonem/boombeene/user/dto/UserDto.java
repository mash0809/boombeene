package com.tonem.boombeene.user.dto;

import com.tonem.boombeene.user.domain.entity.User;

public record UserDto(Long id, String email, String nickname) {

    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getNickname());
    }
}
