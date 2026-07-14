package com.tracelens.auth.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracelens.auth.dto.RegisterRequest;
import com.tracelens.auth.dto.UserResponse;
import com.tracelens.exception.DuplicateEmailException;
import com.tracelens.user.entity.Role;
import com.tracelens.user.entity.User;
import com.tracelens.user.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException(
                    "An account already exists with this email address"
            );
        }

        User user = new User();

        user.setFullName(normalizeFullName(request.fullName()));
        user.setEmail(normalizedEmail);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setRole(Role.INVESTIGATOR);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeFullName(String fullName) {
        return fullName
                .trim()
                .replaceAll("\\s+", " ");
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}