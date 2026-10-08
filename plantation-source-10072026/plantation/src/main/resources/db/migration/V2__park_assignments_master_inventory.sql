-- V2__park_assignments_master_inventory.sql
-- Park ↔ Official assignment mapping + Master tree inventory

CREATE TABLE park_official_assignments (
    assignment_id UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    park_id       UUID        NOT NULL REFERENCES parks(park_id),
    official_id   UUID        NOT NULL REFERENCES mcd_officials(official_id),
    assigned_by   UUID                 REFERENCES mcd_officials(official_id),
    assigned_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_park_official UNIQUE (park_id, official_id)
);

CREATE INDEX idx_poa_park     ON park_official_assignments(park_id);
CREATE INDEX idx_poa_official ON park_official_assignments(official_id);

-- Global master stock of tree species (pool from which slot inventories are drawn)
CREATE TABLE master_tree_inventory (
    master_inv_id UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    species_id    UUID        NOT NULL UNIQUE REFERENCES tree_species(species_id),
    total_qty     INT         NOT NULL DEFAULT 0 CHECK (total_qty >= 0),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
