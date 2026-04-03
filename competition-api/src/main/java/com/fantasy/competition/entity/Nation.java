package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nations")
@Getter @Setter @NoArgsConstructor
public class Nation extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String code;
    private String flagUrl;
    private String description;
    private String primaryColor;
    private String textColor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_media_id")
    private MediaFile flagMedia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "universe_id", nullable = false)
    private Universe universe;

    @OneToMany(mappedBy = "nation", cascade = CascadeType.ALL)
    private List<Team> teams = new ArrayList<>();
}
