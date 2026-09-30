package com.fantasy.competition.format.model;

/** Ordered tie-break criteria applied after points. Unknown strings in a format are rejected by FormatValidator. */
public enum Tiebreaker {
    GD,             // overall goal difference
    GF,             // overall goals scored
    GA,             // fewer overall goals conceded
    GOAL_AVERAGE,   // goals for / goals against (pre-1970s)
    WINS,           // number of wins
    AWAY_GOALS,     // overall away goals scored
    H2H,            // head-to-head mini-table among tied teams: points, goal difference, goals scored (applied recursively)
    H2H_AWAY_GOALS, // away goals in the head-to-head mini-table
    FAIR_PLAY,      // needs disciplinary data; treated as "no separation" when absent
    LOTS,           // drawing of lots; cannot be computed, flags the tie
    PLAYOFF,        // play-off match decides; cannot be computed, flags the tie
    RANKING         // external ranking (e.g. seeding); cannot be computed, flags the tie
}
