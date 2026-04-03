package com.fantasy.competition.controller;

import com.fantasy.competition.entity.*;
import com.fantasy.competition.repository.*;
import com.fantasy.competition.service.GrokService;
import com.fantasy.competition.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AIController {

    private final GrokService grok;
    private final StorageService storage;
    private final PromptTemplateRepository templateRepo;
    private final GenerationJobRepository jobRepo;
    private final GenerationResultRepository resultRepo;
    private final MediaFileRepository mediaRepo;
    private final CollectionRepository collectionRepo;

    // ===== Templates =====
    @GetMapping("/templates")
    public List<Map<String, Object>> listTemplates(@RequestParam(required = false) String category) {
        var list = category != null ? templateRepo.findByCategoryOrderByName(category) : templateRepo.findAllByOrderByName();
        return list.stream().map(this::mapTemplate).toList();
    }

    @PostMapping("/templates")
    public Map<String, Object> createTemplate(@RequestBody Map<String, String> body) {
        PromptTemplate t = new PromptTemplate();
        t.setName(body.get("name"));
        t.setCategory(body.getOrDefault("category", "GENERAL"));
        t.setTemplateText(body.get("templateText"));
        if (body.get("defaultCollectionId") != null)
            collectionRepo.findById(UUID.fromString(body.get("defaultCollectionId"))).ifPresent(t::setDefaultCollection);
        return mapTemplate(templateRepo.save(t));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        templateRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Generate =====
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody Map<String, Object> body) {
        String prompt = (String) body.get("prompt");
        UUID templateId = body.get("templateId") != null ? UUID.fromString((String) body.get("templateId")) : null;
        UUID collectionId = body.get("collectionId") != null ? UUID.fromString((String) body.get("collectionId")) : null;
        int n = body.get("n") != null ? ((Number) body.get("n")).intValue() : 1;

        GenerationJob job = new GenerationJob();

        if (templateId != null) {
            PromptTemplate tpl = templateRepo.findById(templateId).orElse(null);
            if (tpl != null) {
                job.setPromptTemplate(tpl);
                // Replace variables if provided
                String resolved = tpl.getTemplateText();
                @SuppressWarnings("unchecked")
                Map<String, String> vars = (Map<String, String>) body.get("variables");
                if (vars != null) {
                    for (var e : vars.entrySet()) {
                        resolved = resolved.replace("{" + e.getKey() + "}", e.getValue());
                    }
                }
                prompt = resolved;
                if (collectionId == null && tpl.getDefaultCollection() != null)
                    collectionId = tpl.getDefaultCollection().getId();
            }
        }

        job.setResolvedPrompt(prompt);
        job.setStatus("PROCESSING");
        if (collectionId != null) collectionRepo.findById(collectionId).ifPresent(job::setCollection);
        job = jobRepo.save(job);

        try {
            List<String> urls = grok.generate(prompt, n);
            for (String url : urls) {
                GenerationResult r = new GenerationResult();
                r.setJob(job);
                r.setImageUrl(url);
                job.getResults().add(r);
            }
            job.setResultCount(urls.size());
            job.setStatus("COMPLETED");
        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
        }

        job = jobRepo.save(job);
        return mapJob(job);
    }

    // ===== Jobs =====
    @GetMapping("/jobs")
    public List<Map<String, Object>> listJobs() {
        return jobRepo.findAllByOrderByCreatedAtDesc().stream().map(this::mapJob).toList();
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<Map<String, Object>> getJob(@PathVariable UUID id) {
        return jobRepo.findById(id).map(j -> ResponseEntity.ok(mapJob(j))).orElse(ResponseEntity.notFound().build());
    }

    // ===== Save result to MediaFile =====
    @PostMapping("/results/{id}/save")
    public ResponseEntity<Map<String, Object>> saveResult(@PathVariable UUID id,
                                                           @RequestParam(required = false) UUID collectionId) {
        return resultRepo.findById(id).map(r -> {
            try {
                // Download image from Grok URL
                byte[] imageBytes;
                String contentType = "image/png";
                if (r.getImageUrl().startsWith("data:")) {
                    String b64 = r.getImageUrl().substring(r.getImageUrl().indexOf(",") + 1);
                    imageBytes = Base64.getDecoder().decode(b64);
                } else {
                    try (InputStream is = URI.create(r.getImageUrl()).toURL().openStream()) {
                        imageBytes = is.readAllBytes();
                    }
                }

                // Upload to B2
                String path = "competition-fantasy/ai/" + UUID.randomUUID() + ".png";
                String cdnUrl = storage.uploadBytes(path, imageBytes, contentType);

                // Create MediaFile
                MediaFile mf = new MediaFile();
                mf.setFilename("ai-generated-" + r.getId() + ".png");
                mf.setCdnUrl(cdnUrl);
                mf.setContentType(contentType);
                mf.setFileSize((long) imageBytes.length);
                mf.setSourceType(MediaFile.SourceType.AI_GENERATED);
                mf.setPromptUsed(r.getJob().getResolvedPrompt());

                UUID cid = collectionId != null ? collectionId : (r.getJob().getCollection() != null ? r.getJob().getCollection().getId() : null);
                if (cid != null) collectionRepo.findById(cid).ifPresent(mf::setCollection);

                mf = mediaRepo.save(mf);
                r.setMediaFile(mf);
                r.setSaved(true);
                resultRepo.save(r);

                return ResponseEntity.ok(Map.<String, Object>of("mediaFileId", mf.getId(), "cdnUrl", cdnUrl));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(Map.<String, Object>of("error", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("grokConfigured", grok.isConfigured());
    }

    // ===== Mappers =====
    private Map<String, Object> mapTemplate(PromptTemplate t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId()); m.put("name", t.getName());
        m.put("category", t.getCategory()); m.put("templateText", t.getTemplateText());
        m.put("defaultCollectionId", t.getDefaultCollection() != null ? t.getDefaultCollection().getId() : null);
        return m;
    }

    private Map<String, Object> mapJob(GenerationJob j) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", j.getId()); m.put("prompt", j.getResolvedPrompt());
        m.put("status", j.getStatus()); m.put("resultCount", j.getResultCount());
        m.put("error", j.getErrorMessage()); m.put("createdAt", j.getCreatedAt());
        m.put("results", j.getResults().stream().map(r -> {
            Map<String, Object> rm = new LinkedHashMap<>();
            rm.put("id", r.getId()); rm.put("imageUrl", r.getImageUrl());
            rm.put("saved", r.getSaved());
            rm.put("mediaFileId", r.getMediaFile() != null ? r.getMediaFile().getId() : null);
            rm.put("cdnUrl", r.getMediaFile() != null ? r.getMediaFile().getCdnUrl() : null);
            return rm;
        }).toList());
        return m;
    }
}
