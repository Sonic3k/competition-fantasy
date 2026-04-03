package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "collections")
@Getter @Setter @NoArgsConstructor
public class Collection extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CollectionType type = CollectionType.GENERAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Collection parent;

    @OneToMany(mappedBy = "parent")
    private List<Collection> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "universe_id")
    private Universe universe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_media_id")
    private MediaFile coverMedia;

    @OneToMany(mappedBy = "collection")
    private List<MediaFile> mediaFiles = new ArrayList<>();

    public enum CollectionType {
        LOGO, JERSEY, STADIUM, MATCH_POSTER, FLAG, ART, GENERAL
    }
}
