DO $$
DECLARE
  v_uni UUID; v_comp UUID; v_season UUID; r UUID;
  t_ace UUID; t_bal UUID; t_ben UUID; t_can UUID;
  t_dom UUID; t_ful UUID; t_hri UUID; t_ilh UUID;
  t_ite UUID; t_men UUID; t_miz UUID; t_not UUID;
  t_pro UUID; t_rab UUID; t_tat UUID; t_wil UUID;
BEGIN
  SELECT id INTO v_uni FROM universes WHERE name='Othelose' LIMIT 1;
  SELECT id INTO v_comp FROM competitions WHERE name='Othelose League' AND universe_id=v_uni LIMIT 1;
  IF EXISTS (SELECT 1 FROM seasons WHERE name='Season 6' AND competition_id=v_comp) THEN RAISE NOTICE 'Season 6 exists, skip'; RETURN; END IF;

  -- Get existing teams
  SELECT id INTO t_ace FROM teams WHERE name='Acer Robert' AND universe_id=v_uni;
  SELECT id INTO t_bal FROM teams WHERE name='Balland Wolve' AND universe_id=v_uni;
  SELECT id INTO t_ben FROM teams WHERE name='Ben Derber' AND universe_id=v_uni;
  SELECT id INTO t_can FROM teams WHERE name='Cantone' AND universe_id=v_uni;
  SELECT id INTO t_dom FROM teams WHERE name='Domehampton' AND universe_id=v_uni;
  SELECT id INTO t_ful FROM teams WHERE name='Fulhaster' AND universe_id=v_uni;
  SELECT id INTO t_hri FROM teams WHERE name='Hrilling Best' AND universe_id=v_uni;
  SELECT id INTO t_ilh FROM teams WHERE name='Ilham Rower' AND universe_id=v_uni;
  SELECT id INTO t_ite FROM teams WHERE name='Iteser Icer' AND universe_id=v_uni;
  SELECT id INTO t_men FROM teams WHERE name='Mener Lying' AND universe_id=v_uni;
  SELECT id INTO t_miz FROM teams WHERE name='Mize Mole' AND universe_id=v_uni;
  SELECT id INTO t_not FROM teams WHERE name='Notes Valzine' AND universe_id=v_uni;
  SELECT id INTO t_rab FROM teams WHERE name='Rab Pladineer' AND universe_id=v_uni;
  SELECT id INTO t_tat FROM teams WHERE name='Tatines David' AND universe_id=v_uni;

  -- New teams
  IF NOT EXISTS (SELECT 1 FROM teams WHERE name='Processing Probal' AND universe_id=v_uni) THEN
    INSERT INTO teams (name,short_name,type,primary_color,secondary_color,universe_id) VALUES ('Processing Probal','Pro','CLUB','#336699','#ffffff',v_uni) RETURNING id INTO t_pro;
  ELSE SELECT id INTO t_pro FROM teams WHERE name='Processing Probal' AND universe_id=v_uni; END IF;
  IF NOT EXISTS (SELECT 1 FROM teams WHERE name='Willandos' AND universe_id=v_uni) THEN
    INSERT INTO teams (name,short_name,type,primary_color,secondary_color,universe_id) VALUES ('Willandos','Wil','CLUB','#996633','#ffffff',v_uni) RETURNING id INTO t_wil;
  ELSE SELECT id INTO t_wil FROM teams WHERE name='Willandos' AND universe_id=v_uni; END IF;

  INSERT INTO seasons (name,year,status,competition_id) VALUES ('Season 6',2007,'COMPLETED',v_comp) RETURNING id INTO v_season;
  INSERT INTO season_teams (season_id,team_id) VALUES
    (v_season,t_ace),(v_season,t_bal),(v_season,t_ben),(v_season,t_can),(v_season,t_dom),(v_season,t_ful),
    (v_season,t_hri),(v_season,t_ilh),(v_season,t_ite),(v_season,t_men),(v_season,t_miz),(v_season,t_not),
    (v_season,t_pro),(v_season,t_rab),(v_season,t_tat),(v_season,t_wil);

  -- L1
  INSERT INTO rounds (round_number,name,season_id) VALUES (1,'L1',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_miz,t_dom,0,1,'COMPLETED'),(r,t_can,t_rab,0,2,'COMPLETED'),(r,t_men,t_ilh,2,2,'COMPLETED'),(r,t_ace,t_wil,4,0,'COMPLETED'),
    (r,t_ite,t_ful,0,0,'COMPLETED'),(r,t_bal,t_tat,3,1,'COMPLETED'),(r,t_not,t_hri,2,2,'COMPLETED'),(r,t_pro,t_ben,1,1,'COMPLETED');
  -- L2
  INSERT INTO rounds (round_number,name,season_id) VALUES (2,'L2',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ful,t_bal,3,0,'COMPLETED'),(r,t_ben,t_miz,1,0,'COMPLETED'),(r,t_ilh,t_pro,2,2,'COMPLETED'),(r,t_hri,t_can,2,0,'COMPLETED'),
    (r,t_tat,t_ace,1,2,'COMPLETED'),(r,t_dom,t_not,1,0,'COMPLETED'),(r,t_rab,t_ite,1,2,'COMPLETED'),(r,t_wil,t_men,0,2,'COMPLETED');
  -- L3
  INSERT INTO rounds (round_number,name,season_id) VALUES (3,'L3',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_rab,t_wil,2,1,'COMPLETED'),(r,t_ite,t_men,3,0,'COMPLETED'),(r,t_can,t_not,3,4,'COMPLETED'),(r,t_dom,t_ben,0,0,'COMPLETED'),
    (r,t_pro,t_bal,1,0,'COMPLETED'),(r,t_ace,t_hri,1,0,'COMPLETED'),(r,t_ilh,t_ful,3,1,'COMPLETED'),(r,t_tat,t_miz,2,0,'COMPLETED');
  -- L4
  INSERT INTO rounds (round_number,name,season_id) VALUES (4,'L4',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_not,t_ilh,1,0,'COMPLETED'),(r,t_ful,t_wil,2,1,'COMPLETED'),(r,t_miz,t_pro,2,0,'COMPLETED'),(r,t_hri,t_tat,1,1,'COMPLETED'),
    (r,t_ace,t_ite,4,3,'COMPLETED'),(r,t_men,t_rab,1,0,'COMPLETED'),(r,t_ben,t_can,1,0,'COMPLETED'),(r,t_bal,t_dom,0,0,'COMPLETED');
  -- L5
  INSERT INTO rounds (round_number,name,season_id) VALUES (5,'L5',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_tat,t_pro,2,3,'COMPLETED'),(r,t_can,t_ite,0,0,'COMPLETED'),(r,t_ilh,t_dom,0,0,'COMPLETED'),(r,t_rab,t_ben,1,2,'COMPLETED'),
    (r,t_wil,t_bal,2,0,'COMPLETED'),(r,t_ful,t_not,1,2,'COMPLETED'),(r,t_hri,t_miz,2,0,'COMPLETED'),(r,t_men,t_ace,1,1,'COMPLETED');
  -- L6
  INSERT INTO rounds (round_number,name,season_id) VALUES (6,'L6',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ben,t_men,2,2,'COMPLETED'),(r,t_miz,t_ful,0,1,'COMPLETED'),(r,t_not,t_ace,1,0,'COMPLETED'),(r,t_can,t_tat,0,0,'COMPLETED'),
    (r,t_dom,t_rab,1,3,'COMPLETED'),(r,t_bal,t_hri,1,2,'COMPLETED'),(r,t_pro,t_wil,1,2,'COMPLETED'),(r,t_ite,t_ilh,5,2,'COMPLETED');
  -- L7
  INSERT INTO rounds (round_number,name,season_id) VALUES (7,'L7',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_miz,t_can,2,0,'COMPLETED'),(r,t_ace,t_dom,2,2,'COMPLETED'),(r,t_men,t_hri,2,1,'COMPLETED'),(r,t_ful,t_pro,1,0,'COMPLETED'),
    (r,t_wil,t_ite,1,2,'COMPLETED'),(r,t_ben,t_ilh,1,1,'COMPLETED'),(r,t_tat,t_rab,2,0,'COMPLETED'),(r,t_bal,t_not,3,1,'COMPLETED');
  -- L8
  INSERT INTO rounds (round_number,name,season_id) VALUES (8,'L8',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_hri,t_rab,1,0,'COMPLETED'),(r,t_pro,t_ite,0,0,'COMPLETED'),(r,t_ilh,t_wil,2,0,'COMPLETED'),(r,t_tat,t_men,4,1,'COMPLETED'),
    (r,t_not,t_miz,3,3,'COMPLETED'),(r,t_can,t_bal,3,0,'COMPLETED'),(r,t_dom,t_ful,1,1,'COMPLETED'),(r,t_ace,t_ben,1,0,'COMPLETED');
  -- L9
  INSERT INTO rounds (round_number,name,season_id) VALUES (9,'L9',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ite,t_bal,1,0,'COMPLETED'),(r,t_men,t_not,2,3,'COMPLETED'),(r,t_rab,t_ful,4,3,'COMPLETED'),(r,t_miz,t_ace,0,1,'COMPLETED'),
    (r,t_dom,t_tat,2,1,'COMPLETED'),(r,t_wil,t_ben,1,1,'COMPLETED'),(r,t_pro,t_can,2,0,'COMPLETED'),(r,t_hri,t_ilh,2,1,'COMPLETED');
  -- L10
  INSERT INTO rounds (round_number,name,season_id) VALUES (10,'L10',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ful,t_ace,1,2,'COMPLETED'),(r,t_wil,t_hri,2,1,'COMPLETED'),(r,t_can,t_dom,3,1,'COMPLETED'),(r,t_rab,t_pro,1,2,'COMPLETED'),
    (r,t_ben,t_ite,1,0,'COMPLETED'),(r,t_bal,t_ilh,0,0,'COMPLETED'),(r,t_not,t_tat,0,1,'COMPLETED'),(r,t_men,t_miz,2,2,'COMPLETED');
  -- L11
  INSERT INTO rounds (round_number,name,season_id) VALUES (11,'L11',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_not,t_wil,2,0,'COMPLETED'),(r,t_miz,t_rab,2,1,'COMPLETED'),(r,t_ite,t_hri,3,1,'COMPLETED'),(r,t_can,t_men,1,1,'COMPLETED'),
    (r,t_ilh,t_tat,2,0,'COMPLETED'),(r,t_pro,t_dom,1,1,'COMPLETED'),(r,t_ful,t_ben,1,1,'COMPLETED'),(r,t_ace,t_bal,2,2,'COMPLETED');
  -- L12
  INSERT INTO rounds (round_number,name,season_id) VALUES (12,'L12',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_men,t_pro,3,0,'COMPLETED'),(r,t_ilh,t_ace,3,2,'COMPLETED'),(r,t_ful,t_can,0,1,'COMPLETED'),(r,t_ite,t_miz,1,3,'COMPLETED'),
    (r,t_bal,t_ben,2,0,'COMPLETED'),(r,t_rab,t_not,0,1,'COMPLETED'),(r,t_tat,t_wil,1,0,'COMPLETED'),(r,t_hri,t_dom,1,0,'COMPLETED');
  -- L13
  INSERT INTO rounds (round_number,name,season_id) VALUES (13,'L13',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_dom,t_ite,1,1,'COMPLETED'),(r,t_hri,t_ful,2,1,'COMPLETED'),(r,t_ben,t_tat,3,2,'COMPLETED'),(r,t_bal,t_men,2,2,'COMPLETED'),
    (r,t_ace,t_rab,0,0,'COMPLETED'),(r,t_wil,t_miz,0,0,'COMPLETED'),(r,t_pro,t_not,2,0,'COMPLETED'),(r,t_can,t_ilh,2,4,'COMPLETED');
  -- L14
  INSERT INTO rounds (round_number,name,season_id) VALUES (14,'L14',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_rab,t_ilh,1,0,'COMPLETED'),(r,t_can,t_ace,0,2,'COMPLETED'),(r,t_dom,t_wil,1,0,'COMPLETED'),(r,t_pro,t_hri,0,1,'COMPLETED'),
    (r,t_tat,t_ite,3,0,'COMPLETED'),(r,t_not,t_ben,2,0,'COMPLETED'),(r,t_miz,t_bal,2,3,'COMPLETED'),(r,t_men,t_ful,0,0,'COMPLETED');
  -- L15
  INSERT INTO rounds (round_number,name,season_id) VALUES (15,'L15',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ful,t_tat,4,2,'COMPLETED'),(r,t_men,t_dom,2,1,'COMPLETED'),(r,t_ace,t_pro,4,0,'COMPLETED'),(r,t_ite,t_not,3,1,'COMPLETED'),
    (r,t_ilh,t_miz,1,0,'COMPLETED'),(r,t_wil,t_can,1,0,'COMPLETED'),(r,t_bal,t_rab,2,0,'COMPLETED'),(r,t_hri,t_ben,1,0,'COMPLETED');
  -- L16
  INSERT INTO rounds (round_number,name,season_id) VALUES (16,'L16',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_hri,t_not,1,1,'COMPLETED'),(r,t_ben,t_pro,3,1,'COMPLETED'),(r,t_rab,t_can,2,3,'COMPLETED'),(r,t_dom,t_miz,0,0,'COMPLETED'),
    (r,t_tat,t_bal,0,1,'COMPLETED'),(r,t_ilh,t_men,2,2,'COMPLETED'),(r,t_ful,t_ite,2,1,'COMPLETED'),(r,t_wil,t_ace,1,4,'COMPLETED');
  -- L17
  INSERT INTO rounds (round_number,name,season_id) VALUES (17,'L17',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_miz,t_ben,2,0,'COMPLETED'),(r,t_ite,t_rab,1,1,'COMPLETED'),(r,t_bal,t_ful,1,0,'COMPLETED'),(r,t_ace,t_tat,1,0,'COMPLETED'),
    (r,t_men,t_wil,2,0,'COMPLETED'),(r,t_can,t_hri,1,3,'COMPLETED'),(r,t_not,t_dom,0,0,'COMPLETED'),(r,t_pro,t_ilh,1,1,'COMPLETED');
  -- L18
  INSERT INTO rounds (round_number,name,season_id) VALUES (18,'L18',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_wil,t_rab,0,1,'COMPLETED'),(r,t_ful,t_ilh,2,0,'COMPLETED'),(r,t_ben,t_dom,2,1,'COMPLETED'),(r,t_not,t_can,1,0,'COMPLETED'),
    (r,t_bal,t_pro,3,3,'COMPLETED'),(r,t_men,t_ite,0,3,'COMPLETED'),(r,t_hri,t_ace,2,1,'COMPLETED'),(r,t_miz,t_tat,2,0,'COMPLETED');
  -- L19
  INSERT INTO rounds (round_number,name,season_id) VALUES (19,'L19',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ite,t_ace,0,0,'COMPLETED'),(r,t_can,t_ben,3,1,'COMPLETED'),(r,t_pro,t_miz,2,1,'COMPLETED'),(r,t_tat,t_hri,1,0,'COMPLETED'),
    (r,t_dom,t_bal,0,0,'COMPLETED'),(r,t_ilh,t_not,1,0,'COMPLETED'),(r,t_rab,t_men,2,2,'COMPLETED'),(r,t_wil,t_ful,2,0,'COMPLETED');
  -- L20
  INSERT INTO rounds (round_number,name,season_id) VALUES (20,'L20',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_pro,t_tat,2,0,'COMPLETED'),(r,t_not,t_ful,0,0,'COMPLETED'),(r,t_bal,t_wil,1,1,'COMPLETED'),(r,t_ben,t_rab,1,0,'COMPLETED'),
    (r,t_ace,t_men,1,0,'COMPLETED'),(r,t_miz,t_hri,1,2,'COMPLETED'),(r,t_dom,t_ilh,3,2,'COMPLETED'),(r,t_ite,t_can,2,0,'COMPLETED');
  -- L21
  INSERT INTO rounds (round_number,name,season_id) VALUES (21,'L21',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ful,t_miz,1,0,'COMPLETED'),(r,t_men,t_ben,0,3,'COMPLETED'),(r,t_tat,t_can,2,0,'COMPLETED'),(r,t_ilh,t_ite,3,3,'COMPLETED'),
    (r,t_wil,t_pro,0,1,'COMPLETED'),(r,t_rab,t_dom,4,1,'COMPLETED'),(r,t_hri,t_bal,1,0,'COMPLETED'),(r,t_ace,t_not,1,0,'COMPLETED');
  -- L22
  INSERT INTO rounds (round_number,name,season_id) VALUES (22,'L22',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_hri,t_men,1,0,'COMPLETED'),(r,t_dom,t_ace,1,2,'COMPLETED'),(r,t_ilh,t_ben,2,1,'COMPLETED'),(r,t_not,t_bal,3,0,'COMPLETED'),
    (r,t_can,t_miz,2,0,'COMPLETED'),(r,t_ite,t_wil,0,1,'COMPLETED'),(r,t_rab,t_tat,1,1,'COMPLETED'),(r,t_pro,t_ful,0,0,'COMPLETED');
  -- L23
  INSERT INTO rounds (round_number,name,season_id) VALUES (23,'L23',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_bal,t_can,4,2,'COMPLETED'),(r,t_miz,t_not,2,0,'COMPLETED'),(r,t_ite,t_pro,2,1,'COMPLETED'),(r,t_rab,t_hri,2,0,'COMPLETED'),
    (r,t_men,t_tat,2,0,'COMPLETED'),(r,t_ful,t_dom,1,1,'COMPLETED'),(r,t_ben,t_ace,1,0,'COMPLETED'),(r,t_wil,t_ilh,2,0,'COMPLETED');
  -- L24
  INSERT INTO rounds (round_number,name,season_id) VALUES (24,'L24',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_tat,t_dom,1,1,'COMPLETED'),(r,t_ilh,t_hri,0,1,'COMPLETED'),(r,t_ful,t_rab,1,0,'COMPLETED'),(r,t_ben,t_wil,2,2,'COMPLETED'),
    (r,t_ace,t_miz,1,1,'COMPLETED'),(r,t_can,t_pro,0,0,'COMPLETED'),(r,t_not,t_men,2,1,'COMPLETED'),(r,t_bal,t_ite,2,0,'COMPLETED');
  -- L25
  INSERT INTO rounds (round_number,name,season_id) VALUES (25,'L25',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ite,t_ben,1,0,'COMPLETED'),(r,t_miz,t_men,0,0,'COMPLETED'),(r,t_hri,t_wil,4,0,'COMPLETED'),(r,t_tat,t_not,2,3,'COMPLETED'),
    (r,t_ilh,t_bal,2,1,'COMPLETED'),(r,t_ace,t_ful,1,0,'COMPLETED'),(r,t_pro,t_rab,1,1,'COMPLETED'),(r,t_dom,t_can,1,2,'COMPLETED');
  -- L26
  INSERT INTO rounds (round_number,name,season_id) VALUES (26,'L26',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_wil,t_not,2,2,'COMPLETED'),(r,t_bal,t_ace,2,1,'COMPLETED'),(r,t_dom,t_pro,3,0,'COMPLETED'),(r,t_hri,t_ite,3,1,'COMPLETED'),
    (r,t_rab,t_miz,0,0,'COMPLETED'),(r,t_men,t_can,2,0,'COMPLETED'),(r,t_tat,t_ilh,1,0,'COMPLETED'),(r,t_ben,t_ful,1,1,'COMPLETED');
  -- L27
  INSERT INTO rounds (round_number,name,season_id) VALUES (27,'L27',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_pro,t_men,1,2,'COMPLETED'),(r,t_not,t_rab,2,1,'COMPLETED'),(r,t_can,t_ful,1,4,'COMPLETED'),(r,t_ace,t_ilh,2,1,'COMPLETED'),
    (r,t_wil,t_tat,2,0,'COMPLETED'),(r,t_ben,t_bal,1,1,'COMPLETED'),(r,t_dom,t_hri,0,1,'COMPLETED'),(r,t_miz,t_ite,2,3,'COMPLETED');
  -- L28
  INSERT INTO rounds (round_number,name,season_id) VALUES (28,'L28',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_ilh,t_can,1,0,'COMPLETED'),(r,t_tat,t_ben,1,2,'COMPLETED'),(r,t_men,t_bal,1,1,'COMPLETED'),(r,t_ite,t_dom,2,0,'COMPLETED'),
    (r,t_ful,t_hri,0,2,'COMPLETED'),(r,t_miz,t_wil,3,0,'COMPLETED'),(r,t_rab,t_ace,0,0,'COMPLETED'),(r,t_not,t_pro,1,0,'COMPLETED');
  -- L29
  INSERT INTO rounds (round_number,name,season_id) VALUES (29,'L29',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_bal,t_miz,2,0,'COMPLETED'),(r,t_hri,t_pro,2,1,'COMPLETED'),(r,t_ace,t_can,0,0,'COMPLETED'),(r,t_ben,t_not,2,0,'COMPLETED'),
    (r,t_ite,t_tat,3,0,'COMPLETED'),(r,t_ilh,t_rab,0,2,'COMPLETED'),(r,t_wil,t_dom,2,0,'COMPLETED'),(r,t_ful,t_men,3,2,'COMPLETED');
  -- L30
  INSERT INTO rounds (round_number,name,season_id) VALUES (30,'L30',v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_tat,t_ful,0,2,'COMPLETED'),(r,t_dom,t_men,1,1,'COMPLETED'),(r,t_not,t_ite,3,1,'COMPLETED'),(r,t_can,t_wil,1,1,'COMPLETED'),
    (r,t_rab,t_bal,2,0,'COMPLETED'),(r,t_pro,t_ace,2,1,'COMPLETED'),(r,t_miz,t_ilh,3,2,'COMPLETED'),(r,t_ben,t_hri,1,1,'COMPLETED');

  -- Final standings
  INSERT INTO standings (season_id,team_id,type,after_round,played,won,drawn,lost,goals_for,goals_against,points) VALUES
    (v_season,t_hri,'RECORDED',NULL,30,20,4,6,44,24,64),
    (v_season,t_ace,'RECORDED',NULL,30,16,8,6,44,25,56),
    (v_season,t_not,'RECORDED',NULL,30,15,6,9,41,35,51),
    (v_season,t_ite,'RECORDED',NULL,30,14,7,9,47,36,49),
    (v_season,t_ben,'RECORDED',NULL,30,12,10,8,35,31,46),
    (v_season,t_ful,'RECORDED',NULL,30,12,8,10,37,31,44),
    (v_season,t_bal,'RECORDED',NULL,30,11,9,10,37,37,42),
    (v_season,t_men,'RECORDED',NULL,30,9,12,9,40,42,39),
    (v_season,t_ilh,'RECORDED',NULL,30,10,8,12,40,44,38),
    (v_season,t_rab,'RECORDED',NULL,30,10,7,13,35,33,37),
    (v_season,t_pro,'RECORDED',NULL,30,9,9,12,31,40,36),
    (v_season,t_miz,'RECORDED',NULL,30,8,10,12,33,34,34),
    (v_season,t_wil,'RECORDED',NULL,30,9,6,15,27,42,33),
    (v_season,t_dom,'RECORDED',NULL,30,6,13,11,26,35,31),
    (v_season,t_tat,'RECORDED',NULL,30,9,4,17,32,43,31),
    (v_season,t_can,'RECORDED',NULL,30,7,6,17,28,46,27);
END $$;
