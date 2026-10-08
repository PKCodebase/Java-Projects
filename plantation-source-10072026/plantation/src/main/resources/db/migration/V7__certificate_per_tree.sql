-- V7: Certificate table ko per-tree support ke liye update karo
-- Purana: 1 booking = 1 certificate (unique booking_id)
-- Naya:   1 booking = N certificates (ek per tree x quantity)

-- Step 1: Purana unique constraint hatao
ALTER TABLE certificates DROP CONSTRAINT IF EXISTS certificates_booking_id_key;

-- Step 2: Naye columns add karo
ALTER TABLE certificates
    ADD COLUMN IF NOT EXISTS booking_item_id UUID,
    ADD COLUMN IF NOT EXISTS tree_index     INTEGER NOT NULL DEFAULT 1;

-- Step 3: booking_item_id pe foreign key add karo
ALTER TABLE certificates
    ADD CONSTRAINT fk_cert_booking_item
        FOREIGN KEY (booking_item_id) REFERENCES booking_items(booking_item_id);

-- Step 4: Naya unique constraint — ek booking_item + tree_index = ek certificate
ALTER TABLE certificates
    ADD CONSTRAINT uq_cert_booking_item_index
        UNIQUE (booking_item_id, tree_index);
