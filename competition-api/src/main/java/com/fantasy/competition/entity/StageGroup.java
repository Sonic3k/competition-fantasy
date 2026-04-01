package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stage_groups")
@Getter @Setter @NoArgsConstructor
public class StageGroup extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @ManyToMany
    @JoinTable(name = "stage_group_teams",
        joinColumns = @JoinColumn(name = "stage_group_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id"))
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "stageGroup", cascade = CascadeType.ALL)
    private List<Standing> standings = new ArrayList<>();
}
