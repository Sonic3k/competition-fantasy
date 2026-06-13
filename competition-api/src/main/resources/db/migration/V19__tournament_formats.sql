-- =====================================================================
-- V19: Tournament format engine — schema foundation
--   * matches can hold TBD knockout slots (nullable teams) + KO outcome
--   * match_slot: bracket wiring (where each KO participant comes from)
--   * seasons: per-season tournament format (preset key + resolved JSON)
--   * standings: persisted final position (rank) within its group
-- Purely additive except relaxing NOT NULL on match teams.
-- Existing data (Othelose, Wonder Empires) stays fully intact/viewable.
-- =====================================================================

-- --- matches: allow knockout slots before teams are known + KO result --
ALTER TABLE matches ALTER COLUMN home_team_id DROP NOT NULL;
ALTER TABLE matches ALTER COLUMN away_team_id DROP NOT NULL;

ALTER TABLE matches ADD COLUMN home_penalties INTEGER;
ALTER TABLE matches ADD COLUMN away_penalties INTEGER;
ALTER TABLE matches ADD COLUMN winner_team_id UUID REFERENCES teams(id) ON DELETE SET NULL;
ALTER TABLE matches ADD COLUMN decided_by VARCHAR(20);   -- REGULAR|EXTRA_TIME|PENALTIES|AGGREGATE|WALKOVER
ALTER TABLE matches ADD COLUMN tie_id UUID;              -- groups the 2 legs of one knockout tie

CREATE INDEX idx_matches_tie ON matches(tie_id);
CREATE INDEX idx_matches_winner ON matches(winner_team_id);

-- --- match_slot: bracket wiring -------------------------------------------
-- One row per side (HOME/AWAY) of a knockout match, describing where that
-- participant comes from so the advance engine can fill it in automatically.
CREATE TABLE match_slot (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    side VARCHAR(8) NOT NULL,                 -- HOME | AWAY
    label VARCHAR(120),                       -- 'Winner Group A', 'Winner QF1', 'Best 3rd #2'
    source_kind VARCHAR(20) NOT NULL,         -- GROUP_POSITION | MATCH_WINNER | MATCH_LOSER | BEST_THIRD
    source_group_id UUID REFERENCES stage_groups(id) ON DELETE SET NULL,
    source_position INTEGER,                  -- 1=winner, 2=runner-up, 3=third
    source_match_id UUID REFERENCES matches(id) ON DELETE SET NULL,
    best_third_rank INTEGER,                  -- for BEST_THIRD: the Nth-best third-placed team
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
CREATE INDEX idx_match_slot_match ON match_slot(match_id);
CREATE INDEX idx_match_slot_source_match ON match_slot(source_match_id);

-- --- seasons: tournament format -------------------------------------------
ALTER TABLE seasons ADD COLUMN format_preset_key VARCHAR(50);  -- e.g. WORLD_CUP_2026, ASIAN_CUP_2023
ALTER TABLE seasons ADD COLUMN format_config TEXT;             -- resolved TournamentFormat JSON (optional override)

-- --- standings: persisted final rank within group ------------------------
ALTER TABLE standings ADD COLUMN group_rank INTEGER;
