package com.fantasy.competition.script;

import org.springframework.stereotype.Component;
import javax.sql.DataSource;

@Component
public class FixSeasonYearsScript extends SqlImportScript {
    public FixSeasonYearsScript(DataSource ds) { super(ds); }
    @Override public String getId() { return "fix-season-years"; }
    @Override public String getName() { return "Fix Season Years"; }
    @Override public String getDescription() { return "Correct years: Season 4 = 2003, Season 5 = 2004, Season 6 = 2004."; }
    @Override public String getIcon() { return "📅"; }
    @Override protected String getSql() {
        return """
            UPDATE seasons SET year = 2003 WHERE name = 'Season 4';
            UPDATE seasons SET year = 2004 WHERE name = 'Season 5';
            UPDATE seasons SET year = 2004 WHERE name = 'Season 6';
        """;
    }
}
