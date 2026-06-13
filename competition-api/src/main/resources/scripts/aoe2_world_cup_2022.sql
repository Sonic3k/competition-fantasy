-- =====================================================================
-- AOE2 World Cup 2022 — setup only (32 strong AOE2 DE civilisations).
-- Creates 32 nations (get-or-create; reuses the existing Wonder Empires
-- civs), 32 NATIONAL teams, a "World Cup 2022" competition + season tagged
-- with the WORLD_CUP_32 preset, and links the 32 teams.
-- It intentionally creates NO stages/groups/fixtures — open the season in
-- the admin and click "Generate" (World Cup 32) to build the 8 groups +
-- Round of 16/QF/SF/Final + third-place play-off, then enter scores.
-- Safe to re-run: it no-ops if the competition already exists.
-- =====================================================================
DO $$
DECLARE
  v_uni UUID; v_comp UUID; v_season UUID;
  n_fra UUID; n_mon UUID; n_may UUID; n_azt UUID; n_hun UUID; n_bri UUID; n_chi UUID; n_khm UUID;
  n_lit UUID; n_mag UUID; n_ber UUID; n_eth UUID; n_bul UUID; n_vik UUID; n_tat UUID; n_cum UUID;
  n_hin UUID; n_bur UUID; n_mli UUID; n_per UUID; n_byz UUID; n_spa UUID; n_tur UUID; n_jap UUID;
  n_sar UUID; n_cel UUID; n_teu UUID; n_pol UUID; n_boh UUID; n_gur UUID; n_rom UUID; n_vie UUID;
  t_fra UUID; t_mon UUID; t_may UUID; t_azt UUID; t_hun UUID; t_bri UUID; t_chi UUID; t_khm UUID;
  t_lit UUID; t_mag UUID; t_ber UUID; t_eth UUID; t_bul UUID; t_vik UUID; t_tat UUID; t_cum UUID;
  t_hin UUID; t_bur UUID; t_mli UUID; t_per UUID; t_byz UUID; t_spa UUID; t_tur UUID; t_jap UUID;
  t_sar UUID; t_cel UUID; t_teu UUID; t_pol UUID; t_boh UUID; t_gur UUID; t_rom UUID; t_vie UUID;
