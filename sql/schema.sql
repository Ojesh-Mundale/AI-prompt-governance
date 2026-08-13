-- ============================================================
-- AI Prompt Governance System - MySQL Database Schema
-- ============================================================

-- Create Database
CREATE DATABASE IF NOT EXISTS ai_prompt_governance
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ai_prompt_governance;

-- ============================================================
-- Users Table
-- Stores registered user information
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Prompts Table
-- Stores AI prompts created by users
-- ============================================================
CREATE TABLE IF NOT EXISTS prompts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    prompt_text TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    user_id BIGINT NOT NULL,
    INDEX idx_prompts_user_id (user_id),
    INDEX idx_prompts_status (status),
    INDEX idx_prompts_category (category),
    INDEX idx_prompts_created_date (created_date),
    CONSTRAINT fk_prompts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Analysis Table
-- Stores risk analysis results for prompts
-- ============================================================
CREATE TABLE IF NOT EXISTS analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    risk_level VARCHAR(20) NOT NULL,
    reason TEXT,
    analyzed_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    improved_prompt TEXT,
    masked_prompt TEXT,
    detected_secret_types TEXT,
    prompt_id BIGINT NOT NULL UNIQUE,
    INDEX idx_analysis_prompt_id (prompt_id),
    INDEX idx_analysis_risk_level (risk_level),
    CONSTRAINT fk_analysis_prompt FOREIGN KEY (prompt_id) REFERENCES prompts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Insert Default Admin User (password: admin123)
-- BCrypt encoded password
-- ============================================================
INSERT INTO users (name, email, password, role) VALUES
('Admin User', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN')
ON DUPLICATE KEY UPDATE name = VALUES(name);

