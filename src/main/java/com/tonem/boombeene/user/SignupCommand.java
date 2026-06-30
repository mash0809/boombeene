package com.tonem.boombeene.user;

public record SignupCommand(String email, String password, String nickname) {
}
