package com.aipromptgovernance.controller;

import com.aipromptgovernance.dto.PromptDto;
import com.aipromptgovernance.entity.Analysis;
import com.aipromptgovernance.entity.Prompt;
import com.aipromptgovernance.entity.User;
import com.aipromptgovernance.repository.AnalysisRepository;
import com.aipromptgovernance.service.PromptService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller for all prompt management operations:
 * CRUD, analysis, improvement, and history viewing.
 */
@Controller
@RequestMapping("/prompts")
public class PromptController {

    private final PromptService promptService;
    private final AnalysisRepository analysisRepository;

    // Available categories for dropdown
    private static final String[] CATEGORIES = {"Programming", "Education", "Business", "Marketing", "General"};

    public PromptController(PromptService promptService, AnalysisRepository analysisRepository) {
        this.promptService = promptService;
        this.analysisRepository = analysisRepository;
    }

    /**
     * List all prompts for the current user with pagination, search, and sorting.
     */
    @GetMapping
    public String listPrompts(@AuthenticationPrincipal User user,
                              Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "createdDate") String sortField,
                              @RequestParam(defaultValue = "desc") String sortDir,
                              @RequestParam(required = false) String keyword) {

        Page<Prompt> promptPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            promptPage = promptService.searchPromptsByUser(user, keyword, page, size);
            model.addAttribute("keyword", keyword);
        } else {
            promptPage = promptService.getPromptsByUser(user, page, size, sortField, sortDir);
        }

        model.addAttribute("prompts", promptPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promptPage.getTotalPages());
        model.addAttribute("totalItems", promptPage.getTotalElements());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "prompts/list";
    }

    /**
     * Show form to create a new prompt.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("prompt", new PromptDto());
        model.addAttribute("categories", CATEGORIES);
        return "prompts/form";
    }

    /**
     * Save a new prompt.
     */
    @PostMapping("/new")
    public String createPrompt(@Valid @ModelAttribute("prompt") PromptDto promptDto,
                               BindingResult result,
                               @AuthenticationPrincipal User user,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", CATEGORIES);
            return "prompts/form";
        }

        try {
            Prompt savedPrompt = promptService.createPrompt(promptDto, user);
            redirectAttributes.addFlashAttribute("successMessage", "Prompt created successfully!");
            return "redirect:/prompts/view/" + savedPrompt.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/prompts";
        }
    }

    /**
     * Show prompt details.
     */
    @GetMapping("/view/{id}")
    public String viewPrompt(@PathVariable Long id, Model model) {
        Prompt prompt = promptService.getPromptById(id);
        Optional<Analysis> analysis = analysisRepository.findByPrompt(prompt);

        model.addAttribute("prompt", prompt);
        model.addAttribute("analysis", analysis.orElse(null));

        return "prompts/view";
    }

    /**
     * Show form to edit an existing prompt.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Prompt prompt = promptService.getPromptById(id);
        PromptDto promptDto = new PromptDto();
        promptDto.setId(prompt.getId());
        promptDto.setTitle(prompt.getTitle());
        promptDto.setPromptText(prompt.getPromptText());
        promptDto.setCategory(prompt.getCategory());

        model.addAttribute("prompt", promptDto);
        model.addAttribute("categories", CATEGORIES);
        return "prompts/form";
    }

    /**
     * Update an existing prompt.
     */
    @PostMapping("/edit/{id}")
    public String updatePrompt(@PathVariable Long id,
                               @Valid @ModelAttribute("prompt") PromptDto promptDto,
                               BindingResult result,
                               @AuthenticationPrincipal User user,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", CATEGORIES);
            return "prompts/form";
        }

        try {
            promptService.updatePrompt(id, promptDto, user);
            redirectAttributes.addFlashAttribute("successMessage", "Prompt updated successfully!");
            return "redirect:/prompts/view/" + id;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/prompts";
        }
    }

    /**
     * Delete a prompt.
     */
    @GetMapping("/delete/{id}")
    public String deletePrompt(@PathVariable Long id,
                               @AuthenticationPrincipal User user,
                               RedirectAttributes redirectAttributes) {
        try {
            promptService.deletePrompt(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Prompt deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/prompts";
    }

    /**
     * Analyze a prompt for sensitive information.
     */
    @GetMapping("/analyze/{id}")
    public String analyzePrompt(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            Analysis analysis = promptService.analyzePrompt(id);
            redirectAttributes.addFlashAttribute("successMessage",
                "Analysis complete! Risk Level: " + analysis.getRiskLevel());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/prompts/view/" + id;
    }

    /**
     * Improve a prompt using AI.
     */
    @GetMapping("/improve/{id}")
    public String improvePrompt(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            String improvedPrompt = promptService.improvePrompt(id);
            redirectAttributes.addFlashAttribute("successMessage", "Prompt improved using AI!");
            redirectAttributes.addFlashAttribute("improvedPrompt", improvedPrompt);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/prompts/view/" + id;
    }
}

