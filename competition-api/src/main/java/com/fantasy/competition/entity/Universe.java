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
@Table(name = "universes")
@Getter
@Setter
@NoArgsConstructor
public class Universe extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    private String logoUrl;

    @JsonIgnore
    @OneToMany(mappedBy = "universe", cascade = CascadeType.ALL)
    private List<Nation> nations = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "universe", cascade = CascadeType.ALL)
    private List<Team> teams = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "universe", cascade = CascadeType.ALL)
    private List<Competition> competitions = new ArrayList<>();
}
