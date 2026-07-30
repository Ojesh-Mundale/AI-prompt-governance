package com.aipromptgovernance.service;

import com.aipromptgovernance.dto.ProfileDto;
import com.aipromptgovernance.dto.UserRegistrationDto;
import com.aipromptgovernance.entity.User;

/**
 * Service interface for user-related operations.
 */
public interface UserService {

    /**
     * Register a new user.
     */
    User registerUser(UserRegistrationDto registrationDto);

    /**
     * Find user by email.
     */
    User findByEmail(String email);

    /**
     * Update user profile.
     */
    User updateProfile(User user, ProfileDto profileDto);

    /**
     * Change user password.
     */
    void changePassword(User user, String currentPassword, String newPassword);
}

