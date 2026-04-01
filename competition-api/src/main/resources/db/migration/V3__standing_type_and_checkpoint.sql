-- Add type and after_round to standings
ALTER TABLE standings ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'RECORDED';
ALTER TABLE standings ADD COLUMN after_round INTEGER;

-- Update existing standings to RECORDED final
UPDATE standings SET type = 'RECORDED', after_round = NULL;

-- Drop old unique constraint
ALTER TABLE standings DROP CONSTRAINT IF EXISTS standings_season_id_team_id_key;

-- Unique index for checkpoint standings (after_round is not null)
CREATE UNIQUE INDEX idx_standings_checkpoint ON standings(season_id, team_id, type, after_round)
  WHERE after_round IS NOT NULL;

-- Unique index for final standings (after_round is null)
CREATE UNIQUE INDEX idx_standings_final ON standings(season_id, team_id, type)
  WHERE after_round IS NULL;

CREATE INDEX idx_standings_type ON standings(type);
CREATE INDEX idx_standings_after_round ON standings(after_round);
