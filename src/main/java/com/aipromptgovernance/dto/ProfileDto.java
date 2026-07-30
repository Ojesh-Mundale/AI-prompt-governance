package com.aipromptgovernance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for profile update form data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Size(min = 6, max = 100, message = "New password must be between 6 and 100 characters")
    private String newPassword;

    private String confirmNewPassword;
}

