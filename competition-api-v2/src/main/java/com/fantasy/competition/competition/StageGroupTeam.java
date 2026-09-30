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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "stage_group_teams")
@Getter
@Setter
@NoArgsConstructor
public class StageGroupTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stage_group_id", nullable = false)
    private Long stageGroupId;

    @Column(name = "team_id")
    private Long teamId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "entry_source", columnDefinition = "jsonb")
    private String entrySource;

    @Column(nullable = false)
    private Integer position = 0;
}
