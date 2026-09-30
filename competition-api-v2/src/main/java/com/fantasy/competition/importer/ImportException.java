package com.fantasy.competition.importer;

import java.util.List;

public class ImportException extends RuntimeException {
    private final List<String> errors;

    public ImportException(String message, List<String> errors) {
        super(message);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public List<String> getErrors() { return errors; }
}
