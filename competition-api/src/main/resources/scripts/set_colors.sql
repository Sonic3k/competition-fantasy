-- =============================================
-- OTHELOSE TEAMS - from notebook jersey drawings
-- Home = body color + text color from jersey
-- Away = inverted/contrasting (invented)
-- =============================================

-- Hrilling Best: green body, yellow trim
UPDATE teams SET home_bg='#00aa44', home_text='#ffff00', away_bg='#ffff00', away_text='#00aa44' WHERE name='Hrilling Best';
-- Acer Robert: navy blue body
UPDATE teams SET home_bg='#2244aa', home_text='#ffffff', away_bg='#ffffff', away_text='#2244aa' WHERE name='Acer Robert';
-- Notes Valzine: red/pink body, blue shorts
UPDATE teams SET home_bg='#cc2255', home_text='#ffffff', away_bg='#ffffff', away_text='#cc2255' WHERE name='Notes Valzine';
-- Iteser Icer: white body, blue shorts
UPDATE teams SET home_bg='#ffffff', home_text='#003388', away_bg='#003388', away_text='#ffffff' WHERE name='Iteser Icer';
-- Ben Derber: blue body, dark shorts
UPDATE teams SET home_bg='#0044cc', home_text='#ffffff', away_bg='#ffffff', away_text='#0044cc' WHERE name='Ben Derber';
-- Fulhaster: black/dark jersey (X pattern)
UPDATE teams SET home_bg='#222222', home_text='#ffffff', away_bg='#ffffff', away_text='#222222' WHERE name='Fulhaster';
-- Balland Wolve: yellow body, green shorts
UPDATE teams SET home_bg='#dddd00', home_text='#006622', away_bg='#006622', away_text='#dddd00' WHERE name='Balland Wolve';
-- Mener Lying: red body, blue text
UPDATE teams SET home_bg='#cc0000', home_text='#0000cc', away_bg='#ffffff', away_text='#cc0000' WHERE name='Mener Lying';
-- Ilham Rower: purple body
UPDATE teams SET home_bg='#6633aa', home_text='#ffffff', away_bg='#ffffff', away_text='#6633aa' WHERE name='Ilham Rower';
-- Rab Pladineer: blue body, red text
UPDATE teams SET home_bg='#0055cc', home_text='#cc0000', away_bg='#cc0000', away_text='#ffffff' WHERE name='Rab Pladineer';
-- Processing Probal: dark blue body
UPDATE teams SET home_bg='#334488', home_text='#ffffff', away_bg='#ffffff', away_text='#334488' WHERE name='Processing Probal';
-- Mize Mole: pink body, blue text
UPDATE teams SET home_bg='#ff6699', home_text='#0044aa', away_bg='#ffffff', away_text='#ff6699' WHERE name='Mize Mole';
-- Willandos: green body
UPDATE teams SET home_bg='#008833', home_text='#ffffff', away_bg='#ffffff', away_text='#008833' WHERE name='Willandos';
-- Domehampton: light blue body
UPDATE teams SET home_bg='#5588bb', home_text='#000000', away_bg='#000000', away_text='#5588bb' WHERE name='Domehampton';
-- Tatines David: red body
UPDATE teams SET home_bg='#cc0033', home_text='#ffffff', away_bg='#ffffff', away_text='#cc0033' WHERE name='Tatines David';
-- Cantone: red/magenta body
UPDATE teams SET home_bg='#cc0066', home_text='#ffffff', away_bg='#ffffff', away_text='#cc0066' WHERE name='Cantone';
-- Nrucaste: black & white stripes
UPDATE teams SET home_bg='#111111', home_text='#ffffff', away_bg='#ffffff', away_text='#111111' WHERE name='Nrucaste';
-- Twine Oner: teal/cyan body
UPDATE teams SET home_bg='#008888', home_text='#ffffff', away_bg='#ffffff', away_text='#008888' WHERE name='Twine Oner';
-- Occating Menchiote: green/teal body
UPDATE teams SET home_bg='#338855', home_text='#ffffff', away_bg='#ffffff', away_text='#338855' WHERE name='Occating Menchiote';
-- Eldiadion: dark red body
UPDATE teams SET home_bg='#880000', home_text='#ffffff', away_bg='#ffffff', away_text='#880000' WHERE name='Eldiadion';
-- Pulzerr: purple/magenta body
UPDATE teams SET home_bg='#aa00aa', home_text='#ffffff', away_bg='#ffffff', away_text='#aa00aa' WHERE name='Pulzerr';

