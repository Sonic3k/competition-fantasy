-- Season 4: "Chess Othello and Roarden 4" - 16 teams, 30 rounds
DO $$
DECLARE
  v_uni UUID;
  v_comp UUID;
  v_season UUID;
  -- Teams
  t_ace UUID; t_ben UUID; t_eld UUID; t_ful UUID;
  t_hri UUID; t_men UUID; t_miz UUID; t_nru UUID;
  t_not UUID; t_ite UUID; t_occ UUID; t_pul UUID;
  t_rab UUID; t_tat UUID; t_twi UUID; t_dom UUID;
  -- Rounds
  r UUID;
BEGIN
  -- Get or create universe
  SELECT id INTO v_uni FROM universes WHERE name = 'Othelose' LIMIT 1;
  IF v_uni IS NULL THEN
    INSERT INTO universes (name, description) VALUES ('Othelose', 'My fantasy Premier League') RETURNING id INTO v_uni;
  END IF;

  -- Create teams
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Acer Robert', 'Ace', 'CLUB', '#0066cc', '#ffcc00', v_uni) RETURNING id INTO t_ace;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Ben Derber', 'Ben', 'CLUB', '#333333', '#0066cc', v_uni) RETURNING id INTO t_ben;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Eldiadion', 'Eld', 'CLUB', '#990000', '#ffffff', v_uni) RETURNING id INTO t_eld;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Fulhaster', 'Ful', 'CLUB', '#336633', '#ffffff', v_uni) RETURNING id INTO t_ful;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Hrilling Best', 'Hri', 'CLUB', '#00cc66', '#000000', v_uni) RETURNING id INTO t_hri;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Mener Lying', 'Men', 'CLUB', '#cc0000', '#0000cc', v_uni) RETURNING id INTO t_men;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Mize Mole', 'Miz', 'CLUB', '#cc3366', '#0066cc', v_uni) RETURNING id INTO t_miz;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Nrucaste', 'Nru', 'CLUB', '#003366', '#ffffff', v_uni) RETURNING id INTO t_nru;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Notes Valzine', 'Not', 'CLUB', '#6633cc', '#ffffff', v_uni) RETURNING id INTO t_not;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Iteser Icer', 'Ite', 'CLUB', '#ffffff', '#003366', v_uni) RETURNING id INTO t_ite;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Occating Menchiote', 'Occ', 'CLUB', '#cc6600', '#ffffff', v_uni) RETURNING id INTO t_occ;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Pulzerr', 'Pul', 'CLUB', '#ff66cc', '#ffffff', v_uni) RETURNING id INTO t_pul;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Rab Pladineer', 'Rab', 'CLUB', '#0033cc', '#cc0000', v_uni) RETURNING id INTO t_rab;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Tatines David', 'Tat', 'CLUB', '#cc0033', '#ffffff', v_uni) RETURNING id INTO t_tat;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Twine Oner', 'Twi', 'CLUB', '#009999', '#ffffff', v_uni) RETURNING id INTO t_twi;
  INSERT INTO teams (id, name, short_name, type, primary_color, secondary_color, universe_id) VALUES
    (gen_random_uuid(), 'Domehampton', 'Dom', 'CLUB', '#0066cc', '#000000', v_uni) RETURNING id INTO t_dom;

  -- Competition
  INSERT INTO competitions (name, type, team_level, universe_id) VALUES ('Othelose League', 'LEAGUE', 'CLUB', v_uni) RETURNING id INTO v_comp;

  -- Season 4
  INSERT INTO seasons (name, year, status, competition_id) VALUES ('Season 4', 2005, 'COMPLETED', v_comp) RETURNING id INTO v_season;

  -- Link teams to season
  INSERT INTO season_teams (season_id, team_id) VALUES
    (v_season, t_ace), (v_season, t_ben), (v_season, t_eld), (v_season, t_ful),
    (v_season, t_hri), (v_season, t_men), (v_season, t_miz), (v_season, t_nru),
    (v_season, t_not), (v_season, t_ite), (v_season, t_occ), (v_season, t_pul),
    (v_season, t_rab), (v_season, t_tat), (v_season, t_twi), (v_season, t_dom);

  -- L1
  INSERT INTO rounds (round_number, name, season_id) VALUES (1, 'L1', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_eld, t_ite, 0, 1, 'COMPLETED'), (r, t_twi, t_ace, 0, 3, 'COMPLETED'),
    (r, t_occ, t_nru, 2, 1, 'COMPLETED'), (r, t_tat, t_ben, 1, 0, 'COMPLETED'),
    (r, t_rab, t_hri, 2, 3, 'COMPLETED'), (r, t_pul, t_miz, 2, 2, 'COMPLETED'),
    (r, t_ful, t_not, 3, 1, 'COMPLETED'), (r, t_men, t_dom, 4, 0, 'COMPLETED');

  -- L2
  INSERT INTO rounds (round_number, name, season_id) VALUES (2, 'L2', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_nru, t_men, 0, 2, 'COMPLETED'), (r, t_hri, t_pul, 1, 1, 'COMPLETED'),
    (r, t_not, t_twi, 3, 2, 'COMPLETED'), (r, t_miz, t_tat, 2, 2, 'COMPLETED'),
    (r, t_ite, t_ful, 1, 1, 'COMPLETED'), (r, t_ace, t_rab, 0, 0, 'COMPLETED'),
    (r, t_ben, t_occ, 2, 1, 'COMPLETED'), (r, t_dom, t_eld, 1, 0, 'COMPLETED');

  -- L3
  INSERT INTO rounds (round_number, name, season_id) VALUES (3, 'L3', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_miz, t_eld, 1, 0, 'COMPLETED'), (r, t_nru, t_hri, 2, 3, 'COMPLETED'),
    (r, t_men, t_pul, 1, 0, 'COMPLETED'), (r, t_ace, t_ben, 3, 0, 'COMPLETED'),
    (r, t_rab, t_occ, 2, 0, 'COMPLETED'), (r, t_tat, t_not, 1, 1, 'COMPLETED'),
    (r, t_twi, t_ful, 0, 1, 'COMPLETED'), (r, t_ite, t_dom, 1, 3, 'COMPLETED');

  -- L4
  INSERT INTO rounds (round_number, name, season_id) VALUES (4, 'L4', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_ful, t_nru, 0, 0, 'COMPLETED'), (r, t_pul, t_ite, 3, 0, 'COMPLETED'),
    (r, t_occ, t_ace, 2, 4, 'COMPLETED'), (r, t_eld, t_men, 1, 2, 'COMPLETED'),
    (r, t_not, t_miz, 2, 2, 'COMPLETED'), (r, t_hri, t_ben, 2, 0, 'COMPLETED'),
    (r, t_tat, t_rab, 1, 2, 'COMPLETED'), (r, t_dom, t_twi, 1, 1, 'COMPLETED');

  -- L5
  INSERT INTO rounds (round_number, name, season_id) VALUES (5, 'L5', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_ben, t_pul, 2, 1, 'COMPLETED'), (r, t_nru, t_ite, 3, 2, 'COMPLETED'),
    (r, t_ace, t_dom, 2, 0, 'COMPLETED'), (r, t_miz, t_men, 3, 1, 'COMPLETED'),
    (r, t_occ, t_tat, 1, 2, 'COMPLETED'), (r, t_twi, t_rab, 0, 1, 'COMPLETED'),
    (r, t_not, t_eld, 0, 2, 'COMPLETED'), (r, t_hri, t_ful, 0, 0, 'COMPLETED');

  -- L6
  INSERT INTO rounds (round_number, name, season_id) VALUES (6, 'L6', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_rab, t_nru, 1, 0, 'COMPLETED'), (r, t_twi, t_miz, 0, 1, 'COMPLETED'),
    (r, t_men, t_ben, 0, 0, 'COMPLETED'), (r, t_ful, t_tat, 4, 1, 'COMPLETED'),
    (r, t_dom, t_occ, 0, 2, 'COMPLETED'), (r, t_ite, t_ace, 2, 2, 'COMPLETED'),
    (r, t_eld, t_hri, 0, 1, 'COMPLETED'), (r, t_pul, t_not, 3, 1, 'COMPLETED');

  -- L7
  INSERT INTO rounds (round_number, name, season_id) VALUES (7, 'L7', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_hri, t_men, 2, 0, 'COMPLETED'), (r, t_occ, t_not, 5, 0, 'COMPLETED'),
    (r, t_eld, t_twi, 1, 2, 'COMPLETED'), (r, t_dom, t_nru, 0, 0, 'COMPLETED'),
    (r, t_tat, t_ite, 1, 2, 'COMPLETED'), (r, t_pul, t_ful, 1, 0, 'COMPLETED'),
    (r, t_ben, t_rab, 2, 3, 'COMPLETED'), (r, t_miz, t_ace, 4, 2, 'COMPLETED');

  -- L8
  INSERT INTO rounds (round_number, name, season_id) VALUES (8, 'L8', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_not, t_ace, 3, 3, 'COMPLETED'), (r, t_tat, t_dom, 1, 1, 'COMPLETED'),
    (r, t_miz, t_occ, 2, 0, 'COMPLETED'), (r, t_men, t_rab, 1, 1, 'COMPLETED'),
    (r, t_ful, t_eld, 1, 2, 'COMPLETED'), (r, t_twi, t_pul, 0, 0, 'COMPLETED'),
    (r, t_ite, t_hri, 1, 1, 'COMPLETED'), (r, t_nru, t_ben, 0, 2, 'COMPLETED');

  -- L9
  INSERT INTO rounds (round_number, name, season_id) VALUES (9, 'L9', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_ite, t_miz, 1, 0, 'COMPLETED'), (r, t_rab, t_pul, 1, 2, 'COMPLETED'),
    (r, t_ace, t_hri, 2, 1, 'COMPLETED'), (r, t_not, t_nru, 3, 0, 'COMPLETED'),
    (r, t_ben, t_dom, 4, 3, 'COMPLETED'), (r, t_occ, t_ful, 0, 0, 'COMPLETED'),
    (r, t_men, t_twi, 1, 0, 'COMPLETED'), (r, t_eld, t_tat, 2, 2, 'COMPLETED');

  -- L10
  INSERT INTO rounds (round_number, name, season_id) VALUES (10, 'L10', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_ace, t_men, 2, 3, 'COMPLETED'), (r, t_occ, t_eld, 0, 0, 'COMPLETED'),
    (r, t_ful, t_miz, 1, 3, 'COMPLETED'), (r, t_twi, t_ite, 2, 2, 'COMPLETED'),
    (r, t_pul, t_nru, 2, 2, 'COMPLETED'), (r, t_ben, t_not, 1, 0, 'COMPLETED'),
    (r, t_dom, t_rab, 0, 0, 'COMPLETED'), (r, t_hri, t_tat, 2, 0, 'COMPLETED');

  -- L11
  INSERT INTO rounds (round_number, name, season_id) VALUES (11, 'L11', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_tat, t_pul, 4, 0, 'COMPLETED'), (r, t_ful, t_ben, 2, 1, 'COMPLETED'),
    (r, t_twi, t_occ, 0, 2, 'COMPLETED'), (r, t_nru, t_miz, 1, 0, 'COMPLETED'),
    (r, t_hri, t_dom, 3, 1, 'COMPLETED'), (r, t_eld, t_ace, 1, 2, 'COMPLETED'),
    (r, t_men, t_ite, 4, 1, 'COMPLETED'), (r, t_rab, t_not, 1, 1, 'COMPLETED');

  -- L12
  INSERT INTO rounds (round_number, name, season_id) VALUES (12, 'L12', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_rab, t_eld, 2, 0, 'COMPLETED'), (r, t_not, t_ite, 1, 0, 'COMPLETED'),
    (r, t_pul, t_dom, 1, 1, 'COMPLETED'), (r, t_ace, t_ful, 0, 0, 'COMPLETED'),
    (r, t_occ, t_men, 2, 0, 'COMPLETED'), (r, t_nru, t_tat, 0, 1, 'COMPLETED'),
    (r, t_hri, t_twi, 4, 1, 'COMPLETED'), (r, t_miz, t_ben, 1, 2, 'COMPLETED');

  -- L13
  INSERT INTO rounds (round_number, name, season_id) VALUES (13, 'L13', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_twi, t_nru, 0, 2, 'COMPLETED'), (r, t_pul, t_occ, 1, 3, 'COMPLETED'),
    (r, t_tat, t_ace, 1, 2, 'COMPLETED'), (r, t_ben, t_eld, 3, 2, 'COMPLETED'),
    (r, t_ite, t_rab, 1, 1, 'COMPLETED'), (r, t_dom, t_miz, 2, 1, 'COMPLETED'),
    (r, t_men, t_ful, 3, 4, 'COMPLETED'), (r, t_not, t_hri, 1, 0, 'COMPLETED');

  -- L14
  INSERT INTO rounds (round_number, name, season_id) VALUES (14, 'L14', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_men, t_not, 3, 0, 'COMPLETED'), (r, t_ace, t_nru, 2, 1, 'COMPLETED'),
    (r, t_occ, t_hri, 1, 2, 'COMPLETED'), (r, t_twi, t_tat, 3, 3, 'COMPLETED'),
    (r, t_ful, t_dom, 5, 0, 'COMPLETED'), (r, t_ite, t_ben, 0, 0, 'COMPLETED'),
    (r, t_eld, t_pul, 2, 1, 'COMPLETED'), (r, t_miz, t_rab, 0, 0, 'COMPLETED');

  -- L15
  INSERT INTO rounds (round_number, name, season_id) VALUES (15, 'L15', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_rab, t_ful, 1, 1, 'COMPLETED'), (r, t_ben, t_twi, 1, 0, 'COMPLETED'),
    (r, t_hri, t_miz, 1, 1, 'COMPLETED'), (r, t_tat, t_men, 1, 1, 'COMPLETED'),
    (r, t_nru, t_eld, 3, 2, 'COMPLETED'), (r, t_pul, t_ace, 1, 2, 'COMPLETED'),
    (r, t_not, t_dom, 2, 0, 'COMPLETED'), (r, t_occ, t_ite, 2, 2, 'COMPLETED');

  -- L16
  INSERT INTO rounds (round_number, name, season_id) VALUES (16, 'L16', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_miz, t_pul, 3, 3, 'COMPLETED'), (r, t_nru, t_occ, 1, 0, 'COMPLETED'),
    (r, t_ite, t_eld, 2, 0, 'COMPLETED'), (r, t_ace, t_twi, 0, 1, 'COMPLETED'),
    (r, t_dom, t_men, 2, 2, 'COMPLETED'), (r, t_not, t_ful, 2, 1, 'COMPLETED'),
    (r, t_hri, t_rab, 3, 1, 'COMPLETED'), (r, t_ben, t_tat, 0, 2, 'COMPLETED');

  -- L17
  INSERT INTO rounds (round_number, name, season_id) VALUES (17, 'L17', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_rab, t_ace, 3, 0, 'COMPLETED'), (r, t_twi, t_not, 0, 2, 'COMPLETED'),
    (r, t_ful, t_ite, 2, 1, 'COMPLETED'), (r, t_eld, t_dom, 3, 2, 'COMPLETED'),
    (r, t_pul, t_hri, 0, 0, 'COMPLETED'), (r, t_tat, t_miz, 1, 2, 'COMPLETED'),
    (r, t_occ, t_ben, 0, 1, 'COMPLETED'), (r, t_men, t_nru, 2, 2, 'COMPLETED');

  -- L18
  INSERT INTO rounds (round_number, name, season_id) VALUES (18, 'L18', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_hri, t_nru, 0, 1, 'COMPLETED'), (r, t_ful, t_twi, 2, 0, 'COMPLETED'),
    (r, t_occ, t_rab, 2, 0, 'COMPLETED'), (r, t_ben, t_ace, 2, 3, 'COMPLETED'),
    (r, t_eld, t_miz, 1, 1, 'COMPLETED'), (r, t_dom, t_ite, 1, 0, 'COMPLETED'),
    (r, t_pul, t_men, 1, 1, 'COMPLETED'), (r, t_not, t_tat, 3, 0, 'COMPLETED');

  -- L19
  INSERT INTO rounds (round_number, name, season_id) VALUES (19, 'L19', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_ite, t_pul, 0, 2, 'COMPLETED'), (r, t_men, t_eld, 1, 2, 'COMPLETED'),
    (r, t_miz, t_not, 1, 0, 'COMPLETED'), (r, t_rab, t_tat, 0, 1, 'COMPLETED'),
    (r, t_nru, t_ful, 2, 1, 'COMPLETED'), (r, t_twi, t_dom, 2, 0, 'COMPLETED'),
    (r, t_ace, t_occ, 1, 0, 'COMPLETED'), (r, t_ben, t_hri, 3, 1, 'COMPLETED');

  -- L20
  INSERT INTO rounds (round_number, name, season_id) VALUES (20, 'L20', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_dom, t_ace, 0, 0, 'COMPLETED'), (r, t_men, t_miz, 2, 1, 'COMPLETED'),
    (r, t_eld, t_not, 1, 0, 'COMPLETED'), (r, t_rab, t_twi, 1, 1, 'COMPLETED'),
    (r, t_tat, t_occ, 2, 0, 'COMPLETED'), (r, t_pul, t_ben, 2, 2, 'COMPLETED'),
    (r, t_ful, t_hri, 2, 3, 'COMPLETED'), (r, t_ite, t_nru, 1, 0, 'COMPLETED');

  -- L21
  INSERT INTO rounds (round_number, name, season_id) VALUES (21, 'L21', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_ben, t_men, 2, 0, 'COMPLETED'), (r, t_tat, t_ful, 2, 2, 'COMPLETED'),
    (r, t_ace, t_ite, 1, 1, 'COMPLETED'), (r, t_hri, t_eld, 3, 0, 'COMPLETED'),
    (r, t_nru, t_rab, 1, 2, 'COMPLETED'), (r, t_occ, t_dom, 2, 0, 'COMPLETED'),
    (r, t_not, t_pul, 2, 0, 'COMPLETED'), (r, t_miz, t_twi, 0, 0, 'COMPLETED');

  -- L22
  INSERT INTO rounds (round_number, name, season_id) VALUES (22, 'L22', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_twi, t_eld, 2, 2, 'COMPLETED'), (r, t_ful, t_pul, 1, 2, 'COMPLETED'),
    (r, t_not, t_occ, 2, 2, 'COMPLETED'), (r, t_nru, t_dom, 2, 0, 'COMPLETED'),
    (r, t_men, t_hri, 0, 1, 'COMPLETED'), (r, t_ite, t_tat, 0, 3, 'COMPLETED'),
    (r, t_ace, t_miz, 2, 1, 'COMPLETED'), (r, t_rab, t_ben, 1, 3, 'COMPLETED');

  -- L23
  INSERT INTO rounds (round_number, name, season_id) VALUES (23, 'L23', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_occ, t_miz, 1, 0, 'COMPLETED'), (r, t_ben, t_nru, 4, 3, 'COMPLETED'),
    (r, t_rab, t_men, 0, 0, 'COMPLETED'), (r, t_hri, t_ite, 0, 0, 'COMPLETED'),
    (r, t_pul, t_twi, 2, 2, 'COMPLETED'), (r, t_ace, t_not, 2, 0, 'COMPLETED'),
    (r, t_eld, t_ful, 0, 2, 'COMPLETED'), (r, t_dom, t_tat, 1, 2, 'COMPLETED');

  -- L24
  INSERT INTO rounds (round_number, name, season_id) VALUES (24, 'L24', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_hri, t_ace, 2, 2, 'COMPLETED'), (r, t_ful, t_occ, 2, 0, 'COMPLETED'),
    (r, t_dom, t_ben, 1, 0, 'COMPLETED'), (r, t_pul, t_rab, 2, 1, 'COMPLETED'),
    (r, t_miz, t_ite, 1, 0, 'COMPLETED'), (r, t_tat, t_eld, 1, 1, 'COMPLETED'),
    (r, t_twi, t_men, 3, 1, 'COMPLETED'), (r, t_nru, t_not, 2, 0, 'COMPLETED');

  -- L25
  INSERT INTO rounds (round_number, name, season_id) VALUES (25, 'L25', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_ite, t_twi, 5, 2, 'COMPLETED'), (r, t_nru, t_pul, 2, 1, 'COMPLETED'),
    (r, t_eld, t_occ, 1, 1, 'COMPLETED'), (r, t_men, t_ace, 0, 1, 'COMPLETED'),
    (r, t_not, t_ben, 2, 2, 'COMPLETED'), (r, t_miz, t_ful, 0, 0, 'COMPLETED'),
    (r, t_rab, t_dom, 2, 0, 'COMPLETED'), (r, t_tat, t_hri, 1, 1, 'COMPLETED');

  -- L26
  INSERT INTO rounds (round_number, name, season_id) VALUES (26, 'L26', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_miz, t_nru, 3, 0, 'COMPLETED'), (r, t_dom, t_hri, 3, 4, 'COMPLETED'),
    (r, t_ite, t_men, 0, 2, 'COMPLETED'), (r, t_not, t_rab, 1, 0, 'COMPLETED'),
    (r, t_pul, t_tat, 1, 1, 'COMPLETED'), (r, t_occ, t_twi, 2, 0, 'COMPLETED'),
    (r, t_ace, t_eld, 2, 0, 'COMPLETED'), (r, t_ben, t_ful, 0, 1, 'COMPLETED');

  -- L27
  INSERT INTO rounds (round_number, name, season_id) VALUES (27, 'L27', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_dom, t_pul, 4, 2, 'COMPLETED'), (r, t_ful, t_ace, 2, 2, 'COMPLETED'),
    (r, t_tat, t_nru, 1, 0, 'COMPLETED'), (r, t_twi, t_hri, 1, 3, 'COMPLETED'),
    (r, t_eld, t_rab, 0, 1, 'COMPLETED'), (r, t_ben, t_miz, 2, 1, 'COMPLETED'),
    (r, t_men, t_occ, 0, 0, 'COMPLETED'), (r, t_ite, t_not, 1, 1, 'COMPLETED');

  -- L28
  INSERT INTO rounds (round_number, name, season_id) VALUES (28, 'L28', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_eld, t_ben, 3, 2, 'COMPLETED'), (r, t_rab, t_ite, 1, 2, 'COMPLETED'),
    (r, t_ful, t_men, 0, 1, 'COMPLETED'), (r, t_occ, t_pul, 2, 0, 'COMPLETED'),
    (r, t_hri, t_not, 3, 1, 'COMPLETED'), (r, t_ace, t_tat, 2, 0, 'COMPLETED'),
    (r, t_miz, t_dom, 1, 1, 'COMPLETED'), (r, t_nru, t_twi, 0, 0, 'COMPLETED');

  -- L29
  INSERT INTO rounds (round_number, name, season_id) VALUES (29, 'L29', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_nru, t_ace, 1, 1, 'COMPLETED'), (r, t_hri, t_occ, 2, 0, 'COMPLETED'),
    (r, t_rab, t_miz, 2, 1, 'COMPLETED'), (r, t_not, t_men, 2, 2, 'COMPLETED'),
    (r, t_tat, t_twi, 4, 0, 'COMPLETED'), (r, t_dom, t_ful, 1, 4, 'COMPLETED'),
    (r, t_ben, t_ite, 1, 0, 'COMPLETED'), (r, t_pul, t_eld, 2, 2, 'COMPLETED');

  -- L30
  INSERT INTO rounds (round_number, name, season_id) VALUES (30, 'L30', v_season) RETURNING id INTO r;
  INSERT INTO matches (round_id, home_team_id, away_team_id, home_score, away_score, status) VALUES
    (r, t_miz, t_hri, 1, 2, 'COMPLETED'), (r, t_dom, t_not, 1, 1, 'COMPLETED'),
    (r, t_twi, t_ben, 1, 1, 'COMPLETED'), (r, t_ace, t_pul, 2, 3, 'COMPLETED'),
    (r, t_eld, t_nru, 2, 0, 'COMPLETED'), (r, t_ite, t_occ, 0, 0, 'COMPLETED'),
    (r, t_ful, t_rab, 2, 0, 'COMPLETED'), (r, t_men, t_tat, 1, 1, 'COMPLETED');

  -- Final standings
  INSERT INTO standings (season_id, team_id, played, won, drawn, lost, goals_for, goals_against, points) VALUES
    (v_season, t_hri, 30, 18, 8, 4, 54, 29, 62),
    (v_season, t_ace, 30, 16, 9, 5, 52, 35, 57),
    (v_season, t_ben, 30, 15, 5, 10, 45, 40, 50),
    (v_season, t_ful, 30, 13, 9, 8, 50, 30, 48),
    (v_season, t_tat, 30, 11, 11, 8, 44, 37, 44),
    (v_season, t_men, 30, 11, 10, 9, 41, 35, 43),
    (v_season, t_rab, 30, 11, 9, 10, 33, 31, 42),
    (v_season, t_miz, 30, 10, 10, 10, 40, 34, 40),
    (v_season, t_occ, 30, 11, 7, 12, 35, 30, 40),
    (v_season, t_not, 30, 10, 9, 11, 38, 44, 39),
    (v_season, t_pul, 30, 8, 12, 10, 42, 47, 36),
    (v_season, t_nru, 30, 10, 6, 14, 32, 40, 36),
    (v_season, t_ite, 30, 7, 11, 12, 30, 41, 32),
    (v_season, t_eld, 30, 8, 7, 15, 33, 44, 31),
    (v_season, t_dom, 30, 6, 9, 15, 30, 54, 27),
    (v_season, t_twi, 30, 4, 10, 16, 26, 51, 22);

END $$;
