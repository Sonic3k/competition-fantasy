package com.fantasy.competition.standings;

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

import java.time.Instant;

/** One table of a group: CALCULATED from matches, or RECORDED from the notebook. checkpointRound 0 = final/current. */
@Entity
@Table(name = "standings")
@Getter
@Setter
@NoArgsConstructor
public class Standing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stage_group_id", nullable = false)
    private Long stageGroupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StandingType type;

    @Column(name = "checkpoint_round", nullable = false)
    private Integer checkpointRound = 0;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt = Instant.now();
}
