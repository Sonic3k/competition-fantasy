package com.fantasy.competition.competition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "season_id", nullable = false)
    private Long seasonId;

    @Column(name = "stage_id", nullable = false)
    private Long stageId;

    @Column(name = "round_id")
    private Long roundId;

    @Column(name = "tie_id")
    private Long tieId;

    @Column(nullable = false)
    private Integer leg = 1;

    @Column(name = "home_team_id")
    private Long homeTeamId;

    @Column(name = "away_team_id")
    private Long awayTeamId;

    @Column(name = "match_date")
    private LocalDate matchDate;

    @Column(name = "stadium_id")
    private Long stadiumId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Enums.MatchStatus status = Enums.MatchStatus.SCHEDULED;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "home_et")
    private Integer homeEt;

    @Column(name = "away_et")
    private Integer awayEt;

    @Column(name = "home_pens")
    private Integer homePens;

    @Column(name = "away_pens")
    private Integer awayPens;

    @Enumerated(EnumType.STRING)
    @Column(length = 4)
    private Enums.Walkover walkover;

    @Column(name = "winner_team_id")
    private Long winnerTeamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Enums.MatchSource source = Enums.MatchSource.MANUAL;

    @Column(name = "source_ref", length = 160)
    private String sourceRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Enums.Confidence confidence = Enums.Confidence.VERIFIED;

    @Column(columnDefinition = "text")
    private String notes;

    /** True when the match has a 90-minute score that counts for a table. */
    public boolean hasScore() {
        return status == Enums.MatchStatus.PLAYED && homeScore != null && awayScore != null;
    }

    /** Goals after extra time when played, else the 90-minute score. */
    public int homeGoalsFinal() { return homeEt != null ? homeEt : (homeScore == null ? 0 : homeScore); }
    public int awayGoalsFinal() { return awayEt != null ? awayEt : (awayScore == null ? 0 : awayScore); }
}
