-- =====================================================================
-- V21: Optional stadium per match.
--   Teams have a home stadium, but neutral-venue tournaments (World Cup,
--   Euro) play each match at a specific stadium unrelated to either team.
--   Nullable + ON DELETE SET NULL — purely informational, no logic depends
--   on it, so existing data and standings are unaffected.
-- =====================================================================

ALTER TABLE matches ADD COLUMN stadium_id UUID REFERENCES stadiums(id) ON DELETE SET NULL;
CREATE INDEX idx_matches_stadium ON matches(stadium_id);
