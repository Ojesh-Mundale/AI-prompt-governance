package com.aipromptgovernance.service;

import com.aipromptgovernance.dto.PromptDto;
import com.aipromptgovernance.entity.Analysis;
import com.aipromptgovernance.entity.Prompt;
import com.aipromptgovernance.entity.User;
import com.aipromptgovernance.repository.AnalysisRepository;
import com.aipromptgovernance.repository.PromptRepository;
import com.aipromptgovernance.util.RiskAnalyzerUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of PromptService interface.
 * Handles all prompt management, risk analysis, and AI improvement operations.
 * Implements enterprise-grade secret masking: original prompts are scanned for
 * sensitive data and masked before being sent to the AI (Ollama).
 */
@Service
@Transactional
public class PromptServiceImpl implements PromptService {

    private final PromptRepository promptRepository;
    private final AnalysisRepository analysisRepository;
    private final AiImproverService aiImproverService;

    public PromptServiceImpl(PromptRepository promptRepository,
                             AnalysisRepository analysisRepository,
                             AiImproverService aiImproverService) {
        this.promptRepository = promptRepository;
        this.analysisRepository = analysisRepository;
        this.aiImproverService = aiImproverService;
    }

    @Override
    public Prompt createPrompt(PromptDto promptDto, User user) {
        Prompt prompt = new Prompt();
        prompt.setTitle(promptDto.getTitle());
        prompt.setPromptText(promptDto.getPromptText());
        prompt.setCategory(promptDto.getCategory());
        prompt.setCreatedDate(LocalDateTime.now());
        prompt.setStatus("PENDING");
        prompt.setUser(user);
        return promptRepository.save(prompt);
    }

    @Override
    public Prompt updatePrompt(Long id, PromptDto promptDto, User user) {
        Prompt prompt = promptRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prompt not found with id: " + id));

        // Verify ownership
        if (!prompt.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to update this prompt");
        }

        prompt.setTitle(promptDto.getTitle());
        prompt.setPromptText(promptDto.getPromptText());
        prompt.setCategory(promptDto.getCategory());

        // Reset status when prompt is updated
        prompt.setStatus("PENDING");
        prompt.setCreatedDate(LocalDateTime.now());

        // Delete existing analysis when prompt is updated
        if (analysisRepository.existsByPrompt(prompt)) {
            analysisRepository.deleteByPrompt(prompt);
        }

        return promptRepository.save(prompt);
    }

    @Override
    public void deletePrompt(Long id, User user) {
        Prompt prompt = promptRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prompt not found with id: " + id));

        // Verify ownership
        if (!prompt.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to delete this prompt");
        }

        // Delete associated analysis first
        if (analysisRepository.existsByPrompt(prompt)) {
            analysisRepository.deleteByPrompt(prompt);
        }

        promptRepository.delete(prompt);
    }

    @Override
    public Prompt getPromptById(Long id) {
        return promptRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prompt not found with id: " + id));
    }

    @Override
    public Page<Prompt> getPromptsByUser(User user, int page, int size, String sortField, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortField).ascending()
            : Sort.by(sortField).descending();

        // Default sort by createdDate descending if invalid field provided
        try {
            Prompt.class.getDeclaredField(sortField);
        } catch (NoSuchFieldException e) {
            sort = Sort.by("createdDate").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        return promptRepository.findByUserOrderByCreatedDateDesc(user, pageable);
    }

    @Override
    public Page<Prompt> searchPromptsByUser(User user, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return promptRepository.searchByUser(user, keyword, pageable);
    }

    @Override
    public Analysis analyzePrompt(Long promptId) {
        Prompt prompt = promptRepository.findById(promptId)
            .orElseThrow(() -> new RuntimeException("Prompt not found with id: " + promptId));

        // Run risk analysis AND masking using the enhanced utility
        RiskAnalyzerUtil.AnalysisResult result = RiskAnalyzerUtil.analyzeAndMask(prompt.getPromptText());

        // Create or update analysis
        Analysis analysis;
        if (analysisRepository.existsByPrompt(prompt)) {
            analysis = analysisRepository.findByPrompt(prompt).orElse(new Analysis());
        } else {
            analysis = new Analysis();
        }

        analysis.setRiskLevel(result.getRiskLevel());
        analysis.setReason(result.getReason());
        analysis.setMaskedPrompt(result.getMaskedPrompt());
        analysis.setDetectedSecretTypes(formatDetectedItems(result.getDetectedItems()));
        analysis.setAnalyzedDate(LocalDateTime.now());
        analysis.setPrompt(prompt);

        // Update prompt status based on risk level
        String status = result.getRiskLevel().toUpperCase();
        // Normalize: if "SAFE" store as "PENDING" to maintain existing UI badge logic
        if ("SAFE".equals(status)) {
            status = "PENDING";
        }
        prompt.setStatus(status);

        promptRepository.save(prompt);
        return analysisRepository.save(analysis);
    }

    @Override
    public String improvePrompt(Long promptId) {
        Prompt prompt = promptRepository.findById(promptId)
            .orElseThrow(() -> new RuntimeException("Prompt not found with id: " + promptId));

        // Step 1: Analyze and mask the original prompt for secrets
        RiskAnalyzerUtil.AnalysisResult result = RiskAnalyzerUtil.analyzeAndMask(prompt.getPromptText());

        // Step 2: Use the MASKED prompt to send to Ollama — secrets are never revealed
        String maskedPrompt = result.getMaskedPrompt();
        String improvedPrompt = aiImproverService.improvePrompt(maskedPrompt);

        // Step 3: Save analysis with all relevant data
        Analysis analysis;
        if (analysisRepository.existsByPrompt(prompt)) {
            analysis = analysisRepository.findByPrompt(prompt).orElse(new Analysis());
        } else {
            analysis = new Analysis();
        }

        analysis.setRiskLevel(result.getRiskLevel());
        analysis.setReason(result.getReason());
        analysis.setMaskedPrompt(maskedPrompt);
        analysis.setDetectedSecretTypes(formatDetectedItems(result.getDetectedItems()));
        analysis.setImprovedPrompt(improvedPrompt);
        analysis.setAnalyzedDate(LocalDateTime.now());
        analysis.setPrompt(prompt);

        // Update prompt status based on risk level
        String status = result.getRiskLevel().toUpperCase();
        if ("SAFE".equals(status)) {
            status = "PENDING";
        }
        prompt.setStatus(status);
        promptRepository.save(prompt);
        analysisRepository.save(analysis);

        return improvedPrompt;
    }

    /**
     * Formats detected items into a comma-separated string of secret types.
     * Example: "GitHub Token, Private Key, Database URL"
     */
    private String formatDetectedItems(List<RiskAnalyzerUtil.DetectedItem> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return items.stream()
                .map(RiskAnalyzerUtil.DetectedItem::getType)
                .collect(Collectors.joining(", "));
    }
}

