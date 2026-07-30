package com.aipromptgovernance.service;

import com.aipromptgovernance.dto.PromptDto;
import com.aipromptgovernance.entity.Analysis;
import com.aipromptgovernance.entity.Prompt;
import com.aipromptgovernance.entity.User;
import org.springframework.data.domain.Page;

/**
 * Service interface for prompt management operations.
 */
public interface PromptService {

    /**
     * Create a new prompt.
     */
    Prompt createPrompt(PromptDto promptDto, User user);

    /**
     * Update an existing prompt.
     */
    Prompt updatePrompt(Long id, PromptDto promptDto, User user);

    /**
     * Delete a prompt.
     */
    void deletePrompt(Long id, User user);

    /**
     * Get prompt by ID.
     */
    Prompt getPromptById(Long id);

    /**
     * Get paginated prompts for a user.
     */
    Page<Prompt> getPromptsByUser(User user, int page, int size, String sortField, String sortDir);

    /**
     * Search prompts for a user.
     */
    Page<Prompt> searchPromptsByUser(User user, String keyword, int page, int size);

    /**
     * Analyze prompt for sensitive information.
     */
    Analysis analyzePrompt(Long promptId);

    /**
     * Improve prompt using AI (Gemini API).
     */
    String improvePrompt(Long promptId);
}

