DO $$
DECLARE
  v_uni UUID; v_comp UUID; v_season UUID;
  s_grp UUID; s_elim UUID; s_qf UUID; s_sf UUID; s_fin UUID;
  -- stage groups
  ga UUID; gb UUID; gc UUID; gd UUID; ge UUID; gf UUID; gg UUID; gh UUID;
  ea UUID; eb UUID; ec UUID; ed UUID;
  -- nations
  n_bri UUID; n_byz UUID; n_teu UUID; n_chi UUID; n_cel UUID; n_per UUID;
  n_jap UUID; n_sar UUID; n_got UUID; n_mon UUID; n_fra UUID; n_vik UUID; n_tur UUID;
  -- teams (32)
  t_brc UUID; t_bel UUID; t_boh UUID; t_tea UUID;
  t_rid UUID; t_war UUID; t_bla UUID; t_kit UUID;
  t_ben UUID; t_gen UUID; t_nai UUID; t_dra UUID;
  t_ler UUID; t_inf UUID; t_chu UUID; t_dar UUID;
  t_kni UUID; t_man UUID; t_arc UUID; t_mam UUID;
  t_hus UUID; t_kaj UUID; t_grc UUID; t_gru UUID;
  t_yos UUID; t_rol UUID; t_jan UUID; t_shi UUID;
  t_pes UUID; t_ceu UUID; t_rov UUID; t_rud UUID;
  r UUID;
