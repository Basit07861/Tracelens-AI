package com.tracelens.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Full name is required")
        @Size(
                min = 2,
                max = 100,
                message = "Full name must contain between 2 and 100 characters"
        )
        String fullName,

        @NotBlank(message = "Email address is required")
        @Email(message = "Enter a valid email address")
        @Size(
                max = 150,
                message = "Email address cannot exceed 150 characters"
        )
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 64,
                message = "Password must contain between 8 and 64 characters"
        )
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Password must contain an uppercase letter, lowercase letter, number and special character"
        )
        String password
) {
}