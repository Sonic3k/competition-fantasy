package com.fantasy.competition.competition;

/** Small enums of the competition package, kept together to keep the file count sane. */
public final class Enums {
    private Enums() {}

    public enum SeasonStatus { PLANNED, ONGOING, COMPLETED }
    public enum MatchStatus { SCHEDULED, PLAYED, UNKNOWN, CANCELLED }
    public enum MatchSource { NOTEBOOK, MANUAL, IMPORT, SIMULATED }
    public enum Confidence { DRAFT, VERIFIED }
    public enum Walkover { HOME, AWAY }
    public enum TieResolution { AGGREGATE, AWAY_GOALS, EXTRA_TIME, PENALTIES, REPLAY, WALKOVER, LOTS }
    public enum HonourKind { CHAMPION, RUNNER_UP, THIRD, FOURTH, PROMOTED, RELEGATED }
}
