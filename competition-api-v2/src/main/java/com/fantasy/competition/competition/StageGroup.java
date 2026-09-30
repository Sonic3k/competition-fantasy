package com.fantasy.competition.competition;

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
@Table(name = "stage_groups")
@Getter
@Setter
@NoArgsConstructor
public class StageGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stage_id", nullable = false)
    private Long stageId;

    @Column(nullable = false)
    private Integer ordinal;

    @Column(nullable = false, length = 32)
    private String key;

    @Column(nullable = false, length = 120)
    private String name;
}
