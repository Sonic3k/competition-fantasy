package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "matches")
@Getter @Setter @NoArgsConstructor
public class Match extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    // Nullable: knockout matches exist as TBD slots before the teams are known.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    private Integer homeScore;
    private Integer awayScore;
    private Integer leg;

    // Knockout outcome data
    private Integer homePenalties;
    private Integer awayPenalties;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "decided_by")
    private DecidedBy decidedBy;

    // Both legs of a two-leg knockout tie share the same tieId.
    @Column(name = "tie_id")
    private java.util.UUID tieId;

    // Optional match venue (neutral-venue tournaments). Independent of team home stadiums.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    private java.time.Instant matchDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.SCHEDULED;

    private String notes;

    // Bracket wiring: where this match's participants come from (knockout only).
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<MatchSlot> slots = new java.util.ArrayList<>();

    public enum MatchStatus { SCHEDULED, COMPLETED, CANCELLED }
    public enum DecidedBy { REGULAR, EXTRA_TIME, PENALTIES, AGGREGATE, WALKOVER }
}
