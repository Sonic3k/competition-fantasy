package com.fantasy.competition.common;

import java.util.List;

public class BadRequestException extends RuntimeException {
    private final List<String> details;

    public BadRequestException(String message) {
        this(message, List.of());
    }

    public BadRequestException(String message, List<String> details) {
        super(message);
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public List<String> getDetails() { return details; }
}
