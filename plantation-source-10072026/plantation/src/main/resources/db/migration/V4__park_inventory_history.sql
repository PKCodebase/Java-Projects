-- V4__park_inventory_history.sql
-- Park-level tree allocation and master inventory audit history

-- Park-level tree allocation (carved out of master pool, consumed by slot inventories)
CREATE TABLE park_tree_inventory (
    park_inv_id    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    park_id        UUID        NOT NULL REFERENCES parks(park_id),
    species_id     UUID        NOT NULL REFERENCES tree_species(species_id),
    allocated_qty  INT         NOT NULL DEFAULT 0 CHECK (allocated_qty >= 0),
    allocated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_park_species_inv UNIQUE (park_id, species_id)
);

CREATE INDEX idx_pti_park    ON park_tree_inventory(park_id);
CREATE INDEX idx_pti_species ON park_tree_inventory(species_id);

-- Audit log for all master inventory operations
CREATE TABLE master_inventory_history (
    history_id    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    action        VARCHAR(30) NOT NULL CHECK (action IN ('STOCK_SET','PARK_ALLOCATED','PARK_UPDATED','PARK_REMOVED')),
    species_id    UUID        NOT NULL REFERENCES tree_species(species_id),
    qty           INT         NOT NULL,
    park_id       UUID        REFERENCES parks(park_id),
    performed_by  UUID        REFERENCES mcd_officials(official_id),
    performed_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_mih_species   ON master_inventory_history(species_id);
CREATE INDEX idx_mih_performed ON master_inventory_history(performed_at DESC);
