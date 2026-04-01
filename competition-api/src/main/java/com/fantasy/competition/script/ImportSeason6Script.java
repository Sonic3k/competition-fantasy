package com.fantasy.competition.script;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Component
public class ImportSeason6Script extends SqlImportScript {
    public ImportSeason6Script(DataSource ds) { super(ds); }
    @Override public String getId() { return "import-othelose-season6"; }
    @Override public String getName() { return "Import Othelose Season 6"; }
    @Override public String getDescription() { return "16 teams (2 new: Processing Probal, Willandos; Domehampton returns), 30 rounds, 240 matches. Champion: Hrilling Best (64 pts, 3rd consecutive title!)."; }
    @Override public String getIcon() { return "⚽"; }
    @Override protected String getSql() {
        try { return new ClassPathResource("scripts/season6_othelose.sql").getContentAsString(StandardCharsets.UTF_8); }
        catch (Exception e) { throw new RuntimeException("Cannot read SQL", e); }
    }
}
