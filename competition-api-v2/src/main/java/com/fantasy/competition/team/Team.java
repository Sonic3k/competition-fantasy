package com.fantasy.competition.team;

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
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "universe_id", nullable = false)
    private Long universeId;

    @Column(name = "nation_id")
    private Long nationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TeamType type = TeamType.CLUB;

    @Column(nullable = false, length = 64)
    private String key;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "short_name", length = 40)
    private String shortName;

    @Column(length = 4)
    private String code;

    @Column(name = "home_stadium_id")
    private Long homeStadiumId;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "founded_year")
    private Integer foundedYear;

    @Column(name = "dissolved_year")
    private Integer dissolvedYear;
}
