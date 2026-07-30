package com.aipromptgovernance.controller;

import com.aipromptgovernance.dto.ProfileDto;
import com.aipromptgovernance.entity.User;
import com.aipromptgovernance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for user profile management.
 * Allows users to update their name and change password.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Display profile page.
     */
    @GetMapping
    public String showProfile(@AuthenticationPrincipal User user, Model model) {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setName(user.getName());
        model.addAttribute("profile", profileDto);
        return "profile";
    }

    /**
     * Update profile information and/or change password.
     */
    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("profile") ProfileDto profileDto,
                                BindingResult result,
                                @AuthenticationPrincipal User user,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            return "profile";
        }

        try {
            // Update name
            userService.updateProfile(user, profileDto);

            // Change password if new password is provided
            if (profileDto.getNewPassword() != null && !profileDto.getNewPassword().isEmpty()) {
                if (!profileDto.getNewPassword().equals(profileDto.getConfirmNewPassword())) {
                    result.rejectValue("confirmNewPassword", "error.profile", "New passwords do not match");
                    return "profile";
                }
                userService.changePassword(user, profileDto.getCurrentPassword(), profileDto.getNewPassword());
                redirectAttributes.addFlashAttribute("successMessage", "Profile and password updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            }

            return "redirect:/profile";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile";
        }
    }
}

