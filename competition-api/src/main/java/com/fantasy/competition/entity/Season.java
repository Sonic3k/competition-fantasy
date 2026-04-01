package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seasons")
@Getter @Setter @NoArgsConstructor
public class Season extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeasonStatus status = SeasonStatus.PLANNED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    private List<Stage> stages = new ArrayList<>();

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    private List<Round> rounds = new ArrayList<>();

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    private List<Standing> standings = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "season_teams",
        joinColumns = @JoinColumn(name = "season_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id"))
    private List<Team> teams = new ArrayList<>();

    public enum SeasonStatus { PLANNED, IN_PROGRESS, COMPLETED }
}
