package com.fantasy.competition.repository;

import com.fantasy.competition.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {
    List<MediaFile> findByCollectionIdOrderByCreatedAtDesc(UUID collectionId);
    List<MediaFile> findBySourceTypeOrderByCreatedAtDesc(MediaFile.SourceType sourceType);

    @Query("SELECT m FROM MediaFile m JOIN m.tags t WHERE t.id = :tagId ORDER BY m.createdAt DESC")
    List<MediaFile> findByTagId(UUID tagId);

    @Query("SELECT m FROM MediaFile m WHERE m.collection.id = :collectionId AND m.sourceType = :sourceType ORDER BY m.createdAt DESC")
    List<MediaFile> findByCollectionIdAndSourceType(UUID collectionId, MediaFile.SourceType sourceType);
}
