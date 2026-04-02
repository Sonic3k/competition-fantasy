package com.fantasy.competition.script;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Component
public class BuildAllNationsScript extends SqlImportScript {
    public BuildAllNationsScript(DataSource ds) { super(ds); }
    @Override public String getId() { return "build-all-aoe2-nations"; }
    @Override public String getName() { return "Build All AOE2 Nations (47)"; }
    @Override public String getDescription() { return "Creates/updates all 47 AOE2 civilizations as nations with 3-char codes and colors. Excludes Wei, Wu, Shu."; }
    @Override public String getIcon() { return "🏛️"; }
    @Override protected String getSql() {
        try { return new ClassPathResource("scripts/build_all_nations.sql").getContentAsString(StandardCharsets.UTF_8); }
        catch (Exception e) { throw new RuntimeException("Cannot read SQL", e); }
    }
}
