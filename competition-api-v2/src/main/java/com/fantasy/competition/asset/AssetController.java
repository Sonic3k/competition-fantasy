package com.fantasy.competition.asset;

import com.fantasy.competition.asset.AssetService.AssetDto;
import com.fantasy.competition.asset.AssetService.SlotDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class AssetController {

    public record AttachRequest(String ownerType, Long ownerId, String slot, Long assetId) {}

    private final AssetService service;
    private final StorageService storage;

    public AssetController(AssetService service, StorageService storage) {
        this.service = service;
        this.storage = storage;
    }

    @GetMapping("/api/assets")
    public AssetService.AssetPage list(@RequestParam(required = false) String collection,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "50") int size) {
        return service.list(collection, page, size);
    }

    @GetMapping("/api/assets/{id}")
    public AssetDto get(@PathVariable Long id) { return service.get(id); }

    @GetMapping("/api/assets/slots")
    public List<SlotDto> slots(@RequestParam String ownerType, @RequestParam Long ownerId) { return service.slotsOf(ownerType, ownerId); }

    @GetMapping("/api/admin/assets/status")
    public Map<String, Object> status() {
        return Map.of("configured", storage.configured(), "prefix", storage.prefix(), "ownerTypes", AssetService.OWNER_TYPES, "slots", AssetService.SLOTS);
    }

    @PostMapping(value = "/api/admin/assets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AssetDto upload(@RequestPart("file") MultipartFile file,
                           @RequestParam(required = false) String title,
                           @RequestParam(required = false) String collection,
                           @RequestParam(required = false) String tags) throws IOException {
        List<String> tagList = tags == null || tags.isBlank() ? List.of() : Arrays.stream(tags.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return service.upload(file.getBytes(), file.getOriginalFilename(), file.getContentType(), title, collection, tagList);
    }

    @PostMapping("/api/admin/assets/attach")
    public SlotDto attach(@RequestBody AttachRequest req) {
        return service.attach(req.ownerType(), req.ownerId(), req.slot(), req.assetId());
    }

    @DeleteMapping("/api/admin/asset-slots/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detach(@PathVariable Long id) { service.detach(id); }

    @DeleteMapping("/api/admin/assets/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean fromStorage) { service.delete(id, fromStorage); }
}
