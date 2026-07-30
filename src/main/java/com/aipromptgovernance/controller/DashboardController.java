package com.aipromptgovernance.controller;

import com.aipromptgovernance.entity.Analysis;
import com.aipromptgovernance.entity.Prompt;
import com.aipromptgovernance.entity.User;
import com.aipromptgovernance.repository.AnalysisRepository;
import com.aipromptgovernance.repository.PromptRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller for the dashboard page.
 * Displays summary statistics and recent prompt history.
 */
@Controller
public class DashboardController {

    private final PromptRepository promptRepository;
    private final AnalysisRepository analysisRepository;

    public DashboardController(PromptRepository promptRepository, AnalysisRepository analysisRepository) {
        this.promptRepository = promptRepository;
        this.analysisRepository = analysisRepository;
    }

    /**
     * Display dashboard with prompt statistics and recent history.
     */
    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal User user, Model model) {
        // Get all prompts for the user
        List<Prompt> allPrompts = promptRepository.findByUserOrderByCreatedDateDesc(user);

        // Calculate statistics
        long totalPrompts = allPrompts.size();
        long safePrompts = allPrompts.stream()
            .filter(p -> "SAFE".equalsIgnoreCase(p.getStatus()))
            .count();
        long highRiskPrompts = allPrompts.stream()
            .filter(p -> "HIGH".equalsIgnoreCase(p.getStatus()))
            .count();

        // Count prompts that have been improved
        long improvedPrompts = allPrompts.stream()
            .filter(p -> {
                return analysisRepository.findByPrompt(p)
                    .map(a -> a.getImprovedPrompt() != null && !a.getImprovedPrompt().isEmpty())
                    .orElse(false);
            })
            .count();

        // Get recent 5 prompts
        List<Prompt> recentPrompts = promptRepository.findTop5ByUserOrderByCreatedDateDesc(user);

        model.addAttribute("totalPrompts", totalPrompts);
        model.addAttribute("safePrompts", safePrompts);
        model.addAttribute("highRiskPrompts", highRiskPrompts);
        model.addAttribute("improvedPrompts", improvedPrompts);
        model.addAttribute("recentPrompts", recentPrompts);

        return "dashboard";
    }
}

