CREATE DATABASE IF NOT EXISTS bt_lt_web
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bt_lt_web;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    fullname VARCHAR(100) NULL,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(255) NULL,
    roleid INT NOT NULL DEFAULT 5,
    phone VARCHAR(20) NULL,
    createdDate DATE NULL,
    status INT NOT NULL DEFAULT 1
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Category (
    cate_id INT AUTO_INCREMENT PRIMARY KEY,
    cate_name VARCHAR(255) NOT NULL,
    icons VARCHAR(255) NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS remember_tokens (
    token_hash CHAR(64) PRIMARY KEY,
    user_id INT NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_remember_tokens_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_remember_tokens_expiry (expires_at),
    INDEX idx_remember_tokens_user (user_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS otp_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    otp_type VARCHAR(20) NOT NULL,
    expires_at DATETIME NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_otp_email_type (email, otp_type),
    INDEX idx_otp_expiry (expires_at)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Videos (
    video_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    poster VARCHAR(255) NULL,
    video_path VARCHAR(500) NULL,
    description VARCHAR(2000) NULL,
    views INT NOT NULL DEFAULT 0,
    active INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id INT NOT NULL,
    CONSTRAINT fk_videos_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_videos_user (user_id),
    INDEX idx_videos_created (created_at)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
