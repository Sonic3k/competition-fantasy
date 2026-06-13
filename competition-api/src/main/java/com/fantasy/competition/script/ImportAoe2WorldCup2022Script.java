package com.fantasy.competition.script;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Component
public class ImportAoe2WorldCup2022Script extends SqlImportScript {
    public ImportAoe2WorldCup2022Script(DataSource ds) { super(ds); }
    @Override public String getId() { return "import-aoe2-world-cup-2022"; }
    @Override public String getName() { return "AOE2 World Cup 2022 (setup)"; }
    @Override public String getDescription() {
        return "AOE2 Universe: 32 strong AOE2 DE civilisations as national teams + a World Cup 2022 competition/season (WORLD_CUP_32). Creates no stages — open the season and click Generate to build the 8 groups + Round of 16/QF/SF/Final + third-place play-off, then enter scores.";
    }
    @Override public String getIcon() { return "🏆"; }
    @Override protected String getSql() {
        try { return new ClassPathResource("scripts/aoe2_world_cup_2022.sql").getContentAsString(StandardCharsets.UTF_8); }
        catch (Exception e) { throw new RuntimeException("Cannot read SQL", e); }
    }
}
