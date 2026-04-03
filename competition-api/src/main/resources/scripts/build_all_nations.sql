DO $$
DECLARE v_uni UUID;
BEGIN
  SELECT id INTO v_uni FROM universes WHERE name LIKE '%Empires%' OR name LIKE '%AOE%' OR name LIKE '%Age%' LIMIT 1;
  IF v_uni IS NULL THEN RAISE EXCEPTION 'Empires universe not found'; END IF;

  -- 13 existing: update all colors
  UPDATE nations SET code='BRI', primary_color='#CC0000', text_color='#FFFFFF', away_color='#FFFFFF', away_text_color='#CC0000' WHERE name='Britons' AND universe_id=v_uni;
  UPDATE nations SET code='BYZ', primary_color='#7B2D8E', text_color='#FFD700', away_color='#FFD700', away_text_color='#7B2D8E' WHERE name='Byzantines' AND universe_id=v_uni;
  UPDATE nations SET code='CEL', primary_color='#228B22', text_color='#FFFFFF', away_color='#FFFFFF', away_text_color='#228B22' WHERE name='Celts' AND universe_id=v_uni;
  UPDATE nations SET code='CHI', primary_color='#CC0000', text_color='#FFD700', away_color='#FFD700', away_text_color='#CC0000' WHERE name='Chinese' AND universe_id=v_uni;
  UPDATE nations SET code='FRA', primary_color='#0055A4', text_color='#FFD700', away_color='#FFFFFF', away_text_color='#0055A4' WHERE name='Franks' AND universe_id=v_uni;
  UPDATE nations SET code='GOT', primary_color='#4A3728', text_color='#C0C0C0', away_color='#C0C0C0', away_text_color='#4A3728' WHERE name='Goths' AND universe_id=v_uni;
  UPDATE nations SET code='JAP', primary_color='#FFFFFF', text_color='#CC0000', away_color='#CC0000', away_text_color='#FFFFFF' WHERE name='Japanese' AND universe_id=v_uni;
  UPDATE nations SET code='MON', primary_color='#003893', text_color='#FFD700', away_color='#FFD700', away_text_color='#003893' WHERE name='Mongols' AND universe_id=v_uni;
  UPDATE nations SET code='PER', primary_color='#DAA520', text_color='#1C1C1C', away_color='#1C1C1C', away_text_color='#DAA520' WHERE name='Persians' AND universe_id=v_uni;
  UPDATE nations SET code='SAR', primary_color='#C8B200', text_color='#1C1C1C', away_color='#1C1C1C', away_text_color='#C8B200' WHERE name='Saracens' AND universe_id=v_uni;
  UPDATE nations SET code='TEU', primary_color='#1C1C1C', text_color='#FFFFFF', away_color='#FFFFFF', away_text_color='#1C1C1C' WHERE name='Teutons' AND universe_id=v_uni;
  UPDATE nations SET code='TUR', primary_color='#E30A17', text_color='#FFFFFF', away_color='#FFFFFF', away_text_color='#E30A17' WHERE name='Turks' AND universe_id=v_uni;
  UPDATE nations SET code='VIK', primary_color='#4A6A8A', text_color='#FFFFFF', away_color='#FFFFFF', away_text_color='#4A6A8A' WHERE name='Vikings' AND universe_id=v_uni;

  -- 34 new nations
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Armenians','ARM','#D2691E','#FFFFFF','#FFFFFF','#D2691E',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Armenians' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Aztecs','AZT','#006847','#FFD700','#FFD700','#006847',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Aztecs' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Bengalis','BEN','#006A4E','#FFFFFF','#FFFFFF','#006A4E',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Bengalis' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Berbers','BER','#C2956B','#1C1C1C','#1C1C1C','#C2956B',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Berbers' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Bohemians','BOH','#D7141A','#FFFFFF','#FFFFFF','#D7141A',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Bohemians' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Bulgarians','BUL','#00966E','#FFFFFF','#FFFFFF','#00966E',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Bulgarians' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Burgundians','BUR','#1E3A5F','#FFD700','#FFD700','#1E3A5F',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Burgundians' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Burmese','BRM','#FECB00','#1C1C1C','#1C1C1C','#FECB00',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Burmese' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Cumans','CUM','#E8D44D','#2E4057','#2E4057','#E8D44D',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Cumans' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Dravidians','DRA','#800020','#FFD700','#FFD700','#800020',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Dravidians' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Ethiopians','ETH','#009A44','#FCD116','#FCD116','#009A44',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Ethiopians' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Georgians','GEO','#FF0000','#FFFFFF','#FFFFFF','#FF0000',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Georgians' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Gurjaras','GUR','#FF9933','#1C1C1C','#1C1C1C','#FF9933',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Gurjaras' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Hindustanis','HIN','#138808','#FFFFFF','#FFFFFF','#138808',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Hindustanis' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Huns','HUN','#8B0000','#FFD700','#FFD700','#8B0000',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Huns' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Inca','INC','#B8860B','#FFFFFF','#FFFFFF','#B8860B',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Inca' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Italians','ITA','#003399','#FFFFFF','#FFFFFF','#003399',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Italians' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Jurchens','JUR','#006666','#FFD700','#FFD700','#006666',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Jurchens' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Khitans','KHI','#4B0082','#C0C0C0','#C0C0C0','#4B0082',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Khitans' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Khmer','KHM','#8B4513','#FFD700','#FFD700','#8B4513',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Khmer' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Koreans','KOR','#003478','#FFFFFF','#FFFFFF','#003478',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Koreans' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Lithuanians','LIT','#FDB913','#006A44','#006A44','#FDB913',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Lithuanians' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Magyars','MAG','#CE2939','#FFFFFF','#FFFFFF','#CE2939',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Magyars' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Malay','MAL','#660099','#FFD700','#FFD700','#660099',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Malay' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Malians','MLI','#FFD700','#009639','#009639','#FFD700',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Malians' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Maya','MAY','#2E8B57','#1C1C1C','#1C1C1C','#2E8B57',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Maya' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Poles','POL','#DC143C','#FFFFFF','#FFFFFF','#DC143C',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Poles' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Portuguese','POR','#003399','#FFD700','#FFFFFF','#003399',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Portuguese' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Romans','ROM','#6B3FA0','#FFD700','#FFD700','#6B3FA0',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Romans' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Sicilians','SIC','#F4A300','#CC0000','#CC0000','#F4A300',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Sicilians' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Slavs','SLV','#0039A6','#FFFFFF','#FFFFFF','#0039A6',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Slavs' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Spanish','SPA','#AA151B','#F1BF00','#F1BF00','#AA151B',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Spanish' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Tatars','TAT','#2F4F4F','#FFD700','#FFD700','#2F4F4F',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Tatars' AND universe_id=v_uni);
  INSERT INTO nations (name,code,primary_color,text_color,away_color,away_text_color,universe_id) SELECT 'Vietnamese','VIE','#DA251D','#FFD700','#FFD700','#DA251D',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Vietnamese' AND universe_id=v_uni);

  RAISE NOTICE 'All 47 AOE2 nations created/updated with home + away colors';
END $$;
