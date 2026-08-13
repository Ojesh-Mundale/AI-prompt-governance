package com.aipromptgovernance.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enterprise-grade utility for detecting and masking sensitive information in prompts.
 * Scans text using regex patterns, replaces secrets with placeholders,
 * and returns risk assessment with detected secret types.
 */
public class RiskAnalyzerUtil {

    // ============================================================
    // Secret Pattern Definitions (name -> (regex, placeholder, risk))
    // ============================================================

    private static final List<SecretPattern> SECRET_PATTERNS = List.of(
        // --- HIGH risk secrets ---
        new SecretPattern(
            "GitHub Token",
            "(?i)(ghp_|gho_|ghu_|ghs_|ghr_)[A-Za-z0-9_]{36,}",
            "<GITHUB_TOKEN>",
            "HIGH"
        ),
        new SecretPattern(
            "OpenAI API Key",
            "(?i)(sk-[A-Za-z0-9]{20,})",
            "<OPENAI_API_KEY>",
            "HIGH"
        ),
        new SecretPattern(
            "AWS Access Key",
            "(?i)(AKIA[A-Z0-9]{16})",
            "<AWS_ACCESS_KEY>",
            "HIGH"
        ),
        new SecretPattern(
            "AWS Secret Key",
            "(?i)((?![A-Za-z0-9/+=])[A-Za-z0-9/+=]{40}(?![A-Za-z0-9/+=]))",
            "<AWS_SECRET_KEY>",
            "HIGH"
        ),
        new SecretPattern(
            "JWT Token",
            "\\beyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\b",
            "<JWT_TOKEN>",
            "HIGH"
        ),
        new SecretPattern(
            "Bearer Token",
            "(?i)(bearer\\s+)[A-Za-z0-9_\\-.:=+/]{20,}",
            "<BEARER_TOKEN>",
            "HIGH"
        ),
        new SecretPattern(
            "Private Key",
            "-----BEGIN[\\sA-Z]*PRIVATE KEY-----",
            "<PRIVATE_KEY>",
            "HIGH"
        ),
        new SecretPattern(
            "SSH Private Key",
            "-----BEGIN[\\sA-Z]*RSA[\\sA-Z]*PRIVATE KEY-----",
            "<SSH_PRIVATE_KEY>",
            "HIGH"
        ),
        new SecretPattern(
            "Database URL",
            "(?i)((?:jdbc|mysql|postgresql|mongodb|redis)://[^\\s\"'<>]+)",
            "<DATABASE_URL>",
            "HIGH"
        ),
        new SecretPattern(
            "Password",
            "(?i)(password|passwd|pwd)\\s*[:=]\\s*\\S{4,}",
            "<PASSWORD>",
            "HIGH"
        ),
        new SecretPattern(
            "Connection String",
            "(?i)(connection\\s*(?:string|str)|connstr)\\s*[:=]\\s*[^\\s\"'<>]{10,}",
            "<CONNECTION_STRING>",
            "HIGH"
        ),

        // --- MEDIUM risk secrets ---
        new SecretPattern(
            "Email Address",
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b",
            "<EMAIL>",
            "MEDIUM"
        ),
        new SecretPattern(
            "Phone Number",
            "\\b(\\+?\\d{1,3}[-.]?)?\\(?\\d{3}\\)?[-.]?\\d{3}[-.]?\\d{4}\\b",
            "<PHONE_NUMBER>",
            "MEDIUM"
        ),
        new SecretPattern(
            "IP Address",
            "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b",
            "<IP_ADDRESS>",
            "MEDIUM"
        ),
        new SecretPattern(
            "Internal URL",
            "(?i)(https?://(?:localhost|192\\.168\\.\\d{1,3}\\.\\d{1,3}|10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|172\\.(?:1[6-9]|2\\d|3[01])\\.\\d{1,3}\\.\\d{1,3}|127\\.0\\.0\\.1)[^\\s\"'<>]*)",
            "<INTERNAL_URL>",
            "MEDIUM"
        )
    );

    /**
     * Holds regex pattern, placeholder name, and risk level for a secret type.
     */
    private static record SecretPattern(String name, String regex, String placeholder, String riskLevel) {
        Pattern compiledPattern() {
            return Pattern.compile(regex);
        }
    }

    /**
     * Represents a detected sensitive item with its type and risk level.
     */
    public static class DetectedItem {
        private final String type;
        private final String riskLevel;
        private final String description;

        public DetectedItem(String type, String riskLevel, String description) {
            this.type = type;
            this.riskLevel = riskLevel;
            this.description = description;
        }

        public String getType() { return type; }
        public String getRiskLevel() { return riskLevel; }
        public String getDescription() { return description; }
    }

    /**
     * Result of a risk analysis containing risk level, reason,
     * detected items, and the masked prompt text.
     */
    public static class AnalysisResult {
        private final String riskLevel;
        private final String reason;
        private final List<DetectedItem> detectedItems;
        private final String maskedPrompt;

