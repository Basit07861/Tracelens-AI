package com.tracelens.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}