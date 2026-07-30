package com.aipromptgovernance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for prompt create/edit form data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptDto {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Prompt text is required")
    @Size(max = 5000, message = "Prompt text must not exceed 5000 characters")
    private String promptText;

    @NotBlank(message = "Category is required")
    private String category;
}

