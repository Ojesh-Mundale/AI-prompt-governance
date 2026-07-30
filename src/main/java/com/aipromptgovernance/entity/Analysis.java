package com.aipromptgovernance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Analysis entity storing the results of prompt risk analysis.
 * Each analysis is linked to a specific prompt.
 */
@Entity
@Table(name = "analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String riskLevel;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private LocalDateTime analyzedDate = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String improvedPrompt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    @PrePersist
    protected void onCreate() {
        if (analyzedDate == null) {
            analyzedDate = LocalDateTime.now();
        }
    }
}

