package com.tonem.boombeene.user.application;

import com.tonem.boombeene.user.domain.User;
import com.tonem.boombeene.user.dto.SignupRequest;
import com.tonem.boombeene.user.exception.DuplicateEmailException;
import com.tonem.boombeene.user.repository.UserRepository;
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
    public User signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        var encodedPassword = passwordEncoder.encode(request.password());
        return userRepository.save(new User(request.email(), encodedPassword, request.nickname()));
    }
}
