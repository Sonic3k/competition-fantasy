package com.fantasy.competition.common;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String what, Object id) {
        super(what + " not found: " + id);
    }
    public NotFoundException(String message) {
        super(message);
    }
}
