package com.fantasy.competition.script;

import org.springframework.stereotype.Component;

@Component
public class TestScript implements ImportScript {
    @Override public String getId() { return "test-ping"; }
    @Override public String getName() { return "Test Connection"; }
    @Override public String getDescription() { return "Simple test - runs SELECT 1 to verify database connection."; }
    @Override public String getIcon() { return "🏓"; }
    @Override public void execute() { /* no-op, just tests the flow */ }
}
