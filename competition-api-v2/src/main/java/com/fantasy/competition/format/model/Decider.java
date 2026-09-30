package com.fantasy.competition.format.model;

/** Ways a knockout tie can be decided when level, in the order the format applies them. */
public enum Decider {
    AGGREGATE,
    AWAY_GOALS,
    EXTRA_TIME,
    GOLDEN_GOAL,
    SILVER_GOAL,
    PENALTIES,
    REPLAY,
    LOTS
}
