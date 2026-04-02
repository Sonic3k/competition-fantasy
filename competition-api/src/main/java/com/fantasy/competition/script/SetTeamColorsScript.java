package com.fantasy.competition.script;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Component
public class SetTeamColorsScript extends SqlImportScript {
    public SetTeamColorsScript(DataSource ds) { super(ds); }
    @Override public String getId() { return "set-team-colors"; }
    @Override public String getName() { return "Set Team & Nation Colors"; }
    @Override public String getDescription() { return "Sets home/away colors for all Othelose teams and codes+colors for Empires nations. Based on notebook jersey drawings."; }
    @Override public String getIcon() { return "🎨"; }
    @Override protected String getSql() {
        try { return new ClassPathResource("scripts/set_colors.sql").getContentAsString(StandardCharsets.UTF_8); }
        catch (Exception e) { throw new RuntimeException("Cannot read SQL", e); }
    }
}
