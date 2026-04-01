DO $$
DECLARE
  v_uni UUID; v_comp UUID; v_season UUID; r UUID;
  t_ace UUID; t_bal UUID; t_ben UUID; t_can UUID;
  t_ful UUID; t_hri UUID; t_ilh UUID; t_ite UUID;
  t_men UUID; t_miz UUID; t_not UUID; t_nru UUID;
  t_occ UUID; t_pul UUID; t_rab UUID; t_tat UUID;
BEGIN
  SELECT id INTO v_uni FROM universes WHERE name = 'Othelose' LIMIT 1;
  SELECT id INTO v_comp FROM competitions WHERE name = 'Othelose League' AND universe_id = v_uni LIMIT 1;

  -- Skip if Season 5 already exists
  IF EXISTS (SELECT 1 FROM seasons WHERE name = 'Season 5' AND competition_id = v_comp) THEN
    RAISE NOTICE 'Season 5 already exists, skipping';
    RETURN;
  END IF;

  -- Get existing teams
  SELECT id INTO t_ace FROM teams WHERE name='Acer Robert' AND universe_id=v_uni LIMIT 1;
  SELECT id INTO t_ben FROM teams WHERE name='Ben Derber' AND universe_id=v_uni LIMIT 1;
  SELECT id INTO t_ful FROM teams WHERE name='Fulhaster' AND universe_id=v_uni LIMIT 1;
  SELECT id INTO t_hri FROM teams WHERE name='Hrilling Best' AND universe_id=v_uni LIMIT 1;
  SELECT id INTO t_ite FROM teams WHERE name='Iteser Icer' AND universe_id=v_uni LIMIT 1;
  SELECT id INTO t_men FROM teams WHERE name='Mener Lying' AND universe_id=v_uni LIMIT 1;
  SELECT id INTO t_miz FROM teams WHERE name='Mize Mole' AND universe_id=v_uni LIMIT 1;
  SELECT id INTO t_nru FROM teams WHERE name='Nrucaste' AND universe_id=v_uni LIMIT 1;
  SELECT id INTO t_not FROM teams WHERE name='Notes Valzine' AND universe_id=v_uni LIMIT 1;
  SELECT id INTO t_occ FROM teams WHERE name='Occating Menchiote' AND universe_id=v_uni LIMIT 1;
  SELECT id INTO t_pul FROM teams WHERE name='Pulzerr' AND universe_id=v_uni LIMIT 1;
  SELECT id INTO t_rab FROM teams WHERE name='Rab Pladineer' AND universe_id=v_uni LIMIT 1;
  SELECT id INTO t_tat FROM teams WHERE name='Tatines David' AND universe_id=v_uni LIMIT 1;

  -- Create new teams for Season 5
  IF NOT EXISTS (SELECT 1 FROM teams WHERE name='Balland Wolve' AND universe_id=v_uni) THEN
    INSERT INTO teams (name,short_name,type,primary_color,secondary_color,universe_id) VALUES ('Balland Wolve','Bal','CLUB','#ffcc00','#0033cc',v_uni) RETURNING id INTO t_bal;
  ELSE SELECT id INTO t_bal FROM teams WHERE name='Balland Wolve' AND universe_id=v_uni LIMIT 1; END IF;

  IF NOT EXISTS (SELECT 1 FROM teams WHERE name='Cantone' AND universe_id=v_uni) THEN
    INSERT INTO teams (name,short_name,type,primary_color,secondary_color,universe_id) VALUES ('Cantone','Can','CLUB','#cc0000','#ffffff',v_uni) RETURNING id INTO t_can;
  ELSE SELECT id INTO t_can FROM teams WHERE name='Cantone' AND universe_id=v_uni LIMIT 1; END IF;

  IF NOT EXISTS (SELECT 1 FROM teams WHERE name='Ilham Rower' AND universe_id=v_uni) THEN
    INSERT INTO teams (name,short_name,type,primary_color,secondary_color,universe_id) VALUES ('Ilham Rower','Ilh','CLUB','#663399','#ffffff',v_uni) RETURNING id INTO t_ilh;
  ELSE SELECT id INTO t_ilh FROM teams WHERE name='Ilham Rower' AND universe_id=v_uni LIMIT 1; END IF;

  -- Season 5
  INSERT INTO seasons (name,year,status,competition_id) VALUES ('Season 5',2006,'COMPLETED',v_comp) RETURNING id INTO v_season;
  INSERT INTO season_teams (season_id,team_id) VALUES
    (v_season,t_ace),(v_season,t_bal),(v_season,t_ben),(v_season,t_can),
    (v_season,t_ful),(v_season,t_hri),(v_season,t_ilh),(v_season,t_ite),
    (v_season,t_men),(v_season,t_miz),(v_season,t_not),(v_season,t_nru),
    (v_season,t_occ),(v_season,t_pul),(v_season,t_rab),(v_season,t_tat);

  -- L1
  INSERT INTO rounds (round_number,name,season_id) VALUES (1,'L1',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_nru,t_occ,3,2,'COMPLETED'),(r,t_ben,t_pul,0,0,'COMPLETED'),
    (r,t_ful,t_hri,2,3,'COMPLETED'),(r,t_can,t_not,2,0,'COMPLETED'),
    (r,t_ilh,t_men,2,2,'COMPLETED'),(r,t_ite,t_ace,1,2,'COMPLETED'),
    (r,t_bal,t_rab,1,1,'COMPLETED'),(r,t_tat,t_miz,1,0,'COMPLETED');
  -- L2
  INSERT INTO rounds (round_number,name,season_id) VALUES (2,'L2',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_miz,t_nru,3,1,'COMPLETED'),(r,t_hri,t_tat,0,0,'COMPLETED'),
    (r,t_men,t_ite,4,0,'COMPLETED'),(r,t_rab,t_ben,1,1,'COMPLETED'),
    (r,t_ace,t_can,2,0,'COMPLETED'),(r,t_occ,t_bal,1,0,'COMPLETED'),
    (r,t_pul,t_ilh,0,2,'COMPLETED'),(r,t_not,t_ful,1,2,'COMPLETED');
  -- L3
  INSERT INTO rounds (round_number,name,season_id) VALUES (3,'L3',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_occ,t_miz,3,1,'COMPLETED'),(r,t_ace,t_nru,1,0,'COMPLETED'),
    (r,t_hri,t_men,4,2,'COMPLETED'),(r,t_tat,t_ite,2,1,'COMPLETED'),
    (r,t_pul,t_not,2,1,'COMPLETED'),(r,t_ilh,t_ful,2,0,'COMPLETED'),
    (r,t_can,t_rab,1,1,'COMPLETED'),(r,t_ben,t_bal,3,0,'COMPLETED');
  -- L4
  INSERT INTO rounds (round_number,name,season_id) VALUES (4,'L4',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_bal,t_hri,1,2,'COMPLETED'),(r,t_ite,t_occ,1,0,'COMPLETED'),
    (r,t_ful,t_pul,4,0,'COMPLETED'),(r,t_nru,t_tat,0,1,'COMPLETED'),
    (r,t_rab,t_ace,2,3,'COMPLETED'),(r,t_men,t_not,1,1,'COMPLETED'),
    (r,t_can,t_ilh,0,0,'COMPLETED'),(r,t_miz,t_ben,2,1,'COMPLETED');
  -- L5
  INSERT INTO rounds (round_number,name,season_id) VALUES (5,'L5',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_not,t_ite,0,0,'COMPLETED'),(r,t_ful,t_can,3,1,'COMPLETED'),
    (r,t_ben,t_ilh,0,1,'COMPLETED'),(r,t_men,t_bal,1,2,'COMPLETED'),
    (r,t_pul,t_miz,2,0,'COMPLETED'),(r,t_rab,t_nru,2,0,'COMPLETED'),
    (r,t_hri,t_occ,2,1,'COMPLETED'),(r,t_ace,t_tat,3,1,'COMPLETED');
  -- L6
  INSERT INTO rounds (round_number,name,season_id) VALUES (6,'L6',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ben,t_ace,2,2,'COMPLETED'),(r,t_ite,t_rab,0,0,'COMPLETED'),
    (r,t_occ,t_pul,0,1,'COMPLETED'),(r,t_ilh,t_hri,0,0,'COMPLETED'),
    (r,t_tat,t_not,1,2,'COMPLETED'),(r,t_bal,t_can,5,3,'COMPLETED'),
    (r,t_miz,t_ful,1,0,'COMPLETED'),(r,t_nru,t_men,1,2,'COMPLETED');
  -- L7
  INSERT INTO rounds (round_number,name,season_id) VALUES (7,'L7',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_men,t_tat,3,0,'COMPLETED'),(r,t_can,t_occ,3,1,'COMPLETED'),
    (r,t_miz,t_hri,1,2,'COMPLETED'),(r,t_ful,t_rab,2,1,'COMPLETED'),
    (r,t_nru,t_ben,0,2,'COMPLETED'),(r,t_ace,t_pul,1,0,'COMPLETED'),
    (r,t_ite,t_bal,1,1,'COMPLETED'),(r,t_not,t_ilh,0,1,'COMPLETED');
  -- L8
  INSERT INTO rounds (round_number,name,season_id) VALUES (8,'L8',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_rab,t_pul,2,0,'COMPLETED'),(r,t_bal,t_nru,1,3,'COMPLETED'),
    (r,t_hri,t_not,2,0,'COMPLETED'),(r,t_tat,t_ilh,0,2,'COMPLETED'),
    (r,t_ace,t_ful,2,3,'COMPLETED'),(r,t_can,t_miz,1,0,'COMPLETED'),
    (r,t_ben,t_ite,3,0,'COMPLETED'),(r,t_occ,t_men,1,1,'COMPLETED');
  -- L9
  INSERT INTO rounds (round_number,name,season_id) VALUES (9,'L9',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_not,t_miz,0,1,'COMPLETED'),(r,t_ful,t_bal,2,2,'COMPLETED'),
    (r,t_occ,t_ace,1,1,'COMPLETED'),(r,t_pul,t_men,1,2,'COMPLETED'),
    (r,t_tat,t_ben,1,0,'COMPLETED'),(r,t_ilh,t_ite,2,2,'COMPLETED'),
    (r,t_nru,t_can,1,2,'COMPLETED'),(r,t_rab,t_hri,0,0,'COMPLETED');
  -- L10
  INSERT INTO rounds (round_number,name,season_id) VALUES (10,'L10',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ful,t_nru,1,1,'COMPLETED'),(r,t_ite,t_hri,1,2,'COMPLETED'),
    (r,t_miz,t_ilh,1,1,'COMPLETED'),(r,t_ben,t_occ,6,3,'COMPLETED'),
    (r,t_men,t_can,0,1,'COMPLETED'),(r,t_not,t_rab,0,2,'COMPLETED'),
    (r,t_pul,t_tat,1,1,'COMPLETED'),(r,t_bal,t_ace,2,1,'COMPLETED');
  -- L11
  INSERT INTO rounds (round_number,name,season_id) VALUES (11,'L11',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_can,t_ite,1,0,'COMPLETED'),(r,t_bal,t_not,0,0,'COMPLETED'),
    (r,t_nru,t_pul,3,1,'COMPLETED'),(r,t_hri,t_ace,1,1,'COMPLETED'),
    (r,t_ilh,t_rab,0,1,'COMPLETED'),(r,t_tat,t_occ,2,2,'COMPLETED'),
    (r,t_ben,t_ful,2,1,'COMPLETED'),(r,t_men,t_miz,1,1,'COMPLETED');
  -- L12
  INSERT INTO rounds (round_number,name,season_id) VALUES (12,'L12',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_men,t_ben,2,0,'COMPLETED'),(r,t_ful,t_tat,3,1,'COMPLETED'),
    (r,t_ace,t_not,1,0,'COMPLETED'),(r,t_ite,t_miz,3,2,'COMPLETED'),
    (r,t_pul,t_bal,0,0,'COMPLETED'),(r,t_hri,t_can,2,1,'COMPLETED'),
    (r,t_rab,t_occ,1,2,'COMPLETED'),(r,t_ilh,t_nru,0,0,'COMPLETED');
  -- L13
  INSERT INTO rounds (round_number,name,season_id) VALUES (13,'L13',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_occ,t_ilh,0,2,'COMPLETED'),(r,t_miz,t_ace,1,2,'COMPLETED'),
    (r,t_rab,t_men,1,0,'COMPLETED'),(r,t_ben,t_hri,2,2,'COMPLETED'),
    (r,t_not,t_nru,4,1,'COMPLETED'),(r,t_ite,t_ful,1,0,'COMPLETED'),
    (r,t_tat,t_bal,1,2,'COMPLETED'),(r,t_can,t_pul,1,1,'COMPLETED');
  -- L14
  INSERT INTO rounds (round_number,name,season_id) VALUES (14,'L14',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_tat,t_rab,2,1,'COMPLETED'),(r,t_ben,t_can,1,0,'COMPLETED'),
    (r,t_occ,t_not,0,1,'COMPLETED'),(r,t_bal,t_miz,3,3,'COMPLETED'),
    (r,t_pul,t_hri,0,2,'COMPLETED'),(r,t_ace,t_ilh,2,1,'COMPLETED'),
    (r,t_nru,t_ite,1,0,'COMPLETED'),(r,t_ful,t_men,2,2,'COMPLETED');
  -- L15
  INSERT INTO rounds (round_number,name,season_id) VALUES (15,'L15',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_hri,t_nru,0,0,'COMPLETED'),(r,t_men,t_ace,2,1,'COMPLETED'),
    (r,t_ite,t_pul,1,1,'COMPLETED'),(r,t_can,t_tat,2,3,'COMPLETED'),
    (r,t_ilh,t_bal,0,0,'COMPLETED'),(r,t_ful,t_occ,1,1,'COMPLETED'),
    (r,t_not,t_ben,2,0,'COMPLETED'),(r,t_rab,t_miz,0,3,'COMPLETED');
  -- L16
  INSERT INTO rounds (round_number,name,season_id) VALUES (16,'L16',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_pul,t_ben,2,1,'COMPLETED'),(r,t_hri,t_ful,2,2,'COMPLETED'),
    (r,t_rab,t_bal,1,0,'COMPLETED'),(r,t_occ,t_nru,1,0,'COMPLETED'),
    (r,t_miz,t_tat,3,1,'COMPLETED'),(r,t_not,t_can,1,1,'COMPLETED'),
    (r,t_men,t_ilh,1,0,'COMPLETED'),(r,t_ace,t_ite,0,0,'COMPLETED');
  -- L17
  INSERT INTO rounds (round_number,name,season_id) VALUES (17,'L17',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_can,t_ace,0,2,'COMPLETED'),(r,t_nru,t_miz,0,1,'COMPLETED'),
    (r,t_ite,t_men,0,3,'COMPLETED'),(r,t_ilh,t_pul,2,0,'COMPLETED'),
    (r,t_ful,t_not,3,3,'COMPLETED'),(r,t_ben,t_rab,4,0,'COMPLETED'),
    (r,t_bal,t_occ,2,1,'COMPLETED'),(r,t_tat,t_hri,1,2,'COMPLETED');
  -- L18
  INSERT INTO rounds (round_number,name,season_id) VALUES (18,'L18',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ite,t_tat,2,0,'COMPLETED'),(r,t_men,t_hri,0,2,'COMPLETED'),
    (r,t_ful,t_ilh,3,1,'COMPLETED'),(r,t_bal,t_ben,0,0,'COMPLETED'),
    (r,t_miz,t_occ,2,0,'COMPLETED'),(r,t_nru,t_ace,1,1,'COMPLETED'),
    (r,t_not,t_pul,3,0,'COMPLETED'),(r,t_rab,t_can,1,0,'COMPLETED');
  -- L19
  INSERT INTO rounds (round_number,name,season_id) VALUES (19,'L19',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_hri,t_bal,0,1,'COMPLETED'),(r,t_tat,t_nru,2,2,'COMPLETED'),
    (r,t_not,t_men,2,0,'COMPLETED'),(r,t_occ,t_ite,0,0,'COMPLETED'),
    (r,t_ace,t_rab,1,0,'COMPLETED'),(r,t_pul,t_ful,2,1,'COMPLETED'),
    (r,t_ilh,t_can,2,0,'COMPLETED'),(r,t_ben,t_miz,2,2,'COMPLETED');
  -- L20
  INSERT INTO rounds (round_number,name,season_id) VALUES (20,'L20',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_nru,t_rab,2,1,'COMPLETED'),(r,t_miz,t_pul,1,0,'COMPLETED'),
    (r,t_tat,t_ace,1,1,'COMPLETED'),(r,t_can,t_ful,1,1,'COMPLETED'),
    (r,t_ilh,t_ben,0,2,'COMPLETED'),(r,t_ite,t_not,3,3,'COMPLETED'),
    (r,t_bal,t_men,2,1,'COMPLETED'),(r,t_occ,t_hri,1,0,'COMPLETED');
  -- L21
  INSERT INTO rounds (round_number,name,season_id) VALUES (21,'L21',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ful,t_miz,2,1,'COMPLETED'),(r,t_hri,t_ilh,1,0,'COMPLETED'),
    (r,t_men,t_nru,4,1,'COMPLETED'),(r,t_ace,t_ben,2,1,'COMPLETED'),
    (r,t_pul,t_occ,3,0,'COMPLETED'),(r,t_can,t_bal,1,1,'COMPLETED'),
    (r,t_rab,t_ite,0,0,'COMPLETED'),(r,t_not,t_tat,1,1,'COMPLETED');
  -- L22
  INSERT INTO rounds (round_number,name,season_id) VALUES (22,'L22',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_occ,t_can,1,3,'COMPLETED'),(r,t_bal,t_ite,1,2,'COMPLETED'),
    (r,t_ilh,t_not,0,0,'COMPLETED'),(r,t_tat,t_men,0,1,'COMPLETED'),
    (r,t_rab,t_ful,1,0,'COMPLETED'),(r,t_hri,t_miz,4,2,'COMPLETED'),
    (r,t_pul,t_ace,0,1,'COMPLETED'),(r,t_ben,t_nru,2,0,'COMPLETED');
  -- L23
  INSERT INTO rounds (round_number,name,season_id) VALUES (23,'L23',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ilh,t_tat,1,2,'COMPLETED'),(r,t_not,t_hri,3,2,'COMPLETED'),
    (r,t_pul,t_rab,2,0,'COMPLETED'),(r,t_nru,t_bal,3,1,'COMPLETED'),
    (r,t_ite,t_ben,0,0,'COMPLETED'),(r,t_ful,t_ace,1,0,'COMPLETED'),
    (r,t_men,t_occ,1,1,'COMPLETED'),(r,t_miz,t_can,0,0,'COMPLETED');
  -- L24
  INSERT INTO rounds (round_number,name,season_id) VALUES (24,'L24',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_men,t_pul,0,0,'COMPLETED'),(r,t_ben,t_tat,1,1,'COMPLETED'),
    (r,t_ace,t_occ,0,0,'COMPLETED'),(r,t_hri,t_rab,6,2,'COMPLETED'),
    (r,t_can,t_nru,1,0,'COMPLETED'),(r,t_miz,t_not,2,3,'COMPLETED'),
    (r,t_bal,t_ful,0,2,'COMPLETED'),(r,t_ite,t_ilh,1,1,'COMPLETED');
  -- L25
  INSERT INTO rounds (round_number,name,season_id) VALUES (25,'L25',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ace,t_bal,1,0,'COMPLETED'),(r,t_can,t_men,1,3,'COMPLETED'),
    (r,t_ilh,t_miz,2,0,'COMPLETED'),(r,t_nru,t_ful,1,0,'COMPLETED'),
    (r,t_hri,t_ite,2,2,'COMPLETED'),(r,t_occ,t_ben,3,1,'COMPLETED'),
    (r,t_rab,t_not,3,3,'COMPLETED'),(r,t_tat,t_pul,2,0,'COMPLETED');
  -- L26
  INSERT INTO rounds (round_number,name,season_id) VALUES (26,'L26',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ful,t_ben,0,0,'COMPLETED'),(r,t_occ,t_tat,1,2,'COMPLETED'),
    (r,t_not,t_bal,0,0,'COMPLETED'),(r,t_ite,t_can,2,0,'COMPLETED'),
    (r,t_rab,t_ilh,2,2,'COMPLETED'),(r,t_pul,t_nru,2,0,'COMPLETED'),
    (r,t_miz,t_men,0,0,'COMPLETED'),(r,t_ace,t_hri,1,2,'COMPLETED');
  -- L27
  INSERT INTO rounds (round_number,name,season_id) VALUES (27,'L27',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_miz,t_ite,3,0,'COMPLETED'),(r,t_can,t_hri,1,2,'COMPLETED'),
    (r,t_ben,t_men,1,2,'COMPLETED'),(r,t_occ,t_rab,1,0,'COMPLETED'),
    (r,t_bal,t_pul,2,2,'COMPLETED'),(r,t_not,t_ace,1,0,'COMPLETED'),
    (r,t_tat,t_ful,2,2,'COMPLETED'),(r,t_nru,t_ilh,0,1,'COMPLETED');
  -- L28
  INSERT INTO rounds (round_number,name,season_id) VALUES (28,'L28',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_nru,t_not,4,2,'COMPLETED'),(r,t_ace,t_miz,2,0,'COMPLETED'),
    (r,t_ilh,t_occ,1,2,'COMPLETED'),(r,t_ful,t_ite,0,0,'COMPLETED'),
    (r,t_hri,t_ben,2,0,'COMPLETED'),(r,t_bal,t_tat,2,1,'COMPLETED'),
    (r,t_men,t_rab,0,1,'COMPLETED'),(r,t_pul,t_can,3,1,'COMPLETED');
  -- L29
  INSERT INTO rounds (round_number,name,season_id) VALUES (29,'L29',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_rab,t_tat,0,0,'COMPLETED'),(r,t_men,t_ful,2,1,'COMPLETED'),
    (r,t_can,t_ben,0,1,'COMPLETED'),(r,t_hri,t_pul,3,2,'COMPLETED'),
    (r,t_ite,t_nru,1,1,'COMPLETED'),(r,t_ilh,t_ace,3,1,'COMPLETED'),
    (r,t_miz,t_bal,2,1,'COMPLETED'),(r,t_not,t_occ,2,1,'COMPLETED');
  -- L30
  INSERT INTO rounds (round_number,name,season_id) VALUES (30,'L30',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ace,t_men,3,3,'COMPLETED'),(r,t_pul,t_ite,0,2,'COMPLETED'),
    (r,t_bal,t_ilh,2,3,'COMPLETED'),(r,t_miz,t_rab,1,3,'COMPLETED'),
    (r,t_ben,t_not,1,0,'COMPLETED'),(r,t_occ,t_ful,1,2,'COMPLETED'),
    (r,t_tat,t_can,0,0,'COMPLETED'),(r,t_nru,t_hri,1,1,'COMPLETED');

  -- Final standings (RECORDED)
  INSERT INTO standings (season_id,team_id,type,after_round,played,won,drawn,lost,goals_for,goals_against,points) VALUES
    (v_season,t_hri,'RECORDED',NULL,30,18,9,3,53,30,63),
    (v_season,t_ace,'RECORDED',NULL,30,15,8,7,44,30,53),
    (v_season,t_men,'RECORDED',NULL,30,13,9,8,46,33,48),
    (v_season,t_ilh,'RECORDED',NULL,30,12,10,8,35,25,46),
    (v_season,t_ful,'RECORDED',NULL,30,11,10,9,46,38,43),
    (v_season,t_ben,'RECORDED',NULL,30,11,9,10,40,31,42),
    (v_season,t_not,'RECORDED',NULL,30,10,10,10,39,37,40),
    (v_season,t_miz,'RECORDED',NULL,30,11,6,13,40,40,39),
    (v_season,t_rab,'RECORDED',NULL,30,10,9,11,36,40,39),
    (v_season,t_tat,'RECORDED',NULL,30,9,10,11,33,41,37),
    (v_season,t_bal,'RECORDED',NULL,30,8,11,11,35,42,35),
    (v_season,t_ite,'RECORDED',NULL,30,7,14,9,27,35,35),
    (v_season,t_can,'RECORDED',NULL,30,9,7,14,29,40,34),
    (v_season,t_pul,'RECORDED',NULL,30,9,7,14,28,39,34),
    (v_season,t_nru,'RECORDED',NULL,30,8,7,15,31,43,31),
    (v_season,t_occ,'RECORDED',NULL,30,8,7,15,32,45,31);

END $$;
