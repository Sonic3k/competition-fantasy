package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stages")
@Getter @Setter @NoArgsConstructor
public class Stage extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StageType type;

    @Column(nullable = false)
    private Integer orderNumber;

    private Integer legs = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL)
    private List<StageGroup> groups = new ArrayList<>();

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL)
    private List<Round> rounds = new ArrayList<>();

    public enum StageType { GROUP, KNOCKOUT }
}