BEGIN
  SELECT id INTO v_uni FROM universes WHERE name LIKE '%Empires%' OR name LIKE '%AOE%' OR name LIKE '%Age%' LIMIT 1;
  IF v_uni IS NULL THEN RAISE EXCEPTION 'AOE2 universe not found'; END IF;
  IF EXISTS (SELECT 1 FROM competitions WHERE name='World Cup 2022' AND universe_id=v_uni) THEN
    RAISE NOTICE 'World Cup 2022 already exists'; RETURN;
  END IF;

  -- Nations (get-or-create so the 13 existing Wonder Empires civs are reused)
  SELECT id INTO n_fra FROM nations WHERE name='Franks'       AND universe_id=v_uni LIMIT 1; IF n_fra IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Franks','FRA','#0033cc','#ffffff',v_uni) RETURNING id INTO n_fra; END IF;
  SELECT id INTO n_mon FROM nations WHERE name='Mongols'      AND universe_id=v_uni LIMIT 1; IF n_mon IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Mongols','MON','#003399','#ffffff',v_uni) RETURNING id INTO n_mon; END IF;
  SELECT id INTO n_may FROM nations WHERE name='Mayans'       AND universe_id=v_uni LIMIT 1; IF n_may IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Mayans','MAY','#2e8b57','#ffffff',v_uni) RETURNING id INTO n_may; END IF;
  SELECT id INTO n_azt FROM nations WHERE name='Aztecs'       AND universe_id=v_uni LIMIT 1; IF n_azt IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Aztecs','AZT','#b8860b','#ffffff',v_uni) RETURNING id INTO n_azt; END IF;
  SELECT id INTO n_hun FROM nations WHERE name='Huns'         AND universe_id=v_uni LIMIT 1; IF n_hun IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Huns','HUN','#8b0000','#ffffff',v_uni) RETURNING id INTO n_hun; END IF;
  SELECT id INTO n_bri FROM nations WHERE name='Britons'      AND universe_id=v_uni LIMIT 1; IF n_bri IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Britons','BRI','#cc0000','#ffffff',v_uni) RETURNING id INTO n_bri; END IF;
  SELECT id INTO n_chi FROM nations WHERE name='Chinese'      AND universe_id=v_uni LIMIT 1; IF n_chi IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Chinese','CHI','#cc0000','#ffcc00',v_uni) RETURNING id INTO n_chi; END IF;
  SELECT id INTO n_khm FROM nations WHERE name='Khmer'        AND universe_id=v_uni LIMIT 1; IF n_khm IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Khmer','KHM','#006666','#ffffff',v_uni) RETURNING id INTO n_khm; END IF;
  SELECT id INTO n_lit FROM nations WHERE name='Lithuanians'  AND universe_id=v_uni LIMIT 1; IF n_lit IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Lithuanians','LIT','#006633','#ffcc00',v_uni) RETURNING id INTO n_lit; END IF;
  SELECT id INTO n_mag FROM nations WHERE name='Magyars'      AND universe_id=v_uni LIMIT 1; IF n_mag IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Magyars','MAG','#800000','#ffffff',v_uni) RETURNING id INTO n_mag; END IF;
  SELECT id INTO n_ber FROM nations WHERE name='Berbers'      AND universe_id=v_uni LIMIT 1; IF n_ber IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Berbers','BER','#cc7700','#ffffff',v_uni) RETURNING id INTO n_ber; END IF;
  SELECT id INTO n_eth FROM nations WHERE name='Ethiopians'   AND universe_id=v_uni LIMIT 1; IF n_eth IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Ethiopians','ETH','#006600','#ffcc00',v_uni) RETURNING id INTO n_eth; END IF;
  SELECT id INTO n_bul FROM nations WHERE name='Bulgarians'   AND universe_id=v_uni LIMIT 1; IF n_bul IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Bulgarians','BUL','#2e7d32','#ffffff',v_uni) RETURNING id INTO n_bul; END IF;
  SELECT id INTO n_vik FROM nations WHERE name='Vikings'      AND universe_id=v_uni LIMIT 1; IF n_vik IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Vikings','VIK','#666666','#ffffff',v_uni) RETURNING id INTO n_vik; END IF;
  SELECT id INTO n_tat FROM nations WHERE name='Tatars'       AND universe_id=v_uni LIMIT 1; IF n_tat IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Tatars','TAT','#c08a00','#000000',v_uni) RETURNING id INTO n_tat; END IF;
  SELECT id INTO n_cum FROM nations WHERE name='Cumans'       AND universe_id=v_uni LIMIT 1; IF n_cum IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Cumans','CUM','#8a6d00','#ffffff',v_uni) RETURNING id INTO n_cum; END IF;
  SELECT id INTO n_hin FROM nations WHERE name='Hindustanis'  AND universe_id=v_uni LIMIT 1; IF n_hin IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Hindustanis','HIN','#ff9933','#000080',v_uni) RETURNING id INTO n_hin; END IF;
  SELECT id INTO n_bur FROM nations WHERE name='Burmese'      AND universe_id=v_uni LIMIT 1; IF n_bur IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Burmese','BUR','#b8860b','#4b2e0a',v_uni) RETURNING id INTO n_bur; END IF;
  SELECT id INTO n_mli FROM nations WHERE name='Malians'      AND universe_id=v_uni LIMIT 1; IF n_mli IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Malians','MLI','#1a7a3a','#ffd700',v_uni) RETURNING id INTO n_mli; END IF;
  SELECT id INTO n_per FROM nations WHERE name='Persians'     AND universe_id=v_uni LIMIT 1; IF n_per IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Persians','PER','#cc6600','#ffffff',v_uni) RETURNING id INTO n_per; END IF;
  SELECT id INTO n_byz FROM nations WHERE name='Byzantines'   AND universe_id=v_uni LIMIT 1; IF n_byz IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Byzantines','BYZ','#6600cc','#ffffff',v_uni) RETURNING id INTO n_byz; END IF;
  SELECT id INTO n_spa FROM nations WHERE name='Spanish'      AND universe_id=v_uni LIMIT 1; IF n_spa IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Spanish','SPA','#c60b1e','#ffc400',v_uni) RETURNING id INTO n_spa; END IF;
  SELECT id INTO n_tur FROM nations WHERE name='Turks'        AND universe_id=v_uni LIMIT 1; IF n_tur IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Turks','TUR','#cc0000','#ffffff',v_uni) RETURNING id INTO n_tur; END IF;
  SELECT id INTO n_jap FROM nations WHERE name='Japanese'     AND universe_id=v_uni LIMIT 1; IF n_jap IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Japanese','JAP','#ffffff','#cc0000',v_uni) RETURNING id INTO n_jap; END IF;
  SELECT id INTO n_sar FROM nations WHERE name='Saracens'     AND universe_id=v_uni LIMIT 1; IF n_sar IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Saracens','SAR','#cccc00','#000000',v_uni) RETURNING id INTO n_sar; END IF;
  SELECT id INTO n_cel FROM nations WHERE name='Celts'        AND universe_id=v_uni LIMIT 1; IF n_cel IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Celts','CEL','#006633','#ffffff',v_uni) RETURNING id INTO n_cel; END IF;
  SELECT id INTO n_teu FROM nations WHERE name='Teutons'      AND universe_id=v_uni LIMIT 1; IF n_teu IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Teutons','TEU','#333333','#ffffff',v_uni) RETURNING id INTO n_teu; END IF;
  SELECT id INTO n_pol FROM nations WHERE name='Poles'        AND universe_id=v_uni LIMIT 1; IF n_pol IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Poles','POL','#dc143c','#ffffff',v_uni) RETURNING id INTO n_pol; END IF;
  SELECT id INTO n_boh FROM nations WHERE name='Bohemians'    AND universe_id=v_uni LIMIT 1; IF n_boh IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Bohemians','BOH','#c8102e','#ffffff',v_uni) RETURNING id INTO n_boh; END IF;
  SELECT id INTO n_gur FROM nations WHERE name='Gurjaras'     AND universe_id=v_uni LIMIT 1; IF n_gur IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Gurjaras','GUR','#e25822','#ffffff',v_uni) RETURNING id INTO n_gur; END IF;
  SELECT id INTO n_rom FROM nations WHERE name='Romans'       AND universe_id=v_uni LIMIT 1; IF n_rom IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Romans','ROM','#7a0000','#ffd700',v_uni) RETURNING id INTO n_rom; END IF;
  SELECT id INTO n_vie FROM nations WHERE name='Vietnamese'   AND universe_id=v_uni LIMIT 1; IF n_vie IS NULL THEN INSERT INTO nations(name,code,primary_color,text_color,universe_id) VALUES('Vietnamese','VIE','#da251d','#ffff00',v_uni) RETURNING id INTO n_vie; END IF;

  -- 32 national teams (one per civilisation)
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Franks','FRA','NATIONAL',v_uni,n_fra) RETURNING id INTO t_fra;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Mongols','MON','NATIONAL',v_uni,n_mon) RETURNING id INTO t_mon;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Mayans','MAY','NATIONAL',v_uni,n_may) RETURNING id INTO t_may;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Aztecs','AZT','NATIONAL',v_uni,n_azt) RETURNING id INTO t_azt;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Huns','HUN','NATIONAL',v_uni,n_hun) RETURNING id INTO t_hun;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Britons','BRI','NATIONAL',v_uni,n_bri) RETURNING id INTO t_bri;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Chinese','CHI','NATIONAL',v_uni,n_chi) RETURNING id INTO t_chi;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Khmer','KHM','NATIONAL',v_uni,n_khm) RETURNING id INTO t_khm;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Lithuanians','LIT','NATIONAL',v_uni,n_lit) RETURNING id INTO t_lit;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Magyars','MAG','NATIONAL',v_uni,n_mag) RETURNING id INTO t_mag;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Berbers','BER','NATIONAL',v_uni,n_ber) RETURNING id INTO t_ber;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Ethiopians','ETH','NATIONAL',v_uni,n_eth) RETURNING id INTO t_eth;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Bulgarians','BUL','NATIONAL',v_uni,n_bul) RETURNING id INTO t_bul;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Vikings','VIK','NATIONAL',v_uni,n_vik) RETURNING id INTO t_vik;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Tatars','TAT','NATIONAL',v_uni,n_tat) RETURNING id INTO t_tat;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Cumans','CUM','NATIONAL',v_uni,n_cum) RETURNING id INTO t_cum;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Hindustanis','HIN','NATIONAL',v_uni,n_hin) RETURNING id INTO t_hin;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Burmese','BUR','NATIONAL',v_uni,n_bur) RETURNING id INTO t_bur;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Malians','MLI','NATIONAL',v_uni,n_mli) RETURNING id INTO t_mli;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Persians','PER','NATIONAL',v_uni,n_per) RETURNING id INTO t_per;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Byzantines','BYZ','NATIONAL',v_uni,n_byz) RETURNING id INTO t_byz;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Spanish','SPA','NATIONAL',v_uni,n_spa) RETURNING id INTO t_spa;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Turks','TUR','NATIONAL',v_uni,n_tur) RETURNING id INTO t_tur;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Japanese','JAP','NATIONAL',v_uni,n_jap) RETURNING id INTO t_jap;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Saracens','SAR','NATIONAL',v_uni,n_sar) RETURNING id INTO t_sar;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Celts','CEL','NATIONAL',v_uni,n_cel) RETURNING id INTO t_cel;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Teutons','TEU','NATIONAL',v_uni,n_teu) RETURNING id INTO t_teu;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Poles','POL','NATIONAL',v_uni,n_pol) RETURNING id INTO t_pol;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Bohemians','BOH','NATIONAL',v_uni,n_boh) RETURNING id INTO t_boh;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Gurjaras','GUR','NATIONAL',v_uni,n_gur) RETURNING id INTO t_gur;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Romans','ROM','NATIONAL',v_uni,n_rom) RETURNING id INTO t_rom;
  INSERT INTO teams(name,short_name,type,universe_id,nation_id) VALUES('Vietnamese','VIE','NATIONAL',v_uni,n_vie) RETURNING id INTO t_vie;

  -- Competition + season (tagged WORLD_CUP_32). No stages yet — click Generate.
  INSERT INTO competitions(name,type,team_level,universe_id)
    VALUES('World Cup 2022','GROUP_KNOCKOUT','NATIONAL',v_uni) RETURNING id INTO v_comp;
  INSERT INTO seasons(name,year,status,competition_id,format_preset_key)
    VALUES('World Cup 2022',2022,'IN_PROGRESS',v_comp,'WORLD_CUP_32') RETURNING id INTO v_season;

  INSERT INTO season_teams(season_id,team_id) VALUES
    (v_season,t_fra),(v_season,t_mon),(v_season,t_may),(v_season,t_azt),
    (v_season,t_hun),(v_season,t_bri),(v_season,t_chi),(v_season,t_khm),
    (v_season,t_lit),(v_season,t_mag),(v_season,t_ber),(v_season,t_eth),
    (v_season,t_bul),(v_season,t_vik),(v_season,t_tat),(v_season,t_cum),
    (v_season,t_hin),(v_season,t_bur),(v_season,t_mli),(v_season,t_per),
    (v_season,t_byz),(v_season,t_spa),(v_season,t_tur),(v_season,t_jap),
    (v_season,t_sar),(v_season,t_cel),(v_season,t_teu),(v_season,t_pol),
    (v_season,t_boh),(v_season,t_gur),(v_season,t_rom),(v_season,t_vie);

  RAISE NOTICE 'World Cup 2022 created: 32 teams. Open the season and click Generate (World Cup 32) to build the groups + bracket.';
END $$;
