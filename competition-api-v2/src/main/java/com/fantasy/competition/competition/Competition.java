package com.fantasy.competition.competition;

import com.fantasy.competition.team.TeamType;
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

@Entity
@Table(name = "competitions")
@Getter
@Setter
@NoArgsConstructor
public class Competition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "universe_id", nullable = false)
    private Long universeId;

    @Column(nullable = false, length = 64)
    private String key;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 32)
    private String sport = "FOOTBALL";

    @Enumerated(EnumType.STRING)
    @Column(name = "team_level", nullable = false, length = 16)
    private TeamType teamLevel = TeamType.CLUB;

    private Integer tier;

    @Column(columnDefinition = "text")
    private String description;
}
