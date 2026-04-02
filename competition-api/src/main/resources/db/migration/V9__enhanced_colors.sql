-- Nation: add code and colors
ALTER TABLE nations ADD COLUMN code VARCHAR(3);
ALTER TABLE nations ADD COLUMN primary_color VARCHAR(7);
ALTER TABLE nations ADD COLUMN text_color VARCHAR(7);

-- Team: replace simple colors with home/away pairs
ALTER TABLE teams ADD COLUMN home_bg VARCHAR(7);
ALTER TABLE teams ADD COLUMN home_text VARCHAR(7);
ALTER TABLE teams ADD COLUMN away_bg VARCHAR(7);
ALTER TABLE teams ADD COLUMN away_text VARCHAR(7);

-- Migrate existing colors
UPDATE teams SET home_bg = primary_color, home_text = '#ffffff', away_bg = secondary_color, away_text = '#000000'
  WHERE primary_color IS NOT NULL;

-- Can drop old columns later, keep for now
