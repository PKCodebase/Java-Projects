-- Add WAITING value to the booking_status PostgreSQL enum
-- Used when a physical-presence citizen has arrived and is ready to plant
ALTER TYPE booking_status ADD VALUE IF NOT EXISTS 'WAITING' AFTER 'SCHEDULED';
