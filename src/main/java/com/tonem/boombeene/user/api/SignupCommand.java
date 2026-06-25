package com.tonem.boombeene.user.api;

public record SignupCommand(String email, String password, String nickname) {
}
