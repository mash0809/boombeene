package com.tonem.boombeene.user.presentation;

import com.tonem.boombeene.support.AbstractIntegrationTest;
import com.tonem.boombeene.user.dto.LoginRequest;
import com.tonem.boombeene.user.dto.SignupRequest;
import com.tonem.boombeene.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/sql/001_create_users_table.sql")
class AuthApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void signupLoginMeLogoutFlowUsesSessionCookie() {
        var signupResponse = restTemplate.postForEntity(
                "/api/auth/signup",
                new SignupRequest("me@example.com", "password123", "nickname"),
                UserResponse.class
        );
        assertThat(signupResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(signupResponse.getBody().email()).isEqualTo("me@example.com");

        var loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest("me@example.com", "password123"),
                UserResponse.class
        );
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        var headers = new HttpHeaders();
        headers.put(HttpHeaders.COOKIE, extractSessionCookies(loginResponse.getHeaders()));

        var meResponse = restTemplate.exchange(
                "/api/auth/me",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                UserResponse.class
        );
        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(meResponse.getBody().nickname()).isEqualTo("nickname");

        var logoutResponse = restTemplate.postForEntity(
                "/api/auth/logout",
                new HttpEntity<>(headers),
                Void.class
        );
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void signupReturnsConflictWhenEmailAlreadyExists() {
        var request = new SignupRequest("me@example.com", "password123", "nickname");
        restTemplate.postForEntity("/api/auth/signup", request, UserResponse.class);

        var response = restTemplate.postForEntity("/api/auth/signup", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    private static List<String> extractSessionCookies(HttpHeaders headers) {
        return headers.getOrEmpty(HttpHeaders.SET_COOKIE).stream()
                .map(cookie -> cookie.split(";", 2)[0])
                .toList();
    }
}
