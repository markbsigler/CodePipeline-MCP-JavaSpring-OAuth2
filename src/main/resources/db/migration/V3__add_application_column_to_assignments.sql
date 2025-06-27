-- V3__add_application_column_to_assignments.sql
-- Adds the missing 'application' column to the assignments table for JPA compatibility

ALTER TABLE assignments ADD COLUMN application VARCHAR(255);
