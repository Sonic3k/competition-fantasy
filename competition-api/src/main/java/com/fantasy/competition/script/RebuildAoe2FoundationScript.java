package com.fantasy.competition.script;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Component
public class RebuildAoe2FoundationScript extends SqlImportScript {
    public RebuildAoe2FoundationScript(DataSource ds) { super(ds); }

    @Override public String getId() { return "rebuild-aoe2-foundation"; }

    @Override public String getName() { return "AOE2 foundation + clean World Cup"; }

    @Override public String getDescription() {
        return "Cleans up the broken 'World Cup 2022' (and the duplicate 'Mayans' nation), "
             + "ensures all 50 AOE2:DE civs (full roster minus Shu/Wei/Wu) exist as nations, each "
             + "with one NATIONAL team, then rebuilds a clean 'World Cup' competition -> '2022' "
             + "season (WORLD_CUP_32) with no teams/stages. Pick 32 teams in the admin and click "
             + "Generate. Safe to re-run.";
    }

    @Override public String getIcon() { return "\uD83C\uDF0D"; }

    @Override protected String getSql() {
        try {
            return new ClassPathResource("scripts/aoe2_rebuild_foundation.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Cannot read SQL", e);
        }
    }
}
