package com.tonem.boombeene.user.internal.dto;

import com.tonem.boombeene.user.internal.entity.User;

public record UserDto(Long id, String email, String nickname, Integer point) {

    public static UserDto from(User user, Integer point) {
        return new UserDto(user.getId(), user.getEmail(), user.getNickname(), point);
    }
}
