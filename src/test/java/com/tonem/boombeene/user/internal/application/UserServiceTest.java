package com.tonem.boombeene.user.internal.application;

import com.tonem.boombeene.common.exception.EntityNotFoundException;
import com.tonem.boombeene.user.internal.application.UserService;
import com.tonem.boombeene.user.internal.entity.User;
import com.tonem.boombeene.user.internal.dto.SignupRequest;
import com.tonem.boombeene.user.internal.exception.DuplicateEmailException;
import com.tonem.boombeene.user.internal.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 시 비밀번호를 암호화하여 사용자를 저장한다")
    void signupEncodesPasswordAndSavesUser() {
        var request = new SignupRequest("me@example.com", "password123", "nickname");
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.signup(request);

        var userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("me@example.com");
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(userCaptor.getValue().getNickname()).isEqualTo("nickname");
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 가입하면 예외를 던진다")
    void signupThrowsWhenEmailAlreadyExists() {
        var request = new SignupRequest("me@example.com", "password123", "nickname");
        when(userRepository.existsByEmail("me@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(DuplicateEmailException.class);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("저장 중 발생한 DataIntegrityViolationException을 그대로 다시 던진다")
    void signupRethrowsDataIntegrityViolationFromSave() {
        var request = new SignupRequest("me@example.com", "password123", "nickname");
        var exception = new DataIntegrityViolationException("other constraint");
        when(userRepository.existsByEmail("me@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenThrow(exception);

        assertThatThrownBy(() -> userService.signup(request))
                .isSameAs(exception);
        verify(userRepository, times(1)).existsByEmail("me@example.com");
    }

    @Test
    @DisplayName("ID로 조회 시 UserDto를 반환한다")
    void getByIdReturnsUserDto() {
        var user = User.create("me@example.com", "encoded-password", "nickname");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var userDto = userService.getById(1L);

        assertThat(userDto.id()).isNull();
        assertThat(userDto.email()).isEqualTo("me@example.com");
        assertThat(userDto.nickname()).isEqualTo("nickname");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 예외를 던진다")
    void getByIdThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("이메일로 조회 시 UserAuthDto를 반환한다")
    void getByEmailReturnsUserAuthDto() {
        var user = User.create("me@example.com", "encoded-password", "nickname");
        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));

        var userAuthDto = userService.getByEmail("me@example.com");

        assertThat(userAuthDto.id()).isNull();
        assertThat(userAuthDto.email()).isEqualTo("me@example.com");
        assertThat(userAuthDto.password()).isEqualTo("encoded-password");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 예외를 던진다")
    void getByEmailThrowsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail("missing@example.com"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
