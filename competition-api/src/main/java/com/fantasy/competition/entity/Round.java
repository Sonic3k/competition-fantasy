package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rounds")
@Getter @Setter @NoArgsConstructor
public class Round extends BaseEntity {

    @Column(nullable = false)
    private Integer roundNumber;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    private Stage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_group_id")
    private StageGroup stageGroup;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL)
    private List<Match> matches = new ArrayList<>();
}
