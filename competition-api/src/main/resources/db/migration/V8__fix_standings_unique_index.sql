-- Drop old indexes
DROP INDEX IF EXISTS idx_standings_final;
DROP INDEX IF EXISTS idx_standings_checkpoint;

-- New unique indexes that account for stage_group_id
CREATE UNIQUE INDEX idx_standings_group_final ON standings(season_id, team_id, type, stage_group_id)
  WHERE after_round IS NULL AND stage_group_id IS NOT NULL;

CREATE UNIQUE INDEX idx_standings_season_final ON standings(season_id, team_id, type)
  WHERE after_round IS NULL AND stage_group_id IS NULL;

CREATE UNIQUE INDEX idx_standings_checkpoint ON standings(season_id, team_id, type, after_round)
  WHERE after_round IS NOT NULL;
