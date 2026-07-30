package com.aipromptgovernance.repository;

import com.aipromptgovernance.entity.Analysis;
import com.aipromptgovernance.entity.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Analysis entity operations.
 */
@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    /**
     * Find analysis by associated prompt.
     */
    Optional<Analysis> findByPrompt(Prompt prompt);

    /**
     * Check if analysis exists for a prompt.
     */
    boolean existsByPrompt(Prompt prompt);

    /**
     * Delete analysis by prompt.
     */
    void deleteByPrompt(Prompt prompt);
}

