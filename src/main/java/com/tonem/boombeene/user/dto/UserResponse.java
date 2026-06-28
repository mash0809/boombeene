package com.tonem.boombeene.user.dto;

public record UserResponse(Long id, String email, String nickname, Integer point) {

    public static UserResponse from(UserDto user) {
        return new UserResponse(user.id(), user.email(), user.nickname(), user.point());
    }
}
