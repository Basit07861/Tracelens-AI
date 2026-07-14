package com.tracelens.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracelens.auth.dto.LoginRequest;
import com.tracelens.auth.dto.LoginResponse;
import com.tracelens.auth.dto.RegisterRequest;
import com.tracelens.auth.dto.UserResponse;
import com.tracelens.auth.service.AuthService;
import com.tracelens.common.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        UserResponse registeredUser =
                authService.register(request);

        ApiResponse<UserResponse> response =
                ApiResponse.success(
                        "Registration completed successfully",
                        registeredUser
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        LoginResponse loginResponse =
                authService.login(request);

        ApiResponse<LoginResponse> response =
                ApiResponse.success(
                        "Login completed successfully",
                        loginResponse
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {

        UserResponse currentUser =
                authService.getCurrentUser(
                        jwt.getSubject()
                );

        ApiResponse<UserResponse> response =
                ApiResponse.success(
                        "Current user retrieved successfully",
                        currentUser
                );

        return ResponseEntity.ok(response);
    }
}