-- ============================================
-- Sante Diagnostics LIMS - Database Schema
-- Run this file to set up the database
-- ============================================

-- USERS TABLE
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL CHECK (role IN ('SUPER_ADMIN', 'LAB_ATTENDANT', 'CUSTOMER')),
                       is_verified BOOLEAN DEFAULT FALSE,
                       force_password_change BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       created_by INTEGER REFERENCES users(id)
);

-- EMAIL VERIFICATION TOKENS
CREATE TABLE email_verifications (
                                     id SERIAL PRIMARY KEY,
                                     user_id INTEGER REFERENCES users(id),
                                     token VARCHAR(255) NOT NULL,
                                     expires_at TIMESTAMP NOT NULL,
                                     used BOOLEAN DEFAULT FALSE
);

-- TEST TYPES
CREATE TABLE test_types (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            category VARCHAR(50) NOT NULL CHECK (category IN ('Blood', 'Imaging', 'Biopsy', 'Other')),
                            price NUMERIC(10,2) NOT NULL,
                            tat_hours INTEGER NOT NULL,
                            result_format VARCHAR(20) NOT NULL CHECK (result_format IN ('numeric', 'text', 'PDF', 'image')),
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- TEST REQUESTS
CREATE