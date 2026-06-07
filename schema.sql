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
CREATE TABLE test_requests (
                               id SERIAL PRIMARY KEY,
                               customer_id INTEGER REFERENCES users(id),
                               test_type_id INTEGER REFERENCES test_types(id),
                               status VARCHAR(30) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'COLLECTED', 'PROCESSING', 'AWAITING_VALIDATION', 'VALIDATED', 'COMPLETE')),
                               payment_status VARCHAR(10) DEFAULT 'UNPAID' CHECK (payment_status IN ('UNPAID', 'PAID')),
                               payment_reference VARCHAR(100),
                               ordered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               paid_at TIMESTAMP,
                               marked_paid_by INTEGER REFERENCES users(id)
);

-- SAMPLES
CREATE TABLE samples (
                         id SERIAL PRIMARY KEY,
                         test_request_id INTEGER REFERENCES test_requests(id),
                         status VARCHAR(30) DEFAULT 'PENDING_COLLECTION' CHECK (status IN ('PENDING_COLLECTION', 'COLLECTED', 'PROCESSING', 'AWAITING_VALIDATION', 'VALIDATED')),
                         collected_at TIMESTAMP,
                         processed_at TIMESTAMP,
                         validated_at TIMESTAMP,
                         updated_by INTEGER REFERENCES users(id)
);

-- RESULTS
CREATE TABLE results (
                         id SERIAL PRIMARY KEY,
                         test_request_id INTEGER REFERENCES test_requests(id),
                         uploaded_by INTEGER REFERENCES users(id),
                         verified_by INTEGER REFERENCES users(id),
                         result_data TEXT,
                         file_path VARCHAR(500),
                         file_type VARCHAR(10) CHECK (file_type IN ('PDF', 'image', 'text', 'numeric')),
                         is_verified BOOLEAN DEFAULT FALSE,
                         uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         verified_at TIMESTAMP,
                         notification_sent BOOLEAN DEFAULT FALSE
);

-- AUDIT LOG
CREATE TABLE audit_log (
                           id SERIAL PRIMARY KEY,
                           user_id INTEGER REFERENCES users(id),
                           action VARCHAR(100) NOT NULL,
                           target_table VARCHAR(50),
                           target_id INTEGER,
                           details TEXT,
                           performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- BANK DETAILS
CREATE TABLE bank_details (
                              id SERIAL PRIMARY KEY,
                              bank_name VARCHAR(100) NOT NULL,
                              account_name VARCHAR(100) NOT NULL,
                              account_number VARCHAR(20) NOT NULL,
                              is_active BOOLEAN DEFAULT TRUE
);

-- ============================================
-- DEFAULT DATA
-- ============================================

-- Default Super Admin (password: admin123)
INSERT INTO users (full_name, email, password_hash, role, is_verified, force_password_change)
VALUES (
           'Super Admin',
           'admin@santediagnostics.com',
           '$2a$10$fQ6oMW6hzjZl4RWI7d7QKeUDQa2JIxMlQsKSbWXnOmFUhLqM3FlVC',
           'SUPER_ADMIN',
           TRUE,
           FALSE
       );

-- Bank details
INSERT INTO bank_details (bank_name, account_name, account_number)
VALUES ('First Bank Nigeria', 'Sante Diagnostics Ltd', '3012345678');