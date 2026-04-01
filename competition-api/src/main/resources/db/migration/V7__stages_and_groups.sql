-- Stage: a phase within a season (Group Stage, Elimination Groups, Quarter Final, etc.)
CREATE TABLE stages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id UUID NOT NULL REFERENCES seasons(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,  -- GROUP or KNOCKOUT
    order_number INTEGER NOT NULL,
    legs INTEGER DEFAULT 1,     -- 1 for groups/single match, 2 for two-legged knockout
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- StageGroup: a group within a stage (Group A, Group B, etc.)
CREATE TABLE stage_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stage_id UUID NOT NULL REFERENCES stages(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Teams in a stage group
CREATE TABLE stage_group_teams (
    stage_group_id UUID NOT NULL REFERENCES stage_groups(id) ON DELETE CASCADE,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    PRIMARY KEY (stage_group_id, team_id)
);

-- Round now belongs to Stage (not directly to Season)
ALTER TABLE rounds ADD COLUMN stage_id UUID REFERENCES stages(id) ON DELETE CASCADE;
ALTER TABLE rounds ADD COLUMN stage_group_id UUID REFERENCES stage_groups(id) ON DELETE SET NULL;

-- Match gets leg number for knockout ties
ALTER TABLE matches ADD COLUMN leg INTEGER;

-- Standing can belong to a stage_group
ALTER TABLE standings ADD COLUMN stage_group_id UUID REFERENCES stage_groups(id) ON DELETE CASCADE;

-- Migrate existing Othelose data: create a default stage for each existing season
DO $$
DECLARE
  s RECORD;
  v_stage UUID;
  v_group UUID;
BEGIN
  FOR s IN SELECT id FROM seasons LOOP
    -- Create default "League" stage
    INSERT INTO stages (season_id, name, type, order_number) VALUES (s.id, 'League', 'GROUP', 1) RETURNING id INTO v_stage;
    -- Create single group
    INSERT INTO stage_groups (stage_id, name) VALUES (v_stage, 'League') RETURNING id INTO v_group;
    -- Link season_teams to stage_group_teams
    INSERT INTO stage_group_teams (stage_group_id, team_id)
      SELECT v_group, team_id FROM season_teams WHERE season_id = s.id;
    -- Update rounds to point to stage
    UPDATE rounds SET stage_id = v_stage, stage_group_id = v_group WHERE season_id = s.id;
    -- Update standings to point to stage_group
    UPDATE standings SET stage_group_id = v_group WHERE season_id = s.id;
  END LOOP;
END $$;

CREATE INDEX idx_stages_season ON stages(season_id);
CREATE INDEX idx_stage_groups_stage ON stage_groups(stage_id);
CREATE INDEX idx_rounds_stage ON rounds(stage_id);
CREATE INDEX idx_rounds_stage_group ON rounds(stage_group_id);
CREATE INDEX idx_standings_stage_group ON standings(stage_group_id);
