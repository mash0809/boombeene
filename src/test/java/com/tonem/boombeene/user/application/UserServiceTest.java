package com.tonem.boombeene.user.application;

import com.tonem.boombeene.common.exception.EntityNotFoundException;
import com.tonem.boombeene.point.api.PointFacade;
import com.tonem.boombeene.point.api.PointInfo;
import com.tonem.boombeene.user.entity.User;
import com.tonem.boombeene.user.dto.SignupRequest;
import com.tonem.boombeene.user.exception.DuplicateEmailException;
import com.tonem.boombeene.user.repository.UserRepository;
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

    @Mock
    private PointFacade pointFacade;

    @InjectMocks
    private UserService userService;

    @Test
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
    void signupThrowsWhenEmailAlreadyExists() {
        var request = new SignupRequest("me@example.com", "password123", "nickname");
        when(userRepository.existsByEmail("me@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(DuplicateEmailException.class);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
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
    void getByIdReturnsUserDto() {
        var user = User.create("me@example.com", "encoded-password", "nickname");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(pointFacade.getByUserId(1L)).thenReturn(new PointInfo(10));

        var userDto = userService.getById(1L);

        assertThat(userDto.id()).isNull();
        assertThat(userDto.email()).isEqualTo("me@example.com");
        assertThat(userDto.nickname()).isEqualTo("nickname");
    }

    @Test
    void getByIdThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByEmailReturnsUserAuthDto() {
        var user = User.create("me@example.com", "encoded-password", "nickname");
        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));

        var userAuthDto = userService.getByEmail("me@example.com");

        assertThat(userAuthDto.id()).isNull();
        assertThat(userAuthDto.email()).isEqualTo("me@example.com");
        assertThat(userAuthDto.password()).isEqualTo("encoded-password");
    }

    @Test
    void getByEmailThrowsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail("missing@example.com"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
