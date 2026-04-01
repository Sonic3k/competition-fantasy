package com.fantasy.competition.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "standings")
@Getter
@Setter
@NoArgsConstructor
public class Standing extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StandingType type = StandingType.RECORDED;

    @Column(name = "after_round")
    private Integer afterRound;

    private String groupName;

    @Column(nullable = false)
    private int played = 0;

    @Column(nullable = false)
    private int won = 0;

    @Column(nullable = false)
    private int drawn = 0;

    @Column(nullable = false)
    private int lost = 0;

    @Column(nullable = false)
    private int goalsFor = 0;

    @Column(nullable = false)
    private int goalsAgainst = 0;

    @Column(nullable = false)
    private int points = 0;

    public int getGoalDifference() {
        return goalsFor - goalsAgainst;
    }

    public enum StandingType {
        RECORDED, CALCULATED
    }
}
