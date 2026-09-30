package com.fantasy.competition.format.model;

/** Points carried from an earlier stage into this one: {"from":"REG","points":"HALF"} (FULL | HALF | NONE). */
public record CarryOver(String from, String points) {}
