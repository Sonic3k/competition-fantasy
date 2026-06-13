package com.fantasy.competition.dto;

/** Summary returned by the knockout advance endpoint (never silent). */
public record AdvanceResult(int slotsFilled, int tiesResolved, boolean finalDecided, String message) {}
