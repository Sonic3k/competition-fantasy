-- Season 4 checkpoint RECORDED standings from notebook
-- Format in notebook: Club | G (goals for) | L (goals against) | Pts
-- won/drawn/lost derived: played=afterRound, pts=3w+d, played=w+d+l
DO $$
DECLARE
  v_season UUID;
  t_ace UUID; t_ben UUID; t_eld UUID; t_ful UUID;
  t_hri UUID; t_men UUID; t_miz UUID; t_nru UUID;
  t_not UUID; t_ite UUID; t_occ UUID; t_pul UUID;
  t_rab UUID; t_tat UUID; t_twi UUID; t_dom UUID;
BEGIN
  SELECT s.id INTO v_season FROM seasons s JOIN competitions c ON s.competition_id = c.id
    WHERE s.name = 'Season 4' AND c.name = 'Othelose League' LIMIT 1;

  SELECT id INTO t_ace FROM teams WHERE name = 'Acer Robert' LIMIT 1;
  SELECT id INTO t_ben FROM teams WHERE name = 'Ben Derber' LIMIT 1;
  SELECT id INTO t_eld FROM teams WHERE name = 'Eldiadion' LIMIT 1;
  SELECT id INTO t_ful FROM teams WHERE name = 'Fulhaster' LIMIT 1;
  SELECT id INTO t_hri FROM teams WHERE name = 'Hrilling Best' LIMIT 1;
  SELECT id INTO t_men FROM teams WHERE name = 'Mener Lying' LIMIT 1;
  SELECT id INTO t_miz FROM teams WHERE name = 'Mize Mole' LIMIT 1;
  SELECT id INTO t_nru FROM teams WHERE name = 'Nrucaste' LIMIT 1;
  SELECT id INTO t_not FROM teams WHERE name = 'Notes Valzine' LIMIT 1;
  SELECT id INTO t_ite FROM teams WHERE name = 'Iteser Icer' LIMIT 1;
  SELECT id INTO t_occ FROM teams WHERE name = 'Occating Menchiote' LIMIT 1;
  SELECT id INTO t_pul FROM teams WHERE name = 'Pulzerr' LIMIT 1;
  SELECT id INTO t_rab FROM teams WHERE name = 'Rab Pladineer' LIMIT 1;
  SELECT id INTO t_tat FROM teams WHERE name = 'Tatines David' LIMIT 1;
  SELECT id INTO t_twi FROM teams WHERE name = 'Twine Oner' LIMIT 1;
  SELECT id INTO t_dom FROM teams WHERE name = 'Domehampton' LIMIT 1;

  -- After L5
  INSERT INTO standings (season_id,team_id,type,after_round,played,goals_for,goals_against,points,won,drawn,lost) VALUES
    (v_season,t_ace,'RECORDED',5,5,12,2,13,4,1,0),(v_season,t_men,'RECORDED',5,5,10,4,12,4,0,1),
    (v_season,t_hri,'RECORDED',5,5,9,5,11,3,2,0),(v_season,t_rab,'RECORDED',5,5,7,4,10,3,1,1),
    (v_season,t_ful,'RECORDED',5,5,8,2,9,3,0,2),(v_season,t_miz,'RECORDED',5,5,10,7,9,2,3,0),
    (v_season,t_tat,'RECORDED',5,5,7,6,8,2,2,1),(v_season,t_dom,'RECORDED',5,5,5,8,7,2,1,2),
    (v_season,t_ben,'RECORDED',5,5,4,8,6,2,0,3),(v_season,t_pul,'RECORDED',5,5,7,6,5,1,2,2),
    (v_season,t_not,'RECORDED',5,5,7,10,5,1,2,2),(v_season,t_nru,'RECORDED',5,5,6,9,4,1,1,3),
    (v_season,t_ite,'RECORDED',5,5,5,10,4,1,1,3),(v_season,t_eld,'RECORDED',5,5,3,5,3,1,0,4),
    (v_season,t_occ,'RECORDED',5,5,6,11,3,1,0,4),(v_season,t_twi,'RECORDED',5,5,3,9,1,0,1,4);

  -- After L7
  INSERT INTO standings (season_id,team_id,type,after_round,played,goals_for,goals_against,points,won,drawn,lost) VALUES
    (v_season,t_hri,'RECORDED',7,7,12,5,17,5,2,0),(v_season,t_rab,'RECORDED',7,7,11,6,16,5,1,1),
    (v_season,t_miz,'RECORDED',7,7,15,9,15,4,3,0),(v_season,t_ace,'RECORDED',7,7,16,8,14,4,2,1),
    (v_season,t_men,'RECORDED',7,7,10,6,13,4,1,2),(v_season,t_ful,'RECORDED',7,7,12,4,12,4,0,3),
    (v_season,t_pul,'RECORDED',7,7,11,7,11,3,2,2),(v_season,t_occ,'RECORDED',7,7,13,11,9,3,0,4),
    (v_season,t_tat,'RECORDED',7,7,9,12,8,2,2,3),(v_season,t_ite,'RECORDED',7,7,9,13,8,2,2,3),
    (v_season,t_dom,'RECORDED',7,7,5,10,8,2,2,3),(v_season,t_ben,'RECORDED',7,7,6,11,7,2,1,4),
    (v_season,t_nru,'RECORDED',7,7,6,10,5,1,2,4),(v_season,t_not,'RECORDED',7,7,8,18,5,1,2,4),
    (v_season,t_twi,'RECORDED',7,7,5,11,4,1,1,5),(v_season,t_eld,'RECORDED',7,7,4,8,3,1,0,6);

  -- After L9
  INSERT INTO standings (season_id,team_id,type,after_round,played,goals_for,goals_against,points,won,drawn,lost) VALUES
    (v_season,t_ace,'RECORDED',9,9,21,12,18,5,3,1),(v_season,t_miz,'RECORDED',9,9,17,10,18,5,3,1),
    (v_season,t_hri,'RECORDED',9,9,14,8,18,5,3,1),(v_season,t_men,'RECORDED',9,9,12,7,17,5,2,2),
    (v_season,t_rab,'RECORDED',9,9,13,9,17,5,2,2),(v_season,t_pul,'RECORDED',9,9,13,8,15,4,3,2),
    (v_season,t_ful,'RECORDED',9,9,13,6,13,4,1,4),(v_season,t_ben,'RECORDED',9,9,12,14,13,4,1,4),
    (v_season,t_ite,'RECORDED',9,9,11,14,12,3,3,3),(v_season,t_occ,'RECORDED',9,9,13,13,10,3,1,5),
    (v_season,t_tat,'RECORDED',9,9,12,15,10,3,1,5),(v_season,t_dom,'RECORDED',9,9,9,15,9,2,3,4),
    (v_season,t_not,'RECORDED',9,9,14,21,9,2,3,4),(v_season,t_eld,'RECORDED',9,9,8,11,7,2,1,6),
    (v_season,t_twi,'RECORDED',9,9,5,12,5,1,2,6),(v_season,t_nru,'RECORDED',9,9,6,15,5,1,2,6);

  -- After L11
  INSERT INTO standings (season_id,team_id,type,after_round,played,goals_for,goals_against,points,won,drawn,lost) VALUES
    (v_season,t_hri,'RECORDED',11,11,19,9,24,7,3,1),(v_season,t_men,'RECORDED',11,11,19,10,23,7,2,2),
    (v_season,t_ace,'RECORDED',11,11,25,16,21,6,3,2),(v_season,t_miz,'RECORDED',11,11,20,12,21,6,3,2),
    (v_season,t_rab,'RECORDED',11,11,14,10,19,6,1,4),(v_season,t_ful,'RECORDED',11,11,16,10,16,5,1,5),
    (v_season,t_pul,'RECORDED',11,11,15,14,16,4,4,3),(v_season,t_ben,'RECORDED',11,11,14,16,16,5,1,5),
    (v_season,t_occ,'RECORDED',11,11,15,13,14,4,2,5),(v_season,t_tat,'RECORDED',11,11,16,17,13,3,4,4),
    (v_season,t_ite,'RECORDED',11,11,14,20,13,3,4,4),(v_season,t_not,'RECORDED',11,11,15,23,10,2,4,5),
    (v_season,t_dom,'RECORDED',11,11,10,18,10,3,1,7),(v_season,t_nru,'RECORDED',11,11,9,17,9,3,0,8),
    (v_season,t_eld,'RECORDED',11,11,9,13,8,2,2,7),(v_season,t_twi,'RECORDED',11,11,7,16,6,1,3,7);

  -- After L13
  INSERT INTO standings (season_id,team_id,type,after_round,played,goals_for,goals_against,points,won,drawn,lost) VALUES
    (v_season,t_hri,'RECORDED',13,13,23,11,27,8,3,2),(v_season,t_ace,'RECORDED',13,13,27,17,25,7,4,2),
    (v_season,t_men,'RECORDED',13,13,22,16,23,7,2,4),(v_season,t_rab,'RECORDED',13,13,17,11,23,7,2,4),
    (v_season,t_ben,'RECORDED',13,13,19,19,22,7,1,5),(v_season,t_miz,'RECORDED',13,13,22,16,21,6,3,4),
    (v_season,t_ful,'RECORDED',13,13,20,13,20,6,2,5),(v_season,t_occ,'RECORDED',13,13,20,14,20,6,2,5),
    (v_season,t_pul,'RECORDED',13,13,17,18,17,4,5,4),(v_season,t_tat,'RECORDED',13,13,18,19,16,4,4,5),
    (v_season,t_not,'RECORDED',13,13,17,23,16,4,4,5),(v_season,t_ite,'RECORDED',13,13,15,22,14,3,5,5),
    (v_season,t_nru,'RECORDED',13,13,11,18,12,4,0,9),(v_season,t_eld,'RECORDED',13,13,14,18,12,3,3,7),
    (v_season,t_dom,'RECORDED',13,13,13,20,11,3,2,8),(v_season,t_twi,'RECORDED',13,13,8,22,6,1,3,9);

  -- After L15
  INSERT INTO standings (season_id,team_id,type,after_round,played,goals_for,goals_against,points,won,drawn,lost) VALUES
    (v_season,t_hri,'RECORDED',15,15,26,13,31,9,4,2),(v_season,t_ace,'RECORDED',15,15,31,19,31,9,4,2),
    (v_season,t_men,'RECORDED',15,15,26,17,27,8,3,4),(v_season,t_ben,'RECORDED',15,15,20,19,26,8,2,5),
    (v_season,t_rab,'RECORDED',15,15,18,12,25,8,1,6),(v_season,t_ful,'RECORDED',15,15,26,14,24,7,3,5),
    (v_season,t_miz,'RECORDED',15,15,23,17,23,7,2,6),(v_season,t_not,'RECORDED',15,15,19,26,19,5,4,6),
    (v_season,t_tat,'RECORDED',15,15,22,23,18,5,3,7),(v_season,t_pul,'RECORDED',15,15,19,22,17,4,5,6),
    (v_season,t_ite,'RECORDED',15,15,17,24,16,4,4,7),(v_season,t_nru,'RECORDED',15,15,15,22,15,5,0,10),
    (v_season,t_occ,'RECORDED',15,15,23,18,21,6,3,6),(v_season,t_dom,'RECORDED',15,15,13,27,14,4,2,9),
    (v_season,t_eld,'RECORDED',15,15,15,22,11,3,2,10),(v_season,t_twi,'RECORDED',15,15,12,28,10,2,4,9);

  -- After L17
  INSERT INTO standings (season_id,team_id,type,after_round,played,goals_for,goals_against,points,won,drawn,lost) VALUES
    (v_season,t_hri,'RECORDED',17,17,29,14,35,10,5,2),(v_season,t_ace,'RECORDED',17,17,31,23,31,9,4,4),
    (v_season,t_men,'RECORDED',17,17,30,24,29,8,5,4),(v_season,t_ben,'RECORDED',17,17,21,21,29,9,2,6),
    (v_season,t_rab,'RECORDED',17,17,22,15,28,9,1,7),(v_season,t_ful,'RECORDED',17,17,29,17,27,8,3,6),
    (v_season,t_miz,'RECORDED',17,17,28,21,27,8,3,6),(v_season,t_not,'RECORDED',17,17,23,27,25,7,4,6),
    (v_season,t_occ,'RECORDED',17,17,23,20,21,6,3,8),(v_season,t_tat,'RECORDED',17,17,25,25,21,6,3,8),
    (v_season,t_pul,'RECORDED',17,17,22,25,19,5,4,8),(v_season,t_ite,'RECORDED',17,17,20,26,19,5,4,8),
    (v_season,t_nru,'RECORDED',17,17,18,24,19,6,1,10),(v_season,t_dom,'RECORDED',17,17,17,32,15,4,3,10),
    (v_season,t_eld,'RECORDED',17,17,18,26,14,4,2,11),(v_season,t_twi,'RECORDED',17,17,12,28,10,2,4,11);

  -- After L19
  INSERT INTO standings (season_id,team_id,type,after_round,played,goals_for,goals_against,points,won,drawn,lost) VALUES
    (v_season,t_ace,'RECORDED',19,19,35,25,37,11,4,4),(v_season,t_hri,'RECORDED',19,19,30,18,35,10,5,4),
    (v_season,t_ben,'RECORDED',19,19,26,25,32,10,2,7),(v_season,t_miz,'RECORDED',19,19,30,22,31,9,4,6),
    (v_season,t_ful,'RECORDED',19,19,32,19,30,9,3,7),(v_season,t_men,'RECORDED',19,19,32,24,30,9,3,7),
    (v_season,t_rab,'RECORDED',19,19,22,18,28,9,1,9),(v_season,t_not,'RECORDED',19,19,26,28,28,8,4,7),
    (v_season,t_occ,'RECORDED',19,19,25,21,24,7,3,9),(v_season,t_tat,'RECORDED',19,19,26,28,24,7,3,9),
    (v_season,t_pul,'RECORDED',19,19,25,26,23,6,5,8),(v_season,t_ite,'RECORDED',19,19,22,30,23,6,5,8),
    (v_season,t_nru,'RECORDED',19,19,22,28,25,8,1,10),(v_season,t_dom,'RECORDED',19,19,22,31,21,6,3,10),
    (v_season,t_eld,'RECORDED',19,19,21,28,18,5,3,11),(v_season,t_twi,'RECORDED',19,19,14,30,13,3,4,12);

  -- After L21
  INSERT INTO standings (season_id,team_id,type,after_round,played,goals_for,goals_against,points,won,drawn,lost) VALUES
    (v_season,t_hri,'RECORDED',21,21,36,20,41,12,5,4),(v_season,t_ace,'RECORDED',21,21,36,26,39,12,3,6),
    (v_season,t_ben,'RECORDED',21,21,30,27,36,11,3,7),(v_season,t_men,'RECORDED',21,21,34,27,33,10,3,8),
    (v_season,t_miz,'RECORDED',21,21,31,24,32,9,5,7),(v_season,t_rab,'RECORDED',21,21,25,20,32,10,2,9),
    (v_season,t_ful,'RECORDED',21,21,36,24,31,9,4,8),(v_season,t_not,'RECORDED',21,21,28,29,31,9,4,8),
    (v_season,t_tat,'RECORDED',21,21,30,30,28,8,4,9),(v_season,t_pul,'RECORDED',21,21,27,30,24,7,3,11),
    (v_season,t_nru,'RECORDED',21,21,22,28,25,8,1,12),(v_season,t_ite,'RECORDED',21,21,22,30,23,6,5,10),
    (v_season,t_occ,'RECORDED',21,21,27,23,27,8,3,10),(v_season,t_dom,'RECORDED',21,21,22,31,21,6,3,12),
    (v_season,t_eld,'RECORDED',21,21,22,31,21,6,3,12),(v_season,t_twi,'RECORDED',21,21,15,31,15,4,3,14);

  -- After L23
  INSERT INTO standings (season_id,team_id,type,after_round,played,goals_for,goals_against,points,won,drawn,lost) VALUES
    (v_season,t_hri,'RECORDED',23,23,37,20,45,13,6,4),(v_season,t_ace,'RECORDED',23,23,40,27,45,14,3,6),
    (v_season,t_ben,'RECORDED',23,23,37,31,42,13,3,7),(v_season,t_ful,'RECORDED',23,23,38,26,34,10,4,9),
    (v_season,t_men,'RECORDED',23,23,34,28,34,10,4,9),(v_season,t_tat,'RECORDED',23,23,35,31,34,10,4,9),
    (v_season,t_rab,'RECORDED',23,23,26,23,33,10,3,10),(v_season,t_miz,'RECORDED',23,23,32,27,32,9,5,9),
    (v_season,t_not,'RECORDED',23,23,30,33,32,9,5,9),(v_season,t_occ,'RECORDED',23,23,30,25,31,9,4,10),
    (v_season,t_pul,'RECORDED',23,23,31,33,28,8,4,11),(v_season,t_nru,'RECORDED',23,23,27,32,28,9,1,13),
    (v_season,t_ite,'RECORDED',23,23,22,33,24,6,6,11),(v_season,t_eld,'RECORDED',23,23,24,35,22,6,4,13),
    (v_season,t_dom,'RECORDED',23,23,24,35,22,6,4,13),(v_season,t_twi,'RECORDED',23,23,19,35,17,4,5,14);

  -- After L25
  INSERT INTO standings (season_id,team_id,type,after_round,played,goals_for,goals_against,points,won,drawn,lost) VALUES
    (v_season,t_ace,'RECORDED',25,25,43,29,49,15,4,6),(v_season,t_hri,'RECORDED',25,25,40,23,47,14,5,6),
    (v_season,t_ben,'RECORDED',25,25,39,34,43,13,4,8),(v_season,t_ful,'RECORDED',25,25,41,26,38,11,5,9),
    (v_season,t_miz,'RECORDED',25,25,33,27,36,11,3,11),(v_season,t_tat,'RECORDED',25,25,37,33,36,11,3,11),
    (v_season,t_rab,'RECORDED',25,25,29,25,36,11,3,11),(v_season,t_men,'RECORDED',25,25,35,32,34,10,4,11),
    (v_season,t_nru,'RECORDED',25,25,31,33,34,11,1,13),(v_season,t_not,'RECORDED',25,25,32,37,33,9,6,10),
    (v_season,t_occ,'RECORDED',25,25,31,28,32,9,5,11),(v_season,t_pul,'RECORDED',25,25,34,36,31,9,4,12),
    (v_season,t_ite,'RECORDED',25,25,28,39,28,7,7,11),(v_season,t_eld,'RECORDED',25,25,26,37,24,6,6,13),
    (v_season,t_dom,'RECORDED',25,25,27,48,25,7,4,14),(v_season,t_twi,'RECORDED',25,25,25,46,20,5,5,15);

  -- After L27
  INSERT INTO standings (season_id,team_id,type,after_round,played,goals_for,goals_against,points,won,drawn,lost) VALUES
    (v_season,t_hri,'RECORDED',27,27,47,27,53,16,5,6),(v_season,t_ace,'RECORDED',27,27,47,31,53,16,5,6),
    (v_season,t_ben,'RECORDED',27,27,41,36,46,14,4,9),(v_season,t_ful,'RECORDED',27,27,44,28,42,13,3,11),
    (v_season,t_tat,'RECORDED',27,27,39,34,40,12,4,11),(v_season,t_miz,'RECORDED',27,27,37,29,39,12,3,12),
    (v_season,t_rab,'RECORDED',27,27,30,26,39,12,3,12),(v_season,t_men,'RECORDED',27,27,37,32,38,11,5,11),
    (v_season,t_not,'RECORDED',27,27,34,38,37,10,7,10),(v_season,t_occ,'RECORDED',27,27,33,28,36,11,3,13),
    (v_season,t_nru,'RECORDED',27,27,31,37,34,11,1,15),(v_season,t_pul,'RECORDED',27,27,37,41,32,9,5,13),
    (v_season,t_ite,'RECORDED',27,27,28,39,28,7,7,13),(v_season,t_dom,'RECORDED',27,27,27,48,25,7,4,16),
    (v_season,t_eld,'RECORDED',27,27,26,40,24,7,3,17),(v_season,t_twi,'RECORDED',27,27,25,46,20,5,5,17);

END $$;
