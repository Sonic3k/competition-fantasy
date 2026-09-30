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

@Entity
@Table(name = "nations")
@Getter
@Setter
@NoArgsConstructor
public class Nation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "universe_id", nullable = false)
    private Long universeId;

    @Column(nullable = false, length = 64)
    private String key;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 3)
    private String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String colors;

    @Column(columnDefinition = "text")
    private String description;
}
