package com.aipromptgovernance.service;

/**
 * Service interface for AI prompt improvement.
 * Implement this interface to integrate with any AI API (e.g., Gemini, ChatGPT).
 * Default implementation provides a placeholder until API key is configured.
 */
public interface AiImproverService {

    /**
     * Improves the given prompt text by making it clearer, more detailed, and structured.
     *
     * @param promptText the original prompt text to improve
     * @return the improved prompt text
     */
    String improvePrompt(String promptText);
}

