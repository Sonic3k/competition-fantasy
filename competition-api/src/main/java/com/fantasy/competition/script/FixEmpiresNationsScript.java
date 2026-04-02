package com.fantasy.competition.script;

import org.springframework.stereotype.Component;
import javax.sql.DataSource;

@Component
public class FixEmpiresNationsScript extends SqlImportScript {
    public FixEmpiresNationsScript(DataSource ds) { super(ds); }
    @Override public String getId() { return "fix-empires-nations"; }
    @Override public String getName() { return "Fix Empires Club Nations"; }
    @Override public String getDescription() { return "Links all 32 Empires clubs to their correct nation (Britons, Teutons, Byzantines, etc)."; }
    @Override public String getIcon() { return "🔗"; }
    @Override protected String getSql() {
        return """
            DO $$
            DECLARE v_uni UUID;
            BEGIN
              SELECT id INTO v_uni FROM universes WHERE name LIKE '%Empires%' OR name LIKE '%AOE%' OR name LIKE '%Age%' LIMIT 1;
              IF v_uni IS NULL THEN RAISE NOTICE 'No Empires universe'; RETURN; END IF;

              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Britons' AND universe_id=v_uni LIMIT 1) WHERE name='British City' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Britons' AND universe_id=v_uni LIMIT 1) WHERE name='Dark III' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Byzantines' AND universe_id=v_uni LIMIT 1) WHERE name='Belisarius United' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Byzantines' AND universe_id=v_uni LIMIT 1) WHERE name='Greek United' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Teutons' AND universe_id=v_uni LIMIT 1) WHERE name='Bohemia' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Teutons' AND universe_id=v_uni LIMIT 1) WHERE name='Blastoise' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Teutons' AND universe_id=v_uni LIMIT 1) WHERE name='Knight Templar' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Teutons' AND universe_id=v_uni LIMIT 1) WHERE name='Rudolph Swabian' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Chinese' AND universe_id=v_uni LIMIT 1) WHERE name='Teamton' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Chinese' AND universe_id=v_uni LIMIT 1) WHERE name='Chu-Koo-Nu' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Celts' AND universe_id=v_uni LIMIT 1) WHERE name='Rider Woadwood' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Celts' AND universe_id=v_uni LIMIT 1) WHERE name='Green Cycene' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Celts' AND universe_id=v_uni LIMIT 1) WHERE name='Celt United' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Persians' AND universe_id=v_uni LIMIT 1) WHERE name='War Elephant' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Persians' AND universe_id=v_uni LIMIT 1) WHERE name='Rover Shah' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Japanese' AND universe_id=v_uni LIMIT 1) WHERE name='Kitabatake' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Japanese' AND universe_id=v_uni LIMIT 1) WHERE name='Kajinuo' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Japanese' AND universe_id=v_uni LIMIT 1) WHERE name='Yoshiga Minamoto' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Saracens' AND universe_id=v_uni LIMIT 1) WHERE name='Benlizihad United' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Saracens' AND universe_id=v_uni LIMIT 1) WHERE name='Mameluke' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Goths' AND universe_id=v_uni LIMIT 1) WHERE name='General Siede' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Goths' AND universe_id=v_uni LIMIT 1) WHERE name='Huskarl' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Mongols' AND universe_id=v_uni LIMIT 1) WHERE name='Naiman' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Mongols' AND universe_id=v_uni LIMIT 1) WHERE name='Mangudai' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Mongols' AND universe_id=v_uni LIMIT 1) WHERE name='Persata' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Franks' AND universe_id=v_uni LIMIT 1) WHERE name='Dragon Burgundy' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Franks' AND universe_id=v_uni LIMIT 1) WHERE name='Arc Main' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Franks' AND universe_id=v_uni LIMIT 1) WHERE name='Roland' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Vikings' AND universe_id=v_uni LIMIT 1) WHERE name='Infantry Berserk' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Vikings' AND universe_id=v_uni LIMIT 1) WHERE name='Ship Parcicion' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Turks' AND universe_id=v_uni LIMIT 1) WHERE name='Lerberk' AND universe_id=v_uni;
              UPDATE teams SET nation_id = (SELECT id FROM nations WHERE name='Turks' AND universe_id=v_uni LIMIT 1) WHERE name='Janissary' AND universe_id=v_uni;
            END $$;
            """;
    }
}
