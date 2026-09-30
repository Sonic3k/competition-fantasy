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

@Entity
@Table(name = "stadiums")
@Getter
@Setter
@NoArgsConstructor
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "universe_id", nullable = false)
    private Long universeId;

    @Column(nullable = false, length = 64)
    private String key;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 120)
    private String city;

    private Integer capacity;

    @Column(name = "inspired_by", length = 160)
    private String inspiredBy;

    @Column(columnDefinition = "text")
    private String description;
}
