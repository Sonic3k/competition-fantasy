package com.fantasy.competition.team;

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

/** A team's look and details in one calendar year of the universe. */
@Entity
@Table(name = "team_profiles")
@Getter
@Setter
@NoArgsConstructor
public class TeamProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @Column(length = 120)
    private String sponsor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String colors;

    @Column(columnDefinition = "text")
    private String notes;
}
