package com.tonem.boombeene.user.application;

import com.tonem.boombeene.common.exception.EntityNotFoundException;
import com.tonem.boombeene.user.domain.entity.User;
import com.tonem.boombeene.user.dto.SignupRequest;
import com.tonem.boombeene.user.dto.UserAuthDto;
import com.tonem.boombeene.user.dto.UserDto;
import com.tonem.boombeene.user.exception.DuplicateEmailException;
import com.tonem.boombeene.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
        String encodedPassword = passwordEncoder.encode(request.password());
        try {
            userRepository.save(User.create(request.email(), encodedPassword, request.nickname()));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException();
        }
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long userId) {
        return userRepository.findById(userId)
                .map(UserDto::from)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
    }

    @Transactional(readOnly = true)
    public UserAuthDto getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserAuthDto::from)
                .orElseThrow(() -> new EntityNotFoundException("User", email));
    }
}
