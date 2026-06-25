package com.tonem.boombeene.user.dto;

import com.tonem.boombeene.user.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {

    @Test
    void fromMapsUserWithoutPassword() {
        var user = new User("me@example.com", "encoded-password", "nickname");

        var response = UserResponse.from(user);

        assertThat(response.email()).isEqualTo("me@example.com");
        assertThat(response.nickname()).isEqualTo("nickname");
    }
}
