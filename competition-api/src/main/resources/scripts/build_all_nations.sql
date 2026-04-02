DO $$
DECLARE v_uni UUID;
BEGIN
  SELECT id INTO v_uni FROM universes WHERE name LIKE '%Empires%' OR name LIKE '%AOE%' OR name LIKE '%Age%' LIMIT 1;
  IF v_uni IS NULL THEN RAISE EXCEPTION 'Empires universe not found'; END IF;

  -- ============ UPDATE EXISTING 13 ============
  UPDATE nations SET code='BRI', primary_color='#CC0000', text_color='#FFFFFF' WHERE name='Britons' AND universe_id=v_uni;
  UPDATE nations SET code='BYZ', primary_color='#7B2D8E', text_color='#FFD700' WHERE name='Byzantines' AND universe_id=v_uni;
  UPDATE nations SET code='CEL', primary_color='#228B22', text_color='#FFFFFF' WHERE name='Celts' AND universe_id=v_uni;
  UPDATE nations SET code='CHI', primary_color='#CC0000', text_color='#FFD700' WHERE name='Chinese' AND universe_id=v_uni;
  UPDATE nations SET code='FRA', primary_color='#0055A4', text_color='#FFD700' WHERE name='Franks' AND universe_id=v_uni;
  UPDATE nations SET code='GOT', primary_color='#4A3728', text_color='#C0C0C0' WHERE name='Goths' AND universe_id=v_uni;
  UPDATE nations SET code='JAP', primary_color='#FFFFFF', text_color='#CC0000' WHERE name='Japanese' AND universe_id=v_uni;
  UPDATE nations SET code='MON', primary_color='#003893', text_color='#FFD700' WHERE name='Mongols' AND universe_id=v_uni;
  UPDATE nations SET code='PER', primary_color='#DAA520', text_color='#1C1C1C' WHERE name='Persians' AND universe_id=v_uni;
  UPDATE nations SET code='SAR', primary_color='#C8B200', text_color='#1C1C1C' WHERE name='Saracens' AND universe_id=v_uni;
  UPDATE nations SET code='TEU', primary_color='#1C1C1C', text_color='#FFFFFF' WHERE name='Teutons' AND universe_id=v_uni;
  UPDATE nations SET code='TUR', primary_color='#E30A17', text_color='#FFFFFF' WHERE name='Turks' AND universe_id=v_uni;
  UPDATE nations SET code='VIK', primary_color='#4A6A8A', text_color='#FFFFFF' WHERE name='Vikings' AND universe_id=v_uni;

  -- ============ INSERT 34 NEW ============
  -- Armenians: ancient kingdom, red-blue-orange flag
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Armenians','ARM','#D2691E','#FFFFFF',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Armenians' AND universe_id=v_uni);
  -- Aztecs: jade green + gold, Mesoamerican
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Aztecs','AZT','#006847','#FFD700',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Aztecs' AND universe_id=v_uni);
  -- Bengalis: deep green, South Asian
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Bengalis','BEN','#006A4E','#FFFFFF',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Bengalis' AND universe_id=v_uni);
  -- Berbers: desert sand + indigo
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Berbers','BER','#C2956B','#1C1C1C',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Berbers' AND universe_id=v_uni);
  -- Bohemians: red + white, Central European
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Bohemians','BOH','#D7141A','#FFFFFF',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Bohemians' AND universe_id=v_uni);
  -- Bulgarians: white-green-red
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Bulgarians','BUL','#00966E','#FFFFFF',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Bulgarians' AND universe_id=v_uni);
  -- Burgundians: deep blue + gold fleur-de-lis
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Burgundians','BUR','#1E3A5F','#FFD700',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Burgundians' AND universe_id=v_uni);
  -- Burmese: golden pagoda
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Burmese','BRM','#FECB00','#1C1C1C',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Burmese' AND universe_id=v_uni);
  -- Cumans: steppe nomads, yellow-blue
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Cumans','CUM','#E8D44D','#2E4057',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Cumans' AND universe_id=v_uni);
  -- Dravidians: deep maroon, South Indian
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Dravidians','DRA','#800020','#FFD700',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Dravidians' AND universe_id=v_uni);
  -- Ethiopians: green-yellow-red
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Ethiopians','ETH','#009A44','#FCD116',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Ethiopians' AND universe_id=v_uni);
  -- Georgians: white + red cross
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Georgians','GEO','#FF0000','#FFFFFF',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Georgians' AND universe_id=v_uni);
  -- Gurjaras: saffron orange, Rajput warrior
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Gurjaras','GUR','#FF9933','#1C1C1C',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Gurjaras' AND universe_id=v_uni);
  -- Hindustanis: green + white, Mughal era
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Hindustanis','HIN','#138808','#FFFFFF',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Hindustanis' AND universe_id=v_uni);
  -- Huns: dark blood red, barbarian hordes
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Huns','HUN','#8B0000','#FFD700',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Huns' AND universe_id=v_uni);
  -- Inca: gold + earth brown, Andean
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Inca','INC','#B8860B','#FFFFFF',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Inca' AND universe_id=v_uni);
  -- Italians: Renaissance blue + white
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Italians','ITA','#003399','#FFFFFF',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Italians' AND universe_id=v_uni);
  -- Jurchens: dark teal, Manchurian
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Jurchens','JUR','#006666','#FFD700',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Jurchens' AND universe_id=v_uni);
  -- Khitans: deep purple, nomadic dynasty
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Khitans','KHI','#4B0082','#C0C0C0',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Khitans' AND universe_id=v_uni);
  -- Khmer: temple red-brown, Angkor
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Khmer','KHM','#8B4513','#FFD700',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Khmer' AND universe_id=v_uni);
  -- Koreans: deep blue, Joseon dynasty
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Koreans','KOR','#003478','#FFFFFF',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Koreans' AND universe_id=v_uni);
  -- Lithuanians: yellow-green-red
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Lithuanians','LIT','#FDB913','#006A44',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Lithuanians' AND universe_id=v_uni);
  -- Magyars: red-white-green, Hungarian
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Magyars','MAG','#CE2939','#FFFFFF',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Magyars' AND universe_id=v_uni);
  -- Malay: maritime purple, Southeast Asian
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Malay','MAL','#660099','#FFD700',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Malay' AND universe_id=v_uni);
  -- Malians: gold empire, West African
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Malians','MLI','#FFD700','#009639',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Malians' AND universe_id=v_uni);
  -- Maya: jade + obsidian, Mesoamerican
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Maya','MAY','#2E8B57','#1C1C1C',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Maya' AND universe_id=v_uni);
  -- Poles: white eagle on red
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Poles','POL','#DC143C','#FFFFFF',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Poles' AND universe_id=v_uni);
  -- Portuguese: blue + white, maritime
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Portuguese','POR','#003399','#FFD700',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Portuguese' AND universe_id=v_uni);
  -- Romans: imperial purple + gold
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Romans','ROM','#6B3FA0','#FFD700',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Romans' AND universe_id=v_uni);
  -- Sicilians: sun yellow + red, Norman-influenced
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Sicilians','SIC','#F4A300','#CC0000',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Sicilians' AND universe_id=v_uni);
  -- Slavs: blue + red, Eastern European
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Slavs','SLV','#0039A6','#FFFFFF',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Slavs' AND universe_id=v_uni);
  -- Spanish: red + gold, Reconquista
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Spanish','SPA','#AA151B','#F1BF00',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Spanish' AND universe_id=v_uni);
  -- Tatars: steppe blue + gold
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Tatars','TAT','#2F4F4F','#FFD700',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Tatars' AND universe_id=v_uni);
  -- Vietnamese: red + gold star
  INSERT INTO nations (name,code,primary_color,text_color,universe_id) SELECT 'Vietnamese','VIE','#DA251D','#FFD700',v_uni WHERE NOT EXISTS (SELECT 1 FROM nations WHERE name='Vietnamese' AND universe_id=v_uni);

  RAISE NOTICE 'All 47 AOE2 nations created/updated';
END $$;
