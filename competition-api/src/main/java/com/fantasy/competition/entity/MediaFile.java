package com.fantasy.competition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "media_files")
@Getter @Setter @NoArgsConstructor
public class MediaFile extends BaseEntity {

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String cdnUrl;

    private String contentType;
    private Long fileSize;
    private Integer width;
    private Integer height;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType = SourceType.UPLOAD;

    @Column(columnDefinition = "TEXT")
    private String promptUsed;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @ManyToMany
    @JoinTable(name = "media_file_tags",
        joinColumns = @JoinColumn(name = "media_file_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    public enum SourceType { UPLOAD, AI_GENERATED }
}
