package com.fantasy.competition.controller;

import com.fantasy.competition.dto.MediaFileDto;
import com.fantasy.competition.entity.MediaFile;
import com.fantasy.competition.repository.*;
import com.fantasy.competition.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UploadController {

    private final StorageService storage;
    private final MediaFileRepository mediaRepo;
    private final UniverseRepository universeRepo;
    private final TeamRepository teamRepo;
    private final NationRepository nationRepo;

    @PostMapping("/universe/{id}")
    public ResponseEntity<?> uploadUniverseAvatar(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return universeRepo.findById(id).map(u -> {
            try {
                MediaFile mf = uploadAndSave(file, "competition-fantasy/universes/" + id + "/avatar", MediaFile.SourceType.UPLOAD);
                u.setAvatarUrl(mf.getCdnUrl());
                u.setAvatarMedia(mf);
                universeRepo.save(u);
                return ResponseEntity.ok(MediaFileDto.from(mf));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/team/{id}")
    public ResponseEntity<?> uploadTeamLogo(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return teamRepo.findById(id).map(t -> {
            try {
                MediaFile mf = uploadAndSave(file, "competition-fantasy/teams/" + id + "/logo", MediaFile.SourceType.UPLOAD);
                t.setLogoUrl(mf.getCdnUrl());
                t.setLogoMedia(mf);
                teamRepo.save(t);
                return ResponseEntity.ok(MediaFileDto.from(mf));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/nation/{id}")
    public ResponseEntity<?> uploadNationFlag(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return nationRepo.findById(id).map(n -> {
            try {
                MediaFile mf = uploadAndSave(file, "competition-fantasy/nations/" + id + "/flag", MediaFile.SourceType.UPLOAD);
                n.setFlagUrl(mf.getCdnUrl());
                n.setFlagMedia(mf);
                nationRepo.save(n);
                return ResponseEntity.ok(MediaFileDto.from(mf));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    private MediaFile uploadAndSave(MultipartFile file, String basePath, MediaFile.SourceType sourceType) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "png";
        String path = basePath + "." + ext;

        String cdnUrl = storage.upload(path, file);

        MediaFile mf = new MediaFile();
        mf.setFilename(filename);
        mf.setCdnUrl(cdnUrl);
        mf.setContentType(file.getContentType());
        mf.setFileSize(file.getSize());
        mf.setSourceType(sourceType);
        return mediaRepo.save(mf);
    }
}
