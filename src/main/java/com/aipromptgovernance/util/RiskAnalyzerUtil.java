package com.aipromptgovernance.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for analyzing prompts for sensitive information.
 * Uses Java Regex patterns to detect various types of sensitive data.
 */
public class RiskAnalyzerUtil {

    // Regex patterns for detecting sensitive information
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("\\b(\\+?\\d{1,3}[-.]?)?\\(?\\d{3}\\)?[-.]?\\d{3}[-.]?\\d{4}\\b");

    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("(?i)\\b(password|passwd|pwd)\\s*[:=]\\s*\\S+\\b");

    private static final Pattern API_KEY_PATTERN =
        Pattern.compile("(?i)\\b(api[_-]?key|apikey|api[_-]?secret)\\s*[:=]\\s*\\S+\\b");

    private static final Pattern CREDIT_CARD_PATTERN =
        Pattern.compile("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12})\\b");

    private static final Pattern PAN_PATTERN =
        Pattern.compile("\\b[A-Z]{5}[0-9]{4}[A-Z]{1}\\b");

    private static final Pattern AADHAAR_PATTERN =
        Pattern.compile("\\b[2-9]{1}[0-9]{11}\\b");

    private static final Pattern SECRET_KEY_PATTERN =
        Pattern.compile("(?i)\\b(secret|secret[_-]?key|private[_-]?key)\\s*[:=]\\s*\\S+\\b");

    /**
     * Represents a detected sensitive item with its type and description.
     */
    public static class DetectedItem {
        private final String type;
        private final String description;

        public DetectedItem(String type, String description) {
            this.type = type;
            this.description = description;
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
    }

    /**
     * Result of a risk analysis containing risk level, reason, and detected items.
     */
    public static class AnalysisResult {
        private final String riskLevel;
        private final String reason;
        private final List<DetectedItem> detectedItems;

        public AnalysisResult(String riskLevel, String reason, List<DetectedItem> detectedItems) {
            this.riskLevel = riskLevel;
            this.reason = reason;
            this.detectedItems = detectedItems;
        }

        public String getRiskLevel() { return riskLevel; }
        public String getReason() { return reason; }
        public List<DetectedItem> getDetectedItems() { return detectedItems; }
    }

    /**
     * Analyzes the given prompt text for sensitive information.
     *
     * @param promptText the prompt text to analyze
     * @return AnalysisResult containing risk level, reason, and detected items
     */
    public static AnalysisResult analyze(String promptText) {
        List<DetectedItem> detectedItems = new ArrayList<>();

        // Check for email addresses
        if (EMAIL_PATTERN.matcher(promptText).find()) {
            detectedItems.add(new DetectedItem("Email Address", "Prompt contains email address(es)"));
        }

        // Check for phone numbers
        if (PHONE_PATTERN.matcher(promptText).find()) {
            detectedItems.add(new DetectedItem("Phone Number", "Prompt contains phone number(s)"));
        }

        // Check for passwords
        if (PASSWORD_PATTERN.matcher(promptText).find()) {
            detectedItems.add(new DetectedItem("Password", "Prompt may contain password(s)"));
        }

        // Check for API keys
        if (API_KEY_PATTERN.matcher(promptText).find()) {
            detectedItems.add(new DetectedItem("API Key", "Prompt contains API key(s) or API secret(s)"));
        }

        // Check for credit card numbers
        if (CREDIT_CARD_PATTERN.matcher(promptText).find()) {
            detectedItems.add(new DetectedItem("Credit Card Number", "Prompt contains credit card number(s)"));
        }

        // Check for PAN numbers (Indian tax ID)
        if (PAN_PATTERN.matcher(promptText).find()) {
            detectedItems.add(new DetectedItem("PAN Number", "Prompt contains PAN card number(s)"));
        }

        // Check for Aadhaar numbers (Indian ID)
        if (AADHAAR_PATTERN.matcher(promptText).find()) {
            detectedItems.add(new DetectedItem("Aadhaar Number", "Prompt contains Aadhaar number(s)"));
        }

        // Check for secret keys
        if (SECRET_KEY_PATTERN.matcher(promptText).find()) {
            detectedItems.add(new DetectedItem("Secret Key", "Prompt contains secret key(s)"));
        }

        // Determine risk level based on detected items
        String riskLevel;
        String reason;

        if (detectedItems.isEmpty()) {
            riskLevel = "Safe";
            reason = "No sensitive information detected in the prompt.";
        } else if (detectedItems.size() <= 2) {
            riskLevel = "Medium";
            reason = "Sensitive information detected: " + formatDetectedItems(detectedItems);
        } else {
            riskLevel = "High";
            reason = "Multiple sensitive information detected: " + formatDetectedItems(detectedItems);
        }

        return new AnalysisResult(riskLevel, reason, detectedItems);
    }

    /**
     * Formats detected items into a readable string.
     */
    private static String formatDetectedItems(List<DetectedItem> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(items.get(i).getType());
        }
        return sb.toString();
    }
}

