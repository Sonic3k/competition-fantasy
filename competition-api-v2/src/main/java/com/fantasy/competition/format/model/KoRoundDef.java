package com.fantasy.competition.format.model;

/** One knockout round: {"key":"QF","name":"Quarter-finals","legs":2}. legs defaults to the stage's legs; placement marks third-place style matches. */
public record KoRoundDef(String key, String name, Integer legs, Boolean placement) {
    public boolean isPlacement() { return placement != null && placement; }
}
