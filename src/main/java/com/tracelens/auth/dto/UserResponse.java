package com.tracelens.auth.dto;

import java.time.Instant;

import com.tracelens.user.entity.Role;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        boolean active,
        Instant createdAt
) {
}