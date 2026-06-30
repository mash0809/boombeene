package com.tonem.boombeene.user.presentation;

import com.tonem.boombeene.user.application.UserService;
import com.tonem.boombeene.user.dto.SignupRequest;
import com.tonem.boombeene.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@Valid @RequestBody SignupRequest request) {
        userService.signup(request);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal(expression = "userId") Long userId) {
        return UserResponse.from(userService.getById(userId));
    }
}