-- Future teams from the notebook (not yet in DB but ready when imported)
-- Pretton: yellow-green
UPDATE teams SET home_bg='#99cc00', home_text='#333333', away_bg='#333333', away_text='#99cc00' WHERE name='Pretton';
-- Velt Cotroom: green
UPDATE teams SET home_bg='#006600', home_text='#ffffff', away_bg='#ffffff', away_text='#006600' WHERE name='Velt Cotroom';
-- Gafield May: yellow
UPDATE teams SET home_bg='#ffcc00', home_text='#333333', away_bg='#333333', away_text='#ffcc00' WHERE name='Gafield May';
-- Eston Perse: green
UPDATE teams SET home_bg='#339933', home_text='#ffffff', away_bg='#ffffff', away_text='#339933' WHERE name='Eston Perse';
-- Burline Paul: light blue
UPDATE teams SET home_bg='#88bbee', home_text='#003366', away_bg='#003366', away_text='#88bbee' WHERE name='Burline Paul';
-- Southinek: pink
UPDATE teams SET home_bg='#ff66cc', home_text='#ffffff', away_bg='#ffffff', away_text='#ff66cc' WHERE name='Southinek';
-- Sancity Rook: red
UPDATE teams SET home_bg='#ff3333', home_text='#ffffff', away_bg='#ffffff', away_text='#ff3333' WHERE name='Sancity Rook';
-- Eldos Glove: yellow + red
UPDATE teams SET home_bg='#ffcc00', home_text='#cc0000', away_bg='#cc0000', away_text='#ffcc00' WHERE name='Eldos Glove';
-- Rating Chat: teal
UPDATE teams SET home_bg='#339999', home_text='#ffffff', away_bg='#ffffff', away_text='#339999' WHERE name='Rating Chat';
-- Tonist Gunner: red
UPDATE teams SET home_bg='#cc0000', home_text='#ffffff', away_bg='#ffffff', away_text='#cc0000' WHERE name='Tonist Gunner';
-- Lopezton: blue
UPDATE teams SET home_bg='#0033cc', home_text='#ffffff', away_bg='#ffffff', away_text='#0033cc' WHERE name='Lopezton';
-- Tookhampton: green
UPDATE teams SET home_bg='#55bb55', home_text='#ffffff', away_bg='#ffffff', away_text='#55bb55' WHERE name='Tookhampton';
-- Pikeman: light blue
UPDATE teams SET home_bg='#55bbcc', home_text='#ffffff', away_bg='#ffffff', away_text='#55bbcc' WHERE name='Pikeman';
-- Rawling Watton: olive green
UPDATE teams SET home_bg='#669933', home_text='#ffffff', away_bg='#ffffff', away_text='#669933' WHERE name='Rawling Watton';
-- Run Suburb: green
UPDATE teams SET home_bg='#009944', home_text='#ffffff', away_bg='#ffffff', away_text='#009944' WHERE name='Run Suburb';
-- Menast Mole: blue/white
UPDATE teams SET home_bg='#3366cc', home_text='#ffffff', away_bg='#ffffff', away_text='#3366cc' WHERE name='Menast Mole';
-- Force Mile: red
UPDATE teams SET home_bg='#cc3333', home_text='#ffffff', away_bg='#ffffff', away_text='#cc3333' WHERE name='Force Mile';
-- Cretton: black
UPDATE teams SET home_bg='#333333', home_text='#ffffff', away_bg='#ffffff', away_text='#333333' WHERE name='Cretton';
-- Zawach Lines: blue
UPDATE teams SET home_bg='#3333cc', home_text='#ffffff', away_bg='#ffffff', away_text='#3333cc' WHERE name='Zawach Lines';
-- Wilham Delle: light blue/teal
UPDATE teams SET home_bg='#88bbaa', home_text='#003366', away_bg='#003366', away_text='#88bbaa' WHERE name='Wilham Delle';
-- Millwall Edison: blue-green
UPDATE teams SET home_bg='#336699', home_text='#ffffff', away_bg='#ffffff', away_text='#336699' WHERE name='Millwall Edison';
-- Balham: red/salmon
UPDATE teams SET home_bg='#cc6666', home_text='#ffffff', away_bg='#ffffff', away_text='#cc6666' WHERE name='Balham';
-- Lar Crane: beige/khaki
UPDATE teams SET home_bg='#bbaa77', home_text='#333333', away_bg='#333333', away_text='#bbaa77' WHERE name='Lar Crane';
-- Noneson Lab: green
UPDATE teams SET home_bg='#009966', home_text='#ffffff', away_bg='#ffffff', away_text='#009966' WHERE name='Noneson Lab';
-- Rincipal Trolineecie: red
UPDATE teams SET home_bg='#cc3333', home_text='#ffffff', away_bg='#ffffff', away_text='#cc3333' WHERE name='Rincipal Trolineecie';

-- =============================================
-- EMPIRES NATIONS - code + colors
-- =============================================
UPDATE nations SET code='BRI', primary_color='#cc0000', text_color='#ffffff' WHERE name='Britons';
UPDATE nations SET code='BYZ', primary_color='#6600cc', text_color='#ffffff' WHERE name='Byzantines';
UPDATE nations SET code='TEU', primary_color='#333333', text_color='#ffffff' WHERE name='Teutons';
UPDATE nations SET code='CHI', primary_color='#cc0000', text_color='#ffcc00' WHERE name='Chinese';
UPDATE nations SET code='CEL', primary_color='#006633', text_color='#ffffff' WHERE name='Celts';
UPDATE nations SET code='PER', primary_color='#cc6600', text_color='#ffffff' WHERE name='Persians';
UPDATE nations SET code='JAP', primary_color='#ffffff', text_color='#cc0000' WHERE name='Japanese';
UPDATE nations SET code='SAR', primary_color='#cccc00', text_color='#000000' WHERE name='Saracens';
UPDATE nations SET code='GOT', primary_color='#663300', text_color='#ffffff' WHERE name='Goths';
UPDATE nations SET code='MON', primary_color='#003399', text_color='#ffffff' WHERE name='Mongols';
UPDATE nations SET code='FRA', primary_color='#0033cc', text_color='#ffffff' WHERE name='Franks';
UPDATE nations SET code='VIK', primary_color='#666666', text_color='#ffffff' WHERE name='Vikings';
UPDATE nations SET code='TUR', primary_color='#cc0000', text_color='#ffffff' WHERE name='Turks';