BEGIN
  SELECT id INTO v_uni FROM universes WHERE name LIKE '%Empires%' OR name LIKE '%AOE%' OR name LIKE '%Age%' LIMIT 1;
  IF v_uni IS NULL THEN RAISE EXCEPTION 'Empires universe not found'; END IF;
  IF EXISTS (SELECT 1 FROM competitions WHERE name='Wonder Empires Football' AND universe_id=v_uni) THEN RAISE NOTICE 'Already exists'; RETURN; END IF;

  -- Nations
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Britons',NULL,'Britons',v_uni) RETURNING id INTO n_bri;
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Byzantines',NULL,'Byzantines',v_uni) RETURNING id INTO n_byz;
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Teutons',NULL,'Teutons',v_uni) RETURNING id INTO n_teu;
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Chinese',NULL,'Chinese',v_uni) RETURNING id INTO n_chi;
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Celts',NULL,'Celts',v_uni) RETURNING id INTO n_cel;
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Persians',NULL,'Persians',v_uni) RETURNING id INTO n_per;
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Japanese',NULL,'Japanese',v_uni) RETURNING id INTO n_jap;
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Saracens',NULL,'Saracens',v_uni) RETURNING id INTO n_sar;
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Goths',NULL,'Goths',v_uni) RETURNING id INTO n_got;
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Mongols',NULL,'Mongols',v_uni) RETURNING id INTO n_mon;
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Franks',NULL,'Franks',v_uni) RETURNING id INTO n_fra;
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Vikings',NULL,'Vikings',v_uni) RETURNING id INTO n_vik;
  INSERT INTO nations (name,flag_url,description,universe_id) VALUES
    ('Turks',NULL,'Turks',v_uni) RETURNING id INTO n_tur;

  -- 32 Teams
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('British City','BRC','CLUB',v_uni,n_bri) RETURNING id INTO t_brc;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Belisarius United','BEL','CLUB',v_uni,n_byz) RETURNING id INTO t_bel;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Bohemia','BOH','CLUB',v_uni,n_teu) RETURNING id INTO t_boh;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Teamton','TEA','CLUB',v_uni,n_chi) RETURNING id INTO t_tea;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Rider Woadwood','RID','CLUB',v_uni,n_cel) RETURNING id INTO t_rid;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('War Elephant','WAR','CLUB',v_uni,n_per) RETURNING id INTO t_war;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Blastoise','BLS','CLUB',v_uni,n_teu) RETURNING id INTO t_bla;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Kitabatake','KIT','CLUB',v_uni,n_jap) RETURNING id INTO t_kit;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Benlizihad United','BZU','CLUB',v_uni,n_sar) RETURNING id INTO t_ben;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('General Siede','GNS','CLUB',v_uni,n_got) RETURNING id INTO t_gen;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Naiman','NAI','CLUB',v_uni,n_mon) RETURNING id INTO t_nai;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Dragon Burgundy','DRB','CLUB',v_uni,n_fra) RETURNING id INTO t_dra;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Lerberk','LER','CLUB',v_uni,n_tur) RETURNING id INTO t_ler;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Infantry Berserk','INF','CLUB',v_uni,n_vik) RETURNING id INTO t_inf;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Chu-Koo-Nu','CKN','CLUB',v_uni,n_chi) RETURNING id INTO t_chu;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Dark III','DRK','CLUB',v_uni,n_bri) RETURNING id INTO t_dar;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Knight Templar','KNT','CLUB',v_uni,n_teu) RETURNING id INTO t_kni;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Mangudai','MNG','CLUB',v_uni,n_mon) RETURNING id INTO t_man;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Arc Main','ARC','CLUB',v_uni,n_fra) RETURNING id INTO t_arc;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Mameluke','MAM','CLUB',v_uni,n_sar) RETURNING id INTO t_mam;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Huskarl','HUS','CLUB',v_uni,n_got) RETURNING id INTO t_hus;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Kajinuo','KAJ','CLUB',v_uni,n_jap) RETURNING id INTO t_kaj;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Green Cycene','GCY','CLUB',v_uni,n_cel) RETURNING id INTO t_grc;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Greek United','GKU','CLUB',v_uni,n_byz) RETURNING id INTO t_gru;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Yoshiga Minamoto','YOS','CLUB',v_uni,n_jap) RETURNING id INTO t_yos;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Roland','ROL','CLUB',v_uni,n_fra) RETURNING id INTO t_rol;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Janissary','JAN','CLUB',v_uni,n_tur) RETURNING id INTO t_jan;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Ship Parcicion','SHP','CLUB',v_uni,n_vik) RETURNING id INTO t_shi;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Persata','PES','CLUB',v_uni,n_mon) RETURNING id INTO t_pes;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Celt United','CLU','CLUB',v_uni,n_cel) RETURNING id INTO t_ceu;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Rover Shah','RVS','CLUB',v_uni,n_per) RETURNING id INTO t_rov;
  INSERT INTO teams (name,short_name,type,universe_id,nation_id) VALUES ('Rudolph Swabian','RDS','CLUB',v_uni,n_teu) RETURNING id INTO t_rud;

  -- Competition & Season
  INSERT INTO competitions (name,type,team_level,universe_id) VALUES ('Wonder Empires Football','GROUP_KNOCKOUT','CLUB',v_uni) RETURNING id INTO v_comp;
  INSERT INTO seasons (name,year,status,competition_id) VALUES ('Season 1',2004,'COMPLETED',v_comp) RETURNING id INTO v_season;

  -- Add all teams to season
  INSERT INTO season_teams (season_id,team_id) VALUES
    (v_season,t_brc),(v_season,t_bel),(v_season,t_boh),(v_season,t_tea),(v_season,t_rid),(v_season,t_war),(v_season,t_bla),(v_season,t_kit),
    (v_season,t_ben),(v_season,t_gen),(v_season,t_nai),(v_season,t_dra),(v_season,t_ler),(v_season,t_inf),(v_season,t_chu),(v_season,t_dar),
    (v_season,t_kni),(v_season,t_man),(v_season,t_arc),(v_season,t_mam),(v_season,t_hus),(v_season,t_kaj),(v_season,t_grc),(v_season,t_gru),
    (v_season,t_yos),(v_season,t_rol),(v_season,t_jan),(v_season,t_shi),(v_season,t_pes),(v_season,t_ceu),(v_season,t_rov),(v_season,t_rud);

  -- STAGE 1: Group Stage
  INSERT INTO stages (season_id,name,type,order_number,legs) VALUES (v_season,'Group Stage','GROUP',1,1) RETURNING id INTO s_grp;
  INSERT INTO stage_groups (stage_id,name) VALUES (s_grp,'Group A') RETURNING id INTO ga;
  INSERT INTO stage_groups (stage_id,name) VALUES (s_grp,'Group B') RETURNING id INTO gb;
  INSERT INTO stage_groups (stage_id,name) VALUES (s_grp,'Group C') RETURNING id INTO gc;
  INSERT INTO stage_groups (stage_id,name) VALUES (s_grp,'Group D') RETURNING id INTO gd;
  INSERT INTO stage_groups (stage_id,name) VALUES (s_grp,'Group E') RETURNING id INTO ge;
  INSERT INTO stage_groups (stage_id,name) VALUES (s_grp,'Group F') RETURNING id INTO gf;
  INSERT INTO stage_groups (stage_id,name) VALUES (s_grp,'Group G') RETURNING id INTO gg;
  INSERT INTO stage_groups (stage_id,name) VALUES (s_grp,'Group H') RETURNING id INTO gh;
  INSERT INTO stage_group_teams VALUES (ga,t_brc),(ga,t_bel),(ga,t_boh),(ga,t_tea);
  INSERT INTO stage_group_teams VALUES (gb,t_rid),(gb,t_war),(gb,t_bla),(gb,t_kit);
  INSERT INTO stage_group_teams VALUES (gc,t_ben),(gc,t_gen),(gc,t_nai),(gc,t_dra);
  INSERT INTO stage_group_teams VALUES (gd,t_ler),(gd,t_inf),(gd,t_chu),(gd,t_dar);
  INSERT INTO stage_group_teams VALUES (ge,t_kni),(ge,t_man),(ge,t_arc),(ge,t_mam);
  INSERT INTO stage_group_teams VALUES (gf,t_hus),(gf,t_kaj),(gf,t_grc),(gf,t_gru);
  INSERT INTO stage_group_teams VALUES (gg,t_yos),(gg,t_rol),(gg,t_jan),(gg,t_shi);
  INSERT INTO stage_group_teams VALUES (gh,t_pes),(gh,t_ceu),(gh,t_rov),(gh,t_rud);

  -- MD1
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (1,'MD1',v_season,s_grp) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_brc,t_bel,3,3,'COMPLETED'),(r,t_boh,t_tea,1,1,'COMPLETED'),
    (r,t_rid,t_war,3,0,'COMPLETED'),(r,t_bla,t_kit,3,2,'COMPLETED'),
    (r,t_ben,t_gen,2,0,'COMPLETED'),(r,t_nai,t_dra,2,2,'COMPLETED'),
    (r,t_ler,t_inf,1,1,'COMPLETED'),(r,t_chu,t_dar,1,1,'COMPLETED'),
    (r,t_kni,t_man,3,0,'COMPLETED'),(r,t_arc,t_mam,0,0,'COMPLETED'),
    (r,t_hus,t_kaj,3,1,'COMPLETED'),(r,t_grc,t_gru,0,2,'COMPLETED'),
    (r,t_yos,t_rol,2,3,'COMPLETED'),(r,t_jan,t_shi,3,1,'COMPLETED'),
    (r,t_pes,t_ceu,2,1,'COMPLETED'),(r,t_rov,t_rud,3,3,'COMPLETED');
  -- MD2
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (2,'MD2',v_season,s_grp) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_brc,t_boh,0,0,'COMPLETED'),(r,t_tea,t_bel,2,2,'COMPLETED'),
    (r,t_rid,t_bla,3,1,'COMPLETED'),(r,t_kit,t_war,1,1,'COMPLETED'),
    (r,t_ben,t_nai,0,1,'COMPLETED'),(r,t_dra,t_gen,2,1,'COMPLETED'),
    (r,t_ler,t_chu,4,0,'COMPLETED'),(r,t_dar,t_inf,1,1,'COMPLETED'),
    (r,t_kni,t_arc,2,0,'COMPLETED'),(r,t_mam,t_man,3,0,'COMPLETED'),
    (r,t_hus,t_grc,2,1,'COMPLETED'),(r,t_gru,t_kaj,0,0,'COMPLETED'),
    (r,t_yos,t_jan,1,0,'COMPLETED'),(r,t_shi,t_rol,2,1,'COMPLETED'),
    (r,t_pes,t_rov,3,1,'COMPLETED'),(r,t_rud,t_ceu,1,2,'COMPLETED');
  -- MD3
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (3,'MD3',v_season,s_grp) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_bel,t_boh,2,3,'COMPLETED'),(r,t_tea,t_brc,0,2,'COMPLETED'),
    (r,t_war,t_bla,3,2,'COMPLETED'),(r,t_kit,t_rid,1,0,'COMPLETED'),
    (r,t_gen,t_nai,2,0,'COMPLETED'),(r,t_dra,t_ben,1,2,'COMPLETED'),
    (r,t_inf,t_chu,1,2,'COMPLETED'),(r,t_dar,t_ler,0,0,'COMPLETED'),
    (r,t_man,t_arc,1,2,'COMPLETED'),(r,t_mam,t_kni,2,0,'COMPLETED'),
    (r,t_kaj,t_grc,2,2,'COMPLETED'),(r,t_gru,t_hus,0,3,'COMPLETED'),
    (r,t_rol,t_jan,2,1,'COMPLETED'),(r,t_shi,t_yos,3,3,'COMPLETED'),
    (r,t_ceu,t_rov,2,2,'COMPLETED'),(r,t_rud,t_pes,1,0,'COMPLETED');
  -- MD4
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (4,'MD4',v_season,s_grp) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_boh,t_bel,2,3,'COMPLETED'),(r,t_brc,t_tea,2,1,'COMPLETED'),
    (r,t_bla,t_war,1,1,'COMPLETED'),(r,t_rid,t_kit,3,1,'COMPLETED'),
    (r,t_nai,t_gen,1,1,'COMPLETED'),(r,t_ben,t_dra,2,2,'COMPLETED'),
    (r,t_chu,t_inf,1,3,'COMPLETED'),(r,t_ler,t_dar,0,1,'COMPLETED'),
    (r,t_arc,t_man,1,0,'COMPLETED'),(r,t_kni,t_mam,4,3,'COMPLETED'),
    (r,t_grc,t_kaj,3,0,'COMPLETED'),(r,t_hus,t_gru,0,0,'COMPLETED'),
    (r,t_jan,t_rol,3,3,'COMPLETED'),(r,t_yos,t_shi,1,1,'COMPLETED'),
    (r,t_rov,t_ceu,2,0,'COMPLETED'),(r,t_pes,t_rud,3,2,'COMPLETED');
  -- MD5
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (5,'MD5',v_season,s_grp) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_bel,t_brc,4,2,'COMPLETED'),(r,t_tea,t_boh,2,3,'COMPLETED'),
    (r,t_war,t_rid,2,0,'COMPLETED'),(r,t_kit,t_bla,1,1,'COMPLETED'),
    (r,t_gen,t_ben,3,3,'COMPLETED'),(r,t_dra,t_nai,1,0,'COMPLETED'),
    (r,t_inf,t_ler,0,3,'COMPLETED'),(r,t_dar,t_chu,0,2,'COMPLETED'),
    (r,t_man,t_kni,0,0,'COMPLETED'),(r,t_mam,t_arc,3,1,'COMPLETED'),
    (r,t_kaj,t_hus,1,0,'COMPLETED'),(r,t_gru,t_grc,0,1,'COMPLETED'),
    (r,t_rol,t_yos,3,1,'COMPLETED'),(r,t_shi,t_jan,0,1,'COMPLETED'),
    (r,t_ceu,t_pes,1,1,'COMPLETED'),(r,t_rud,t_rov,2,0,'COMPLETED');
  -- MD6
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (6,'MD6',v_season,s_grp) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_bel,t_tea,5,1,'COMPLETED'),(r,t_boh,t_brc,0,0,'COMPLETED'),
    (r,t_war,t_kit,0,0,'COMPLETED'),(r,t_bla,t_rid,1,0,'COMPLETED'),
    (r,t_gen,t_dra,2,2,'COMPLETED'),(r,t_nai,t_ben,3,2,'COMPLETED'),
    (r,t_inf,t_dar,2,1,'COMPLETED'),(r,t_chu,t_ler,2,2,'COMPLETED'),
    (r,t_man,t_mam,0,0,'COMPLETED'),(r,t_arc,t_kni,0,1,'COMPLETED'),
    (r,t_kaj,t_gru,1,2,'COMPLETED'),(r,t_grc,t_hus,1,1,'COMPLETED'),
    (r,t_rol,t_shi,1,0,'COMPLETED'),(r,t_jan,t_yos,0,2,'COMPLETED'),
    (r,t_ceu,t_rud,0,3,'COMPLETED'),(r,t_rov,t_pes,2,2,'COMPLETED');

  -- Group standings (RECORDED)
  INSERT INTO standings (season_id,team_id,type,stage_group_id,played,won,drawn,lost,goals_for,goals_against,points) VALUES
    (v_season,t_bel,'RECORDED',ga,6,3,2,1,19,13,11),(v_season,t_boh,'RECORDED',ga,6,2,3,1,9,8,9),
    (v_season,t_brc,'RECORDED',ga,6,2,3,1,9,8,9),(v_season,t_tea,'RECORDED',ga,6,0,2,4,7,15,2),
    (v_season,t_rid,'RECORDED',gb,6,3,0,3,9,6,9),(v_season,t_war,'RECORDED',gb,6,2,3,1,7,7,9),
    (v_season,t_bla,'RECORDED',gb,6,2,2,2,9,10,8),(v_season,t_kit,'RECORDED',gb,6,1,3,2,6,8,6),
    (v_season,t_dra,'RECORDED',gc,6,2,3,1,10,9,9),(v_season,t_ben,'RECORDED',gc,6,2,2,2,11,10,8),
    (v_season,t_nai,'RECORDED',gc,6,2,2,2,8,7,8),(v_season,t_gen,'RECORDED',gc,6,1,3,2,9,10,6),
    (v_season,t_ler,'RECORDED',gd,6,2,3,1,10,4,9),(v_season,t_inf,'RECORDED',gd,6,2,2,2,8,9,8),
    (v_season,t_chu,'RECORDED',gd,6,2,2,2,8,11,8),(v_season,t_dar,'RECORDED',gd,6,1,2,3,4,6,5),
    (v_season,t_kni,'RECORDED',ge,6,4,1,1,10,5,13),(v_season,t_mam,'RECORDED',ge,6,3,2,1,11,5,11),
    (v_season,t_arc,'RECORDED',ge,6,2,1,3,4,7,7),(v_season,t_man,'RECORDED',ge,6,0,2,4,1,9,2),
    (v_season,t_hus,'RECORDED',gf,6,3,2,1,9,4,11),(v_season,t_grc,'RECORDED',gf,6,2,2,2,8,7,8),
    (v_season,t_gru,'RECORDED',gf,6,0,2,4,4,5,8),(v_season,t_kaj,'RECORDED',gf,6,1,2,3,5,10,5),
    (v_season,t_rol,'RECORDED',gg,6,4,1,1,13,9,13),(v_season,t_yos,'RECORDED',gg,6,2,2,2,10,10,8),
    (v_season,t_jan,'RECORDED',gg,6,2,1,3,8,9,7),(v_season,t_shi,'RECORDED',gg,6,1,2,3,7,10,5),
    (v_season,t_pes,'RECORDED',gh,6,3,2,1,11,8,11),(v_season,t_rud,'RECORDED',gh,6,3,1,2,12,8,10),
    (v_season,t_rov,'RECORDED',gh,6,1,3,2,10,12,6),(v_season,t_ceu,'RECORDED',gh,6,1,2,3,6,11,5);

  -- STAGE 2: Elimination Groups II
  INSERT INTO stages (season_id,name,type,order_number,legs) VALUES (v_season,'Elimination Groups II','GROUP',2,1) RETURNING id INTO s_elim;
  INSERT INTO stage_groups (stage_id,name) VALUES (s_elim,'Group A') RETURNING id INTO ea;
  INSERT INTO stage_groups (stage_id,name) VALUES (s_elim,'Group B') RETURNING id INTO eb;
  INSERT INTO stage_groups (stage_id,name) VALUES (s_elim,'Group C') RETURNING id INTO ec;
  INSERT INTO stage_groups (stage_id,name) VALUES (s_elim,'Group D') RETURNING id INTO ed;
  INSERT INTO stage_group_teams VALUES (ea,t_hus),(ea,t_pes),(ea,t_rid),(ea,t_rol);
  INSERT INTO stage_group_teams VALUES (eb,t_boh),(eb,t_war),(eb,t_inf),(eb,t_grc);
  INSERT INTO stage_group_teams VALUES (ec,t_yos),(ec,t_dra),(ec,t_rud),(ec,t_mam);
  INSERT INTO stage_group_teams VALUES (ed,t_ben),(ed,t_kni),(ed,t_bel),(ed,t_ler);

  -- Elim MD1
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (7,'Elim MD1',v_season,s_elim) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_hus,t_pes,2,2,'COMPLETED'),(r,t_rid,t_rol,0,0,'COMPLETED'),
    (r,t_boh,t_war,1,3,'COMPLETED'),(r,t_inf,t_grc,2,0,'COMPLETED'),
    (r,t_yos,t_dra,0,0,'COMPLETED'),(r,t_rud,t_mam,1,2,'COMPLETED'),
    (r,t_ben,t_kni,1,2,'COMPLETED'),(r,t_bel,t_ler,2,2,'COMPLETED');
  -- Elim MD2
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (8,'Elim MD2',v_season,s_elim) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_hus,t_rid,1,1,'COMPLETED'),(r,t_rol,t_pes,1,2,'COMPLETED'),
    (r,t_boh,t_inf,1,1,'COMPLETED'),(r,t_grc,t_war,1,0,'COMPLETED'),
    (r,t_yos,t_rud,3,3,'COMPLETED'),(r,t_mam,t_dra,3,0,'COMPLETED'),
    (r,t_ben,t_bel,2,1,'COMPLETED'),(r,t_ler,t_kni,0,0,'COMPLETED');
  -- Elim MD3
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (9,'Elim MD3',v_season,s_elim) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_pes,t_rid,2,3,'COMPLETED'),(r,t_rol,t_hus,1,1,'COMPLETED'),
    (r,t_war,t_inf,3,3,'COMPLETED'),(r,t_grc,t_boh,1,0,'COMPLETED'),
    (r,t_dra,t_rud,1,0,'COMPLETED'),(r,t_mam,t_yos,0,0,'COMPLETED'),
    (r,t_kni,t_bel,1,0,'COMPLETED'),(r,t_ler,t_ben,2,0,'COMPLETED');
  -- Elim MD4
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (10,'Elim MD4',v_season,s_elim) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_rid,t_pes,0,2,'COMPLETED'),(r,t_hus,t_rol,2,0,'COMPLETED'),
    (r,t_inf,t_war,2,2,'COMPLETED'),(r,t_boh,t_grc,0,0,'COMPLETED'),
    (r,t_rud,t_dra,3,1,'COMPLETED'),(r,t_yos,t_mam,0,3,'COMPLETED'),
    (r,t_bel,t_kni,2,0,'COMPLETED'),(r,t_ben,t_ler,0,0,'COMPLETED');
  -- Elim MD5
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (11,'Elim MD5',v_season,s_elim) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_pes,t_hus,2,1,'COMPLETED'),(r,t_rol,t_rid,2,2,'COMPLETED'),
    (r,t_war,t_boh,0,2,'COMPLETED'),(r,t_grc,t_inf,1,1,'COMPLETED'),
    (r,t_dra,t_yos,2,0,'COMPLETED'),(r,t_mam,t_rud,0,0,'COMPLETED'),
    (r,t_kni,t_ben,3,2,'COMPLETED'),(r,t_ler,t_bel,1,3,'COMPLETED');
  -- Elim MD6
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (12,'Elim MD6',v_season,s_elim) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,status) VALUES
    (r,t_pes,t_rol,0,0,'COMPLETED'),(r,t_rid,t_hus,0,3,'COMPLETED'),
    (r,t_war,t_grc,2,2,'COMPLETED'),(r,t_inf,t_boh,0,1,'COMPLETED'),
    (r,t_dra,t_mam,2,1,'COMPLETED'),(r,t_rud,t_yos,0,1,'COMPLETED'),
    (r,t_bel,t_ben,1,0,'COMPLETED'),(r,t_kni,t_ler,2,3,'COMPLETED');

  -- Elim standings
  INSERT INTO standings (season_id,team_id,type,stage_group_id,played,won,drawn,lost,goals_for,goals_against,points) VALUES
    (v_season,t_pes,'RECORDED',ea,6,3,2,1,10,6,11),(v_season,t_hus,'RECORDED',ea,6,2,3,1,10,7,9),
    (v_season,t_rid,'RECORDED',ea,6,1,3,2,6,10,6),(v_season,t_rol,'RECORDED',ea,6,0,4,2,4,7,4),
    (v_season,t_grc,'RECORDED',eb,6,2,3,1,5,5,9),(v_season,t_boh,'RECORDED',eb,6,2,2,2,5,5,8),
    (v_season,t_inf,'RECORDED',eb,6,1,4,1,9,8,7),(v_season,t_war,'RECORDED',eb,6,1,3,2,10,11,6),
    (v_season,t_mam,'RECORDED',ec,6,3,2,1,9,3,11),(v_season,t_dra,'RECORDED',ec,6,3,1,2,6,7,10),
    (v_season,t_yos,'RECORDED',ec,6,1,3,2,4,8,6),(v_season,t_rud,'RECORDED',ec,6,1,2,3,7,8,5),
    (v_season,t_bel,'RECORDED',ed,6,3,1,2,9,6,10),(v_season,t_kni,'RECORDED',ed,6,3,1,2,8,8,10),
    (v_season,t_ler,'RECORDED',ed,6,2,3,1,8,7,9),(v_season,t_ben,'RECORDED',ed,6,1,1,4,5,9,4);

  -- STAGE 3: Quarter Final (2 legs)
  INSERT INTO stages (season_id,name,type,order_number,legs) VALUES (v_season,'Quarter Final','KNOCKOUT',3,2) RETURNING id INTO s_qf;
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (13,'QF Leg 1',v_season,s_qf) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,leg,status) VALUES
    (r,t_kni,t_hus,4,0,1,'COMPLETED'),(r,t_boh,t_pes,2,2,1,'COMPLETED'),
    (r,t_grc,t_mam,1,1,1,'COMPLETED'),(r,t_bel,t_dra,1,0,1,'COMPLETED');
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (14,'QF Leg 2',v_season,s_qf) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,leg,status) VALUES
    (r,t_hus,t_kni,3,2,2,'COMPLETED'),(r,t_pes,t_boh,2,0,2,'COMPLETED'),
    (r,t_mam,t_grc,3,0,2,'COMPLETED'),(r,t_dra,t_bel,1,2,2,'COMPLETED');

  -- STAGE 4: Semi Final (2 legs)
  INSERT INTO stages (season_id,name,type,order_number,legs) VALUES (v_season,'Semi Final','KNOCKOUT',4,2) RETURNING id INTO s_sf;
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (15,'SF Leg 1',v_season,s_sf) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,leg,status) VALUES
    (r,t_pes,t_kni,0,2,1,'COMPLETED'),(r,t_mam,t_bel,1,2,1,'COMPLETED');
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (16,'SF Leg 2',v_season,s_sf) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,leg,status) VALUES
    (r,t_kni,t_pes,3,3,2,'COMPLETED'),(r,t_bel,t_mam,3,1,2,'COMPLETED');

  -- STAGE 5: Final
  INSERT INTO stages (season_id,name,type,order_number,legs) VALUES (v_season,'Final','KNOCKOUT',5,1) RETURNING id INTO s_fin;
  INSERT INTO rounds (round_number,name,season_id,stage_id) VALUES (17,'Final',v_season,s_fin) RETURNING id INTO r;
  INSERT INTO matches (round_id,home_team_id,away_team_id,home_score,away_score,leg,status,notes) VALUES
    (r,t_kni,t_bel,2,1,1,'COMPLETED','Mainer Park');

END $$;
