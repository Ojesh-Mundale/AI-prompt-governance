package com.aipromptgovernance.repository;

import com.aipromptgovernance.entity.Prompt;
import com.aipromptgovernance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Prompt entity operations.
 */
@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {

    /**
     * Find all prompts by user with pagination.
     */
    Page<Prompt> findByUserOrderByCreatedDateDesc(User user, Pageable pageable);

    /**
     * Find all prompts by user.
     */
    List<Prompt> findByUserOrderByCreatedDateDesc(User user);

    /**
     * Count prompts by user.
     */
    long countByUser(User user);

    /**
     * Search prompts by user with title or promptText containing keyword.
     */
    @Query("SELECT p FROM Prompt p WHERE p.user = :user AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.promptText) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY p.createdDate DESC")
    Page<Prompt> searchByUser(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);

    /**
     * Find recent prompts by user.
     */
    List<Prompt> findTop5ByUserOrderByCreatedDateDesc(User user);
}

