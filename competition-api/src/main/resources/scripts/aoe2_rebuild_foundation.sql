-- =====================================================================
-- AOE2 foundation rebuild + clean World Cup scaffold.
--
-- 1. CLEANUP
--    - Delete the mis-built "World Cup 2022" competition. Every child FK
--      is ON DELETE CASCADE (seasons -> season_teams / rounds -> matches
--      -> match_slot / stages -> stage_groups -> stage_group_teams /
--      standings), so this single delete removes the whole broken
--      tournament in one shot.
--    - Then delete the duplicate "Mayans" national team (now unreferenced)
--      and the duplicate "Mayans" nation. The real civ is "Maya".
--
-- 2. FOUNDATION (pool, lives at the universe level)
--    - Get-or-create all 50 AOE2:DE civs = the full roster MINUS Shu / Wei
--      / Wu, using the exact DE names (Maya, Inca, Malay, ...). Existing
--      nations are reused, only the missing few are added.
--    - Ensure every nation has exactly ONE NATIONAL team named after the
--      civ. Any existing national team for that nation is reused.
--
-- 3. REBUILD WORLD CUP (scaffold only)
--    - Competition "World Cup" -> Season "2022" (WORLD_CUP_32 preset on the
--      season). NO teams, NO stages. Pick the 32 participants in the admin
--      and click Generate to build groups + R16/QF/SF/Final + 3rd place.
--
-- Idempotent: safe to re-run. It never touches the "World Cup" competition
-- once created, so re-running will not wipe a configured tournament.
-- =====================================================================
DO $$
DECLARE
  v_uni      UUID;
  v_comp     UUID;
  v_season   UUID;
  v_nation   UUID;
  v_civ      TEXT;
  v_civs     TEXT[] := ARRAY[
    'Armenians','Aztecs','Bengalis','Berbers','Bohemians','Britons','Bulgarians',
    'Burgundians','Burmese','Byzantines','Celts','Chinese','Cumans','Dravidians',
    'Ethiopians','Franks','Georgians','Goths','Gurjaras','Hindustanis','Huns',
    'Inca','Italians','Japanese','Jurchens','Khitans','Khmer','Koreans',
    'Lithuanians','Magyars','Malay','Malians','Mapuche','Maya','Mongols','Muisca',
    'Persians','Poles','Portuguese','Romans','Saracens','Sicilians','Slavs',
    'Spanish','Tatars','Teutons','Tupi','Turks','Vietnamese','Vikings'
  ];
BEGIN
  -- locate the AOE2 universe (same lookup the original seed used)
  SELECT id INTO v_uni FROM universes
    WHERE name LIKE '%Empires%' OR name LIKE '%AOE%' OR name LIKE '%Age%' LIMIT 1;
  IF v_uni IS NULL THEN RAISE EXCEPTION 'AOE2 universe not found'; END IF;

  -- ---- 1. CLEANUP ---------------------------------------------------
  DELETE FROM competitions WHERE name = 'World Cup 2022' AND universe_id = v_uni;

  DELETE FROM teams   WHERE name = 'Mayans' AND universe_id = v_uni;
  DELETE FROM nations WHERE name = 'Mayans' AND universe_id = v_uni;

  -- ---- 2. FOUNDATION ------------------------------------------------
  FOREACH v_civ IN ARRAY v_civs LOOP
    -- nation (get-or-create)
    SELECT id INTO v_nation FROM nations
      WHERE name = v_civ AND universe_id = v_uni LIMIT 1;
    IF v_nation IS NULL THEN
      INSERT INTO nations(name, universe_id) VALUES (v_civ, v_uni)
        RETURNING id INTO v_nation;
    END IF;

    -- one NATIONAL team per nation (reuse any existing one)
    IF NOT EXISTS (
      SELECT 1 FROM teams
        WHERE universe_id = v_uni AND nation_id = v_nation AND type = 'NATIONAL'
    ) THEN
      INSERT INTO teams(name, type, universe_id, nation_id)
        VALUES (v_civ, 'NATIONAL', v_uni, v_nation);
    END IF;
  END LOOP;

  -- ---- 3. REBUILD WORLD CUP (scaffold only) -------------------------
  SELECT id INTO v_comp FROM competitions
    WHERE name = 'World Cup' AND universe_id = v_uni LIMIT 1;
  IF v_comp IS NULL THEN
    INSERT INTO competitions(name, type, team_level, universe_id)
      VALUES ('World Cup', 'GROUP_KNOCKOUT', 'NATIONAL', v_uni)
      RETURNING id INTO v_comp;
  END IF;

  SELECT id INTO v_season FROM seasons
    WHERE name = '2022' AND competition_id = v_comp LIMIT 1;
  IF v_season IS NULL THEN
    INSERT INTO seasons(name, year, status, competition_id, format_preset_key)
      VALUES ('2022', 2022, 'PLANNED', v_comp, 'WORLD_CUP_32')
      RETURNING id INTO v_season;
  END IF;

  RAISE NOTICE 'AOE2 rebuild done: 50 civ nations + national teams ensured; World Cup -> 2022 scaffold ready (pick 32 teams + Generate).';
END $$;
