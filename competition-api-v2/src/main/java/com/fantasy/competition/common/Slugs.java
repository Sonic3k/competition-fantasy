package com.fantasy.competition.common;

import java.text.Normalizer;
import java.util.Locale;

public final class Slugs {
    private Slugs() {}

    public static String of(String input) {
        if (input == null) return null;
        String n = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        n = n.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        return n.isEmpty() ? "x" : n;
    }
}
