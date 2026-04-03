package com.fantasy.competition.script;

import org.springframework.stereotype.Component;
import javax.sql.DataSource;

@Component
public class GenerateMatchDatesScript extends SqlImportScript {
    public GenerateMatchDatesScript(DataSource ds) { super(ds); }
    @Override public String getId() { return "generate-match-dates"; }
    @Override public String getName() { return "Generate Match Dates"; }
    @Override public String getDescription() { return "S4=Aug-Nov 2003, S5=Feb-May 2004, S6=Aug-Nov 2004, Wonder Empires=Mar-Aug 2004. 4 months each."; }
    @Override public String getIcon() { return "📅"; }
    @Override protected String getSql() {
        return """
            DO $$
            DECLARE
              s RECORD; r RECORD; m RECORD;
              v_start DATE; v_days_span INT; v_total_rounds INT;
              v_round_date DATE; v_match_date TIMESTAMP; v_match_idx INT;
            BEGIN
              UPDATE matches SET match_date = NULL;

              FOR s IN
                SELECT se.id, se.name, c.name as comp_name
                FROM seasons se JOIN competitions c ON se.competition_id = c.id
              LOOP
                SELECT COUNT(DISTINCT round_number) INTO v_total_rounds FROM rounds WHERE season_id = s.id;
                IF v_total_rounds = 0 THEN CONTINUE; END IF;

                IF s.name = 'Season 4' AND s.comp_name = 'Othelose League' THEN
                  v_start := '2003-08-02'::DATE; v_days_span := 110;
                ELSIF s.name = 'Season 5' AND s.comp_name = 'Othelose League' THEN
                  v_start := '2004-02-07'::DATE; v_days_span := 110;
                ELSIF s.name = 'Season 6' AND s.comp_name = 'Othelose League' THEN
                  v_start := '2004-08-07'::DATE; v_days_span := 110;
                ELSIF s.comp_name = 'Wonder Empires Football' THEN
                  v_start := '2004-03-02'::DATE; v_days_span := 155;
                ELSE
                  v_start := (COALESCE(s.name, '2003') || '-01-15')::DATE; v_days_span := 120;
                END IF;

                FOR r IN SELECT DISTINCT round_number FROM rounds WHERE season_id = s.id ORDER BY round_number LOOP
                  v_round_date := v_start + ((r.round_number - 1) * v_days_span / GREATEST(v_total_rounds - 1, 1));
                  v_match_idx := 0;
                  FOR m IN SELECT ma.id FROM matches ma JOIN rounds rd ON ma.round_id = rd.id
                    WHERE rd.season_id = s.id AND rd.round_number = r.round_number ORDER BY ma.id
                  LOOP
                    v_match_date := v_round_date + (v_match_idx * INTERVAL '30 minutes') + INTERVAL '15 hours';
                    UPDATE matches SET match_date = v_match_date WHERE id = m.id;
                    v_match_idx := v_match_idx + 1;
                  END LOOP;
                END LOOP;
              END LOOP;
            END $$;
            """;
    }
}
