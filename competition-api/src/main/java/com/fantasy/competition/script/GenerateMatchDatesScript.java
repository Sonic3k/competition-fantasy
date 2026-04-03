package com.fantasy.competition.script;

import org.springframework.stereotype.Component;
import javax.sql.DataSource;

@Component
public class GenerateMatchDatesScript extends SqlImportScript {
    public GenerateMatchDatesScript(DataSource ds) { super(ds); }
    @Override public String getId() { return "generate-match-dates"; }
    @Override public String getName() { return "Generate Match Dates"; }
    @Override public String getDescription() { return "Assigns realistic match dates to all matches based on season year and round number. League: Aug-May spread. Cup: Sep-Jan."; }
    @Override public String getIcon() { return "📅"; }
    @Override protected String getSql() {
        return """
            DO $$
            DECLARE
              s RECORD;
              r RECORD;
              m RECORD;
              v_year INT;
              v_start DATE;
              v_total_rounds INT;
              v_interval INT;
              v_match_date TIMESTAMP;
              v_round_date DATE;
              v_match_idx INT;
            BEGIN
              FOR s IN SELECT id, year, name, competition_id FROM seasons LOOP
                v_year := COALESCE(s.year, 2003);

                -- Count distinct round numbers in this season
                SELECT COUNT(DISTINCT round_number) INTO v_total_rounds
                FROM rounds WHERE season_id = s.id;

                IF v_total_rounds = 0 THEN CONTINUE; END IF;

                -- League (30 rounds): Aug to May = ~280 days
                -- Cup (fewer rounds): Sep to Jan = ~150 days
                IF v_total_rounds >= 20 THEN
                  v_start := (v_year || '-08-16')::DATE;
                  v_interval := 280 / v_total_rounds;
                ELSE
                  v_start := (v_year || '-09-10')::DATE;
                  v_interval := 150 / GREATEST(v_total_rounds, 1);
                END IF;

                -- For each round, assign date
                FOR r IN SELECT DISTINCT round_number FROM rounds WHERE season_id = s.id ORDER BY round_number LOOP
                  v_round_date := v_start + ((r.round_number - 1) * v_interval * INTERVAL '1 day');
                  v_match_idx := 0;

                  -- Each match in this round gets the same date, staggered by hours
                  FOR m IN SELECT ma.id FROM matches ma
                    JOIN rounds rd ON ma.round_id = rd.id
                    WHERE rd.season_id = s.id AND rd.round_number = r.round_number
                    ORDER BY ma.id
                  LOOP
                    v_match_date := v_round_date + (v_match_idx * INTERVAL '2 hours') + INTERVAL '15 hours';
                    UPDATE matches SET match_date = v_match_date WHERE id = m.id AND match_date IS NULL;
                    v_match_idx := v_match_idx + 1;
                  END LOOP;
                END LOOP;
              END LOOP;
            END $$;
            """;
    }
}
