package com.aipromptgovernance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

/**
 * Implementation of AiImproverService.
 * Integrates with Google Gemini API when API key is configured.
 * Falls back to a placeholder response if API key is not set.
 */
@Service
public class AiImproverServiceImpl implements AiImproverService {

    private static final Logger LOGGER = Logger.getLogger(AiImproverServiceImpl.class.getName());

    @Value("${gemini.api.key:PLACEHOLDER_API_KEY}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public AiImproverServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String improvePrompt(String promptText) {
        // Check if API key is configured (not placeholder)
        if (apiKey == null || apiKey.isEmpty() || "PLACEHOLDER_API_KEY".equals(apiKey)) {
            LOGGER.warning("Gemini API key not configured. Using placeholder improvement.");
            return getPlaceholderImprovement(promptText);
        }

        try {
            return callGeminiApi(promptText);
        } catch (Exception e) {
            LOGGER.severe("Failed to call Gemini API: " + e.getMessage());
            return getPlaceholderImprovement(promptText);
        }
    }

    /**
     * Calls the Google Gemini API to improve the prompt.
     * Implementation details for Gemini API integration.
     */
    private String callGeminiApi(String promptText) {
        // Construct the request payload for Gemini API
        String requestBody = String.format(
            "{\"contents\":[{\"parts\":[{\"text\":\"Improve this AI prompt without changing its meaning. Make it clearer, more detailed and structured.\\n\\nOriginal Prompt:\\n%s\"}]}]}",
            promptText.replace("\"", "\\\"").replace("\n", "\\n")
        );

        String url = apiUrl + "?key=" + apiKey;

        // Make the API call
        String response = restTemplate.postForObject(url, requestBody, String.class);

        // Parse and return the improved text from response
        if (response != null && response.contains("\"text\"")) {
            // Basic parsing of Gemini response
            int textStart = response.indexOf("\"text\"") + 7;
            textStart = response.indexOf("\"", textStart) + 1;
            int textEnd = response.indexOf("\"", textStart);
            if (textStart > 0 && textEnd > textStart) {
                return response.substring(textStart, textEnd)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
            }
        }

        return getPlaceholderImprovement(promptText);
    }

    /**
     * Provides a placeholder improvement when API is not available.
     * This method can be replaced with actual AI API integration later.
     */
    private String getPlaceholderImprovement(String originalPrompt) {
        return "**Improved Version (Placeholder - Configure Gemini API key for AI-powered improvement)**\n\n"
             + "I understand you want me to help with the following prompt. Let me restructure and clarify it:\n\n"
             + "---\n\n"
             + "**Original Prompt:**\n" + originalPrompt + "\n\n"
             + "---\n\n"
             + "**Enhanced Prompt:**\n\n"
             + "Based on your original request, here is a more detailed and structured version:\n\n"
             + "1. **Objective:** Clearly define what you want to achieve with this prompt.\n"
             + "2. **Context:** Provide relevant background information to guide the AI.\n"
             + "3. **Specific Requirements:** List any specific constraints or preferences.\n"
             + "4. **Expected Output Format:** Specify how you want the response structured.\n\n"
             + "---\n\n"
             + "*Note: To enable real AI-powered improvements, please configure the 'gemini.api.key' property in application.properties with a valid Gemini API key.*";
    }
}

