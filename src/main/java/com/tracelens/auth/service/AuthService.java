package com.tracelens.auth.service;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracelens.auth.dto.LoginRequest;
import com.tracelens.auth.dto.LoginResponse;
import com.tracelens.auth.dto.RegisterRequest;
import com.tracelens.auth.dto.UserResponse;
import com.tracelens.exception.DuplicateEmailException;
import com.tracelens.exception.UserNotFoundException;
import com.tracelens.security.JwtService;
import com.tracelens.user.entity.Role;
import com.tracelens.user.entity.User;
import com.tracelens.user.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String normalizedEmail =
                normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(
                normalizedEmail
        )) {

            throw new DuplicateEmailException(
                    "An account already exists with this email address"
            );
        }

        User user = new User();

        user.setFullName(
                normalizeFullName(request.fullName())
        );
        user.setEmail(normalizedEmail);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setRole(Role.INVESTIGATOR);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        String normalizedEmail =
                normalizeEmail(request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.password()
                )
        );

        User user = findUserByEmail(normalizedEmail);

        String accessToken =
                jwtService.generateAccessToken(user);

        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                mapToResponse(user)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {

        User user = findUserByEmail(
                normalizeEmail(email)
        );

        return mapToResponse(user);
    }

    private User findUserByEmail(String email) {

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User account was not found"
                ));
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