package com.fantasy.competition.format.model;

/** Best-placed teams across groups, e.g. the 4 best third-placed teams: {"position":3,"count":4}. */
public record BestPlaced(Integer position, Integer count) {}
