-- V3__zone_ward_master.sql
-- Zone and Ward master tables + migration of string zone columns to FK relationships

-- ── Zone master ──────────────────────────────────────────────────
CREATE TABLE zones (
    zone_id    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL UNIQUE,
    code       VARCHAR(20),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Ward master (depends on Zone) ───────────────────────────────
CREATE TABLE wards (
    ward_id      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    zone_id      UUID         NOT NULL REFERENCES zones(zone_id),
    name         VARCHAR(100) NOT NULL,
    ward_number  VARCHAR(20),
    is_active    BOOLEAN      NOT NULL DEFAULT true,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_ward_zone_name UNIQUE (zone_id, name)
);

CREATE INDEX idx_wards_zone_id ON wards(zone_id);

-- ── Seed zones from existing park zone strings ──────────────────
INSERT INTO zones (name)
SELECT DISTINCT zone FROM parks WHERE zone IS NOT NULL AND zone <> ''
ON CONFLICT (name) DO NOTHING;

-- ── Drop view that depends on parks.zone before altering column ─
DROP VIEW IF EXISTS slot_inventory_view;

-- ── Migrate parks: add FK columns, populate, drop string column ─
ALTER TABLE parks
    ADD COLUMN zone_id UUID REFERENCES zones(zone_id),
    ADD COLUMN ward_id UUID REFERENCES wards(ward_id);

UPDATE parks p
SET zone_id = z.zone_id
FROM zones z
WHERE z.name = p.zone;

ALTER TABLE parks DROP COLUMN zone;

CREATE INDEX idx_parks_zone_id ON parks(zone_id);
CREATE INDEX idx_parks_ward_id ON parks(ward_id);

-- ── Migrate officials: add FK columns, populate best-effort, drop string column ─
ALTER TABLE mcd_officials
    ADD COLUMN zone_id UUID REFERENCES zones(zone_id),
    ADD COLUMN ward_id UUID REFERENCES wards(ward_id);

UPDATE mcd_officials o
SET zone_id = z.zone_id
FROM zones z
WHERE z.name = o.zone;

ALTER TABLE mcd_officials DROP COLUMN zone;

CREATE INDEX idx_officials_zone_id ON mcd_officials(zone_id);
CREATE INDEX idx_officials_ward_id ON mcd_officials(ward_id);

-- ── Recreate view using zones join ──────────────────────────────
CREATE VIEW slot_inventory_view AS
SELECT
    sti.inventory_id,
    sti.slot_id,
    sti.species_id,
    sti.stock_qty,
    sti.reserved_qty,
    (sti.stock_qty - sti.reserved_qty) AS available_qty,
    ps.slot_date,
    ps.start_time,
    ps.end_time,
    ps.capacity,
    ps.booked_count,
    ps.status AS slot_status,
    p.park_id,
    p.name AS park_name,
    z.name AS zone,
    ts.common_name,
    ts.scientific_name,
    ts.emoji_code,
    ts.price,
    ts.category
FROM slot_tree_inventory sti
JOIN park_slots       ps  ON ps.slot_id   = sti.slot_id
JOIN parks            p   ON p.park_id    = ps.park_id
LEFT JOIN zones       z   ON z.zone_id    = p.zone_id
JOIN tree_species     ts  ON ts.species_id = sti.species_id;
