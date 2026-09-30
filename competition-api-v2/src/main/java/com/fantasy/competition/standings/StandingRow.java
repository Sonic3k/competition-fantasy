package com.fantasy.competition.standings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "standing_rows")
@Getter
@Setter
@NoArgsConstructor
public class StandingRow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "standing_id", nullable = false)
    private Long standingId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private Integer played = 0;

    @Column(nullable = false)
    private Integer won = 0;

    @Column(nullable = false)
    private Integer drawn = 0;

    @Column(nullable = false)
    private Integer lost = 0;

    @Column(name = "goals_for", nullable = false)
    private Integer goalsFor = 0;

    @Column(name = "goals_against", nullable = false)
    private Integer goalsAgainst = 0;

    @Column(name = "goal_diff", nullable = false)
    private Integer goalDiff = 0;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(nullable = false)
    private Integer adjustment = 0;

    @Column(length = 32)
    private String zone;

    @Column(name = "tie_note", length = 160)
    private String tieNote;
}
