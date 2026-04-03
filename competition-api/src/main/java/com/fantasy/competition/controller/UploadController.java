package com.fantasy.competition.controller;

import com.fantasy.competition.repository.NationRepository;
import com.fantasy.competition.repository.TeamRepository;
import com.fantasy.competition.repository.UniverseRepository;
import com.fantasy.competition.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UploadController {

    private final StorageService storage;
    private final UniverseRepository universeRepo;
    private final TeamRepository teamRepo;
    private final NationRepository nationRepo;

    @PostMapping("/universe/{id}")
    public ResponseEntity<?> uploadUniverseAvatar(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return universeRepo.findById(id).map(u -> {
            try {
                String ext = getExt(file.getOriginalFilename());
                String url = storage.upload("competition-fantasy/universes/" + id + "/avatar." + ext, file);
                u.setAvatarUrl(url);
                universeRepo.save(u);
                return ResponseEntity.ok(Map.of("url", url));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/team/{id}")
    public ResponseEntity<?> uploadTeamLogo(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return teamRepo.findById(id).map(t -> {
            try {
                String ext = getExt(file.getOriginalFilename());
                String url = storage.upload("competition-fantasy/teams/" + id + "/logo." + ext, file);
                t.setLogoUrl(url);
                teamRepo.save(t);
                return ResponseEntity.ok(Map.of("url", url));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/nation/{id}")
    public ResponseEntity<?> uploadNationFlag(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return nationRepo.findById(id).map(n -> {
            try {
                String ext = getExt(file.getOriginalFilename());
                String url = storage.upload("competition-fantasy/nations/" + id + "/flag." + ext, file);
                n.setFlagUrl(url);
                nationRepo.save(n);
                return ResponseEntity.ok(Map.of("url", url));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    private String getExt(String filename) {
        if (filename == null) return "png";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "png";
    }
}
