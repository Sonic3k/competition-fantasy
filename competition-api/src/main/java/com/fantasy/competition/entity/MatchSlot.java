package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One side (HOME/AWAY) of a knockout match, describing where that participant
 * comes from. The advance engine reads these to fill in TBD matches:
 *   - GROUP_POSITION: position {sourcePosition} of group {sourceGroup}
 *   - MATCH_WINNER / MATCH_LOSER: winner/loser of {sourceMatch}
 *   - BEST_THIRD: the {bestThirdRank}-th best third-placed team
 */
@Entity
@Table(name = "match_slot")
@Getter @Setter @NoArgsConstructor
public class MatchSlot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side side;

    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_kind", nullable = false)
    private SourceKind sourceKind;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_group_id")
    private StageGroup sourceGroup;

    @Column(name = "source_position")
    private Integer sourcePosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_match_id")
    private Match sourceMatch;

    @Column(name = "best_third_rank")
    private Integer bestThirdRank;

    public enum Side { HOME, AWAY }
    public enum SourceKind { GROUP_POSITION, MATCH_WINNER, MATCH_LOSER, BEST_THIRD }
}
