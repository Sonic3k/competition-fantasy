package com.fantasy.competition.script;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Component
public class ImportWonderEmpiresScript extends SqlImportScript {
    public ImportWonderEmpiresScript(DataSource ds) { super(ds); }
    @Override public String getId() { return "import-wonder-empires-football"; }
    @Override public String getName() { return "Import Wonder Empires Football"; }
    @Override public String getDescription() { return "AOE2 Universe: 13 Nations, 32 Clubs, Group Stage (8 groups), Elimination Groups II (4 groups), Quarter/Semi/Final (2 legs). Champion: Knight Templar (Teutons)."; }
    @Override public String getIcon() { return "🏰"; }
    @Override protected String getSql() {
        try { return new ClassPathResource("scripts/wonder_empires_football.sql").getContentAsString(StandardCharsets.UTF_8); }
        catch (Exception e) { throw new RuntimeException("Cannot read SQL", e); }
    }
}
