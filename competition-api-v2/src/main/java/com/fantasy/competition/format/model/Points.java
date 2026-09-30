package com.fantasy.competition.format.model;

public record Points(Integer win, Integer draw, Integer loss) {
    public static final Points STANDARD = new Points(3, 1, 0);

    public int winOrDefault() { return win == null ? 3 : win; }
    public int drawOrDefault() { return draw == null ? 1 : draw; }
    public int lossOrDefault() { return loss == null ? 0 : loss; }
}
