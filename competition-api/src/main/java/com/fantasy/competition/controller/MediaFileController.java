package com.fantasy.competition.controller;

import com.fantasy.competition.dto.MediaFileDto;
import com.fantasy.competition.entity.MediaFile;
import com.fantasy.competition.entity.Tag;
import com.fantasy.competition.repository.CollectionRepository;
import com.fantasy.competition.repository.MediaFileRepository;
import com.fantasy.competition.repository.TagRepository;
import com.fantasy.competition.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@CrossOrigin("*")
@RequiredArgsConstructor
public class MediaFileController {

    private final MediaFileRepository repo;
    private final CollectionRepository collectionRepo;
    private final TagRepository tagRepo;
    private final StorageService storage;

    @GetMapping
    public List<MediaFileDto> list(@RequestParam(required = false) UUID collectionId,
                                   @RequestParam(required = false) UUID tagId,
                                   @RequestParam(required = false) String sourceType) {
        if (tagId != null) return repo.findByTagId(tagId).stream().map(MediaFileDto::from).toList();
        if (collectionId != null && sourceType != null)
            return repo.findByCollectionIdAndSourceType(collectionId, MediaFile.SourceType.valueOf(sourceType)).stream().map(MediaFileDto::from).toList();
        if (collectionId != null) return repo.findByCollectionIdOrderByCreatedAtDesc(collectionId).stream().map(MediaFileDto::from).toList();
        if (sourceType != null) return repo.findBySourceTypeOrderByCreatedAtDesc(MediaFile.SourceType.valueOf(sourceType)).stream().map(MediaFileDto::from).toList();
        return repo.findAll().stream().map(MediaFileDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaFileDto> get(@PathVariable UUID id) {
        return repo.findById(id).map(e -> ResponseEntity.ok(MediaFileDto.from(e))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public MediaFileDto upload(@RequestParam("file") MultipartFile file,
                               @RequestParam(required = false) UUID collectionId,
                               @RequestParam(required = false) String tags) {
        try {
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1) : "png";
            String path = "competition-fantasy/media/" + UUID.randomUUID() + "." + ext;

            String cdnUrl = storage.upload(path, file);

            MediaFile mf = new MediaFile();
            mf.setFilename(filename);
            mf.setCdnUrl(cdnUrl);
            mf.setContentType(file.getContentType());
            mf.setFileSize(file.getSize());
            mf.setSourceType(MediaFile.SourceType.UPLOAD);

            if (collectionId != null) collectionRepo.findById(collectionId).ifPresent(mf::setCollection);

            if (tags != null && !tags.isBlank()) {
                for (String tagName : tags.split(",")) {
                    String t = tagName.trim();
                    if (t.isEmpty()) continue;
                    Tag tag = tagRepo.findByName(t).orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(t);
                        return tagRepo.save(newTag);
                    });
                    mf.getTags().add(tag);
                }
            }

            return MediaFileDto.from(repo.save(mf));
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}/tags")
    public ResponseEntity<MediaFileDto> updateTags(@PathVariable UUID id, @RequestBody List<String> tagNames) {
        return repo.findById(id).map(mf -> {
            mf.getTags().clear();
            for (String name : tagNames) {
                Tag tag = tagRepo.findByName(name).orElseGet(() -> {
                    Tag t = new Tag();
                    t.setName(name);
                    return tagRepo.save(t);
                });
                mf.getTags().add(tag);
            }
            return ResponseEntity.ok(MediaFileDto.from(repo.save(mf)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/collection")
    public ResponseEntity<MediaFileDto> moveToCollection(@PathVariable UUID id, @RequestParam UUID collectionId) {
        return repo.findById(id).map(mf -> {
            collectionRepo.findById(collectionId).ifPresent(mf::setCollection);
            return ResponseEntity.ok(MediaFileDto.from(repo.save(mf)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
