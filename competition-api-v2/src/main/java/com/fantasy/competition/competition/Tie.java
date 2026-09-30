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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ties")
@Getter
@Setter
@NoArgsConstructor
public class Tie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ko_round_id", nullable = false)
    private Long koRoundId;

    @Column(nullable = false)
    private Integer position;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "home_source", columnDefinition = "jsonb")
    private String homeSource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "away_source", columnDefinition = "jsonb")
    private String awaySource;

    @Column(name = "home_team_id")
    private Long homeTeamId;

    @Column(name = "away_team_id")
    private Long awayTeamId;

    @Column(name = "winner_team_id")
    private Long winnerTeamId;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Enums.TieResolution resolution;
}
