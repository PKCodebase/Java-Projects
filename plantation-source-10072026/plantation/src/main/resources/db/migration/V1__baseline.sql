-- V1__initial_schema.sql
-- MCD Green Plantation Portal — Initial schema
-- This file delegates to the canonical DB script.
-- Run mcd_plantation_db.sql directly for the first time,
-- then Flyway tracks subsequent migrations from V2 onwards.
-- 
-- For first-time Flyway setup against an existing database:
--   spring.flyway.baseline-on-migrate=true  (already set in application.yml)

-- Verify core tables exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'parks') THEN
        RAISE EXCEPTION 'Core schema not initialised. Run mcd_plantation_db.sql first.';
    END IF;
END $$;
