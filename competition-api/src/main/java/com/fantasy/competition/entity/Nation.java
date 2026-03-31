package com.fantasy.competition.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nations")
@Getter
@Setter
@NoArgsConstructor
public class Nation extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String flagUrl;

    private String description;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "universe_id", nullable = false)
    private Universe universe;

    @JsonIgnore
    @OneToMany(mappedBy = "nation", cascade = CascadeType.ALL)
    private List<Team> teams = new ArrayList<>();
}
