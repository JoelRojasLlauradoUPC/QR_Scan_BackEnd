CREATE DATABASE IF NOT EXISTS qr_app
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

SET NAMES utf8mb4;
USE qr_app;

CREATE TABLE IF NOT EXISTS event_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    apellido VARCHAR(120) NOT NULL,
    correo_electronico VARCHAR(255) NOT NULL,
    tipo TINYINT NOT NULL,
    pmr BOOLEAN NOT NULL DEFAULT FALSE,
    hash VARCHAR(128) NOT NULL UNIQUE,
    numero_local INT NOT NULL,
    consumed BOOLEAN NOT NULL DEFAULT FALSE,
    consumed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_tickets_email (correo_electronico),
    INDEX idx_event_tickets_consumed (consumed),
    CHECK (tipo BETWEEN 0 AND 4)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;




