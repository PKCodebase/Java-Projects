-- V6: Occasions master table, species-occasion mapping, booking occasion FK

CREATE TABLE occasions (
    occasion_id    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(100) NOT NULL UNIQUE,
    description    TEXT,
    emoji_code     VARCHAR(10)  NOT NULL DEFAULT '🌿',
    display_order  SMALLINT     NOT NULL DEFAULT 0,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE tree_species_occasions (
    species_id  UUID NOT NULL REFERENCES tree_species(species_id) ON DELETE CASCADE,
    occasion_id UUID NOT NULL REFERENCES occasions(occasion_id)   ON DELETE CASCADE,
    PRIMARY KEY (species_id, occasion_id)
);

ALTER TABLE bookings
    ADD COLUMN occasion_id UUID REFERENCES occasions(occasion_id) ON DELETE SET NULL;

-- Seed 10 default occasions
INSERT INTO occasions (name, description, emoji_code, display_order) VALUES
    ('Birthday',            'Celebrate a birthday by planting a tree that grows with the person', '🎂', 1),
    ('Anniversary',         'Mark a special anniversary with a tree that endures through the years', '💍', 2),
    ('Memorial / Tribute',  'Honour the memory of a loved one with a living tribute', '🕊️', 3),
    ('Wedding',             'Commemorate a new union with a tree that flourishes together', '👰', 4),
    ('New Born',            'Welcome a new life by planting a tree in their honour', '👶', 5),
    ('Environmental Day',   'Contribute to the environment on World Environment Day or similar events', '🌍', 6),
    ('Festival',            'Celebrate a festival season by giving back to nature', '🎉', 7),
    ('Corporate CSR',       'Fulfil corporate social responsibility with a green initiative', '🏢', 8),
    ('School / College',    'Mark an academic milestone or institutional event', '🎓', 9),
    ('Public Ceremony',     'Commemorate a public or government event with plantation', '🏛️', 10);
