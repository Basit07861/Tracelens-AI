package com.tracelens.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "Email address is required")
        @Email(message = "Enter a valid email address")
        @Size(
                max = 150,
                message = "Email address cannot exceed 150 characters"
        )
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                max = 64,
                message = "Password cannot exceed 64 characters"
        )
        String password
) {
}