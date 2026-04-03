package com.fantasy.competition.entity;

import com.fantasy.competition.entity.MediaFile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
@Getter @Setter @NoArgsConstructor
public class Team extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String shortName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamType type;

    private String primaryColor;
    private String secondaryColor;
    private String homeBg;
    private String homeText;
    private String awayBg;
    private String awayText;
    private String logoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logo_media_id")
    private MediaFile logoMedia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "universe_id", nullable = false)
    private Universe universe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nation_id")
    private Nation nation;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Jersey> jerseys = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    public enum TeamType { CLUB, NATIONAL }
}
