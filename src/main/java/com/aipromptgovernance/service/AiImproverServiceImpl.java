package com.aipromptgovernance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of AiImproverService.
 * Integrates with Ollama (Llama 3) to improve AI prompts.
 */
@Service
public class AiImproverServiceImpl implements AiImproverService {

    private static final Logger LOGGER = Logger.getLogger(AiImproverServiceImpl.class.getName());

    @Value("${ollama.api.url}")
    private String ollamaUrl;

    @Value("${ollama.api.model}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiImproverServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String improvePrompt(String promptText) {
        try {
            return callOllamaApi(promptText);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to call Ollama API: " + e.getMessage(), e);
            return getPlaceholderImprovement(promptText);
        }
    }

    /**
     * Calls the Ollama API to improve the prompt using the configured model.
     */
    private String callOllamaApi(String promptText) {
        String systemPrompt = "Improve this AI prompt without changing its meaning. Make it clearer, more detailed and structured.";
        String fullPrompt = systemPrompt + "\n\nOriginal Prompt:\n" + promptText;

        String escapedPrompt = fullPrompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        String requestBody = String.format(
                "{\"model\":\"%s\",\"prompt\":\"%s\",\"stream\":false}",
                model,
                escapedPrompt
        );

        LOGGER.info("Calling Ollama API at: " + ollamaUrl + " with model: " + model);

        String response = restTemplate.postForObject(ollamaUrl, requestBody, String.class);

        if (response != null) {
            try {
                JsonNode root = objectMapper.readTree(response);
                JsonNode responseField = root.get("response");
                if (responseField != null && !responseField.isNull()) {
                    return responseField.asText();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to parse Ollama response JSON", e);
            }
        }

        LOGGER.warning("Ollama returned empty or unparseable response. Using placeholder.");
        return getPlaceholderImprovement(promptText);
    }

    /**
     * Provides a placeholder improvement when Ollama API is not available.
     */
    private String getPlaceholderImprovement(String originalPrompt) {
        return "**Improved Version (Placeholder - Ollama API unavailable)**\n\n"
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
             + "*Note: To enable real AI-powered improvements, please ensure Ollama is running locally with the '" + model + "' model pulled.*";
    }
}
