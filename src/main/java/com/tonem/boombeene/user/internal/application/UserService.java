package com.tonem.boombeene.user.internal.application;

import com.tonem.boombeene.common.exception.EntityNotFoundException;
import com.tonem.boombeene.user.internal.entity.User;
import com.tonem.boombeene.user.internal.dto.SignupRequest;
import com.tonem.boombeene.user.internal.dto.UserAuthDto;
import com.tonem.boombeene.user.internal.dto.UserDto;
import com.tonem.boombeene.user.internal.exception.DuplicateEmailException;
import com.tonem.boombeene.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequest request) {
        validateEmailNotDuplicated(request.email());

        String encodedPassword = passwordEncoder.encode(request.password());
        userRepository.save(User.create(request.email(), encodedPassword, request.nickname()));
    }

    private void validateEmailNotDuplicated(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        return UserDto.from(user);
    }

    @Transactional(readOnly = true)
    public UserAuthDto getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserAuthDto::from)
                .orElseThrow(() -> new EntityNotFoundException("User", email));
    }
}
