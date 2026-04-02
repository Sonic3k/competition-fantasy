package com.fantasy.competition.script;

import org.springframework.stereotype.Component;
import javax.sql.DataSource;

@Component
public class CleanWonderEmpiresScript extends SqlImportScript {
    public CleanWonderEmpiresScript(DataSource ds) { super(ds); }
    @Override public String getId() { return "clean-wonder-empires"; }
    @Override public String getName() { return "Clean Wonder Empires Data"; }
    @Override public String getDescription() { return "Deletes all Wonder Empires Football data (competition, seasons, matches, standings, stages, teams, nations) so it can be re-imported."; }
    @Override public String getIcon() { return "🗑️"; }
    @Override protected String getSql() {
        return """
            DO $$
            DECLARE v_uni UUID; v_comp UUID;
            BEGIN
              SELECT id INTO v_uni FROM universes WHERE name LIKE '%Empires%' OR name LIKE '%AOE%' OR name LIKE '%Age%' LIMIT 1;
              IF v_uni IS NULL THEN RAISE NOTICE 'No Empires universe'; RETURN; END IF;
              SELECT id INTO v_comp FROM competitions WHERE name='Wonder Empires Football' AND universe_id=v_uni;
              IF v_comp IS NULL THEN RAISE NOTICE 'No competition found'; RETURN; END IF;

              -- Cascade: seasons -> stages -> stage_groups -> rounds -> matches -> standings
              DELETE FROM competitions WHERE id = v_comp;
              -- Delete teams and nations created by the import
              DELETE FROM teams WHERE universe_id = v_uni;
              DELETE FROM nations WHERE universe_id = v_uni;
              RAISE NOTICE 'Wonder Empires data cleaned';
            END $$;
            """;
    }
}
