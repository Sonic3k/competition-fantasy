package com.fantasy.competition.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class Match extends BaseEntity {

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "season", "matches"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "universe", "nation", "stadium", "jerseys"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "universe", "nation", "stadium", "jerseys"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    private Integer homeScore;
    private Integer awayScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.SCHEDULED;

    private String notes;

    public enum MatchStatus {
        SCHEDULED, COMPLETED, CANCELLED
    }
}
