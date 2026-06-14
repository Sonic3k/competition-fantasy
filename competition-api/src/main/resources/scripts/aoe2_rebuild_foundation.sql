-- =====================================================================
-- AOE2 foundation rebuild (complete data) + clean World Cup scaffold.
--
-- 1. CLEANUP  - delete the broken "World Cup 2022" (cascades its season /
--    stages / groups / rounds / matches / standings) and the duplicate
--    "Mayans" nation + team (the real civ is "Maya").
--
-- 2. ADD MISSING CIVS - Mapuche / Muisca / Tupi were the only 3 civs (of
--    the full roster minus Shu/Wei/Wu) not yet present. Insert them WITH
--    full fields (code + home/away colours) matching the existing 47, and
--    backfill those fields if they were already created bare.
--
-- 3. NATIONAL TEAMS - ensure EVERY nation has exactly one NATIONAL team,
--    styled from the nation: name, code -> short_name, nation colours ->
--    kit colours. Then backfill short_name + kit colours on any national
--    team that already exists without them (so none render as "???").
--
-- 4. WORLD CUP - Competition "World Cup" -> Season "2022" (WORLD_CUP_32),
--    no teams/stages. Pick 32 in the admin and click Generate.
--
-- Idempotent: safe to re-run; never touches the "World Cup" competition
-- once created.
-- =====================================================================
DO $$
DECLARE
  v_uni    UUID;
  v_comp   UUID;
  v_season UUID;
BEGIN
  SELECT id INTO v_uni FROM universes
    WHERE name LIKE '%Empires%' OR name LIKE '%AOE%' OR name LIKE '%Age%' LIMIT 1;
  IF v_uni IS NULL THEN RAISE EXCEPTION 'AOE2 universe not found'; END IF;

  -- ---- 1. CLEANUP ---------------------------------------------------
  DELETE FROM competitions WHERE name = 'World Cup 2022' AND universe_id = v_uni;
  DELETE FROM teams        WHERE name = 'Mayans'         AND universe_id = v_uni;
  DELETE FROM nations      WHERE name = 'Mayans'         AND universe_id = v_uni;

  -- ---- 2. ADD MISSING CIVS (with full fields) -----------------------
  INSERT INTO nations(name, code, primary_color, text_color, away_color, away_text_color, universe_id)
    SELECT 'Mapuche','MAP','#1B4F9C','#FFFFFF','#FFFFFF','#1B4F9C', v_uni
    WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Mapuche' AND universe_id=v_uni);
  INSERT INTO nations(name, code, primary_color, text_color, away_color, away_text_color, universe_id)
    SELECT 'Muisca','MUI','#C9A227','#222222','#222222','#C9A227', v_uni
    WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Muisca' AND universe_id=v_uni);
  INSERT INTO nations(name, code, primary_color, text_color, away_color, away_text_color, universe_id)
    SELECT 'Tupi','TUP','#1E7A3D','#FFD23F','#FFD23F','#1E7A3D', v_uni
    WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Tupi' AND universe_id=v_uni);

  -- backfill in case they were already created bare (name only)
  UPDATE nations SET code='MAP', primary_color='#1B4F9C', text_color='#FFFFFF', away_color='#FFFFFF', away_text_color='#1B4F9C'
    WHERE name='Mapuche' AND universe_id=v_uni AND code IS NULL;
  UPDATE nations SET code='MUI', primary_color='#C9A227', text_color='#222222', away_color='#222222', away_text_color='#C9A227'
    WHERE name='Muisca' AND universe_id=v_uni AND code IS NULL;
  UPDATE nations SET code='TUP', primary_color='#1E7A3D', text_color='#FFD23F', away_color='#FFD23F', away_text_color='#1E7A3D'
    WHERE name='Tupi' AND universe_id=v_uni AND code IS NULL;

  -- ---- 3. ONE STYLED NATIONAL TEAM PER NATION -----------------------
  INSERT INTO teams(name, short_name, type, universe_id, nation_id, home_bg, home_text, away_bg, away_text)
    SELECT n.name, n.code, 'NATIONAL', v_uni, n.id,
           n.primary_color, n.text_color, n.away_color, n.away_text_color
    FROM nations n
    WHERE n.universe_id = v_uni
      AND NOT EXISTS (
        SELECT 1 FROM teams t
        WHERE t.universe_id = v_uni AND t.nation_id = n.id AND t.type = 'NATIONAL');

  -- backfill short_name + kit colours on national teams missing them
  UPDATE teams t SET
    short_name = COALESCE(t.short_name, n.code),
    home_bg    = COALESCE(t.home_bg,   n.primary_color),
    home_text  = COALESCE(t.home_text, n.text_color),
    away_bg    = COALESCE(t.away_bg,   n.away_color),
    away_text  = COALESCE(t.away_text, n.away_text_color)
  FROM nations n
  WHERE t.nation_id = n.id AND t.universe_id = v_uni AND t.type = 'NATIONAL';

  -- ---- 4. CLEAN WORLD CUP SCAFFOLD ----------------------------------
  SELECT id INTO v_comp FROM competitions WHERE name='World Cup' AND universe_id=v_uni LIMIT 1;
  IF v_comp IS NULL THEN
    INSERT INTO competitions(name, type, team_level, universe_id)
      VALUES ('World Cup','GROUP_KNOCKOUT','NATIONAL', v_uni) RETURNING id INTO v_comp;
  END IF;

  SELECT id INTO v_season FROM seasons WHERE name='2022' AND competition_id=v_comp LIMIT 1;
  IF v_season IS NULL THEN
    INSERT INTO seasons(name, year, status, competition_id, format_preset_key)
      VALUES ('2022', 2022, 'PLANNED', v_comp, 'WORLD_CUP_32') RETURNING id INTO v_season;
  END IF;

  RAISE NOTICE 'AOE2 rebuild done: Mapuche/Muisca/Tupi added with full fields; every nation has a styled NATIONAL team; World Cup -> 2022 ready (pick 32 + Generate).';
END $$;
