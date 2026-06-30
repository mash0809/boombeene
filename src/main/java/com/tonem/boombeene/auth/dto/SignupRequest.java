package com.tonem.boombeene.auth.dto;

import com.tonem.boombeene.user.SignupCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank @Size(max = 50) String nickname
) {

    public SignupCommand toCommand() {
        return new SignupCommand(email, password, nickname);
    }
}
