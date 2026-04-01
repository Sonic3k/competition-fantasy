package com.fantasy.competition.script;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

public abstract class SqlImportScript implements ImportScript {

    private final DataSource dataSource;

    protected SqlImportScript(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected abstract String getSql();

    @Override
    public void execute() {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            stmt.execute(getSql());
            conn.commit();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
