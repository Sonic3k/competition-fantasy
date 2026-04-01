package com.fantasy.competition.script;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Component
public class ImportSeason5Script extends SqlImportScript {

    public ImportSeason5Script(DataSource dataSource) { super(dataSource); }

    @Override public String getId() { return "import-othelose-season5"; }
    @Override public String getName() { return "Import Othelose Season 5"; }
    @Override public String getDescription() { return "16 teams (3 new: Balland Wolve, Cantone, Ilham Rower), 30 rounds, 240 matches. Champion: Hrilling Best (63 pts)."; }
    @Override public String getIcon() { return "⚽"; }

    @Override
    protected String getSql() {
        try {
            return new ClassPathResource("scripts/season5_othelose.sql").getContentAsString(StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Cannot read SQL file", e); }
    }
}