        public AnalysisResult(String riskLevel, String reason,
                              List<DetectedItem> detectedItems,
                              String maskedPrompt) {
            this.riskLevel = riskLevel;
            this.reason = reason;
            this.detectedItems = detectedItems;
            this.maskedPrompt = maskedPrompt;
        }

        public String getRiskLevel() { return riskLevel; }
        public String getReason() { return reason; }
        public List<DetectedItem> getDetectedItems() { return detectedItems; }
        public String getMaskedPrompt() { return maskedPrompt; }
    }

    /**
     * Analyzes the given text for sensitive information AND returns a masked version
     * where all secrets are replaced with placeholders.
     *
     * @param text the input text to analyze and mask
     * @return AnalysisResult containing risk level, reason, detected items, and masked prompt
     */
    public static AnalysisResult analyzeAndMask(String text) {
        if (text == null || text.isEmpty()) {
            return new AnalysisResult("SAFE", "No content to analyze.",
                    List.of(), text);
        }

        List<DetectedItem> detectedItems = new ArrayList<>();
        StringBuilder maskedText = new StringBuilder(text);

        // Track unique secret types detected
        Map<String, String> uniqueTypes = new LinkedHashMap<>();

        for (SecretPattern sp : SECRET_PATTERNS) {
            Pattern pattern = sp.compiledPattern();
            Matcher matcher = pattern.matcher(maskedText.toString());

            while (matcher.find()) {
                String match = matcher.group();
                // Skip IP-like numbers that are too short (e.g. version numbers)
                if ("IP Address".equals(sp.name) && match.startsWith("0")) {
                    continue;
                }
                if (!uniqueTypes.containsKey(sp.name)) {
                    uniqueTypes.put(sp.name, sp.riskLevel);
                    String desc = switch (sp.name) {
                        case "GitHub Token" -> "Prompt contains GitHub access token(s)";
                        case "OpenAI API Key" -> "Prompt contains OpenAI API key(s)";
                        case "AWS Access Key" -> "Prompt contains AWS access key(s)";
                        case "AWS Secret Key" -> "Prompt contains AWS secret key(s)";
                        case "JWT Token" -> "Prompt contains JWT token(s)";
                        case "Bearer Token" -> "Prompt contains Bearer authentication token(s)";
                        case "Private Key" -> "Prompt contains private key(s)";
                        case "SSH Private Key" -> "Prompt contains SSH private key(s)";
                        case "Database URL" -> "Prompt contains database connection URL(s)";
                        case "Password" -> "Prompt contains password(s)";
                        case "Connection String" -> "Prompt contains connection string(s)";
                        case "Email Address" -> "Prompt contains email address(es)";
                        case "Phone Number" -> "Prompt contains phone number(s)";
                        case "IP Address" -> "Prompt contains IP address(es)";
                        case "Internal URL" -> "Prompt contains internal URL(s)";
                        default -> "Prompt contains " + sp.name.toLowerCase() + "(s)";
                    };
                    detectedItems.add(new DetectedItem(sp.name, sp.riskLevel, desc));
                }

                // Replace the matched secret with placeholder
                int start = matcher.start();
                int end = matcher.end();
                maskedText.replace(start, end, sp.placeholder);
                // Reset matcher to scan from after the placeholder
                matcher = pattern.matcher(maskedText.toString());
            }
        }

        // Determine overall risk level
        String riskLevel;
        String reason;
        boolean hasHigh = false;
        boolean hasMedium = false;

        for (DetectedItem item : detectedItems) {
            if ("HIGH".equals(item.getRiskLevel())) {
                hasHigh = true;
            } else if ("MEDIUM".equals(item.getRiskLevel())) {
                hasMedium = true;
            }
        }

        if (detectedItems.isEmpty()) {
            riskLevel = "SAFE";
            reason = "No sensitive information detected in the prompt.";
        } else if (hasHigh) {
            riskLevel = "HIGH";
            reason = "High risk sensitive information detected: " + formatDetectedTypes(detectedItems);
        } else if (hasMedium) {
            riskLevel = "MEDIUM";
            reason = "Medium risk information detected: " + formatDetectedTypes(detectedItems);
        } else {
            riskLevel = "SAFE";
            reason = "No sensitive information detected.";
        }

        return new AnalysisResult(riskLevel, reason, detectedItems, maskedText.toString());
    }

    /**
     * Legacy analyze method - performs analysis without masking.
     * Kept for backward compatibility but delegates to analyzeAndMask.
     */
    public static AnalysisResult analyze(String promptText) {
        AnalysisResult result = analyzeAndMask(promptText);
        // Return result with original text as maskedPrompt for backward compat
        return new AnalysisResult(
                result.getRiskLevel(),
                result.getReason(),
                result.getDetectedItems(),
                promptText
        );
    }

    /**
     * Formats detected items into a readable string of types.
     */
    private static String formatDetectedTypes(List<DetectedItem> items) {
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

