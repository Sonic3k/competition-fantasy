package com.fantasy.competition.asset;

import com.fantasy.competition.common.BadRequestException;
import com.fantasy.competition.common.Json;
import com.fantasy.competition.common.NotFoundException;
import com.fantasy.competition.common.Slugs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AssetService {

    public static final Set<String> OWNER_TYPES = Set.of("UNIVERSE", "NATION", "TEAM", "KIT", "STADIUM", "COMPETITION", "SEASON", "WEB");
    public static final Set<String> SLOTS = Set.of("LOGO", "BANNER", "FLAG", "EMBLEM", "AVATAR", "IMAGE", "POSTER", "BACKGROUND", "KIT_HOME", "KIT_AWAY", "KIT_THIRD");
    private static final Set<String> IMAGE_TYPES = Set.of("image/png", "image/jpeg", "image/webp", "image/gif", "image/svg+xml");

    public record AssetDto(Long id, String storageKey, String url, String contentType, Long sizeBytes, Integer width, Integer height,
                           String title, String collection, List<String> tags) {}

    public record SlotDto(Long id, String ownerType, Long ownerId, String slot, Long assetId, String url, boolean current) {}

    private final AssetRepository assets;
    private final AssetSlotRepository slots;
    private final StorageService storage;
    private final Json json;

    public AssetService(AssetRepository assets, AssetSlotRepository slots, StorageService storage, Json json) {
        this.assets = assets;
        this.slots = slots;
        this.storage = storage;
        this.json = json;
    }

    @Transactional
    public AssetDto upload(byte[] bytes, String originalName, String contentType, String title, String collection, List<String> tags) {
        if (bytes == null || bytes.length == 0) throw new BadRequestException("file is empty");
        String type = contentType == null ? "application/octet-stream" : contentType.toLowerCase(Locale.ROOT);
        if (!IMAGE_TYPES.contains(type)) throw new BadRequestException("Only image uploads are accepted (png, jpeg, webp, gif, svg)");
        String ext = extension(originalName, type);
        String base = Slugs.of(stripExtension(originalName == null ? "asset" : originalName));
        String folder = collection == null || collection.isBlank() ? "uploads" : Slugs.of(collection);
        String key = storage.prefix() + "/" + folder + "/" + Year.now().getValue() + "/" + base + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;

        Integer width = null, height = null;
        if (!type.equals("image/svg+xml")) {
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                if (img != null) { width = img.getWidth(); height = img.getHeight(); }
            } catch (Exception ignored) {
                // dimensions are optional
            }
        }
        storage.put(key, bytes, type);

        Asset a = new Asset();
        a.setStorageKey(key);
        a.setUrl(storage.publicUrl(key));
        a.setContentType(type);
        a.setSizeBytes((long) bytes.length);
        a.setWidth(width);
        a.setHeight(height);
        a.setTitle(title == null || title.isBlank() ? stripExtension(originalName) : title);
        a.setCollection(collection == null || collection.isBlank() ? null : collection);
        a.setTags(tags == null || tags.isEmpty() ? null : json.write(tags));
        return dto(assets.save(a));
    }

    public record AssetPage(List<AssetDto> items, long total, int page, int size) {}

    @Transactional(readOnly = true)
    public AssetPage list(String collection, int page, int size) {
        PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        Page<Asset> p = collection == null || collection.isBlank() ? assets.findAllByOrderByCreatedAtDesc(pr) : assets.findByCollectionOrderByCreatedAtDesc(collection, pr);
        return new AssetPage(p.getContent().stream().map(this::dto).toList(), p.getTotalElements(), p.getNumber(), p.getSize());
    }

    @Transactional(readOnly = true)
    public AssetDto get(Long id) {
        return dto(assets.findById(id).orElseThrow(() -> new NotFoundException("Asset", id)));
    }

    @Transactional
    public void delete(Long id, boolean deleteFromStorage) {
        Asset a = assets.findById(id).orElseThrow(() -> new NotFoundException("Asset", id));
        if (deleteFromStorage && storage.configured()) {
            try { storage.delete(a.getStorageKey()); } catch (RuntimeException ignored) { /* keep the row deletion */ }
        }
        assets.delete(a);
    }

    /** Attaches an asset to an owner's slot; the previous current image stays in the history. */
    @Transactional
    public SlotDto attach(String ownerType, Long ownerId, String slot, Long assetId) {
        String ot = ownerType == null ? "" : ownerType.toUpperCase(Locale.ROOT);
        String sl = slot == null ? "" : slot.toUpperCase(Locale.ROOT);
        if (!OWNER_TYPES.contains(ot)) throw new BadRequestException("Unknown ownerType " + ownerType + "; allowed: " + OWNER_TYPES);
        if (!SLOTS.contains(sl)) throw new BadRequestException("Unknown slot " + slot + "; allowed: " + SLOTS);
        if (ownerId == null) throw new BadRequestException("ownerId is required");
        Asset a = assets.findById(assetId).orElseThrow(() -> new NotFoundException("Asset", assetId));
        for (AssetSlot old : slots.findByOwnerTypeAndOwnerIdAndSlotAndCurrentTrue(ot, ownerId, sl)) {
            old.setCurrent(false);
            slots.save(old);
        }
        AssetSlot s = new AssetSlot();
        s.setOwnerType(ot);
        s.setOwnerId(ownerId);
        s.setSlot(sl);
        s.setAssetId(a.getId());
        s.setCurrent(true);
        s = slots.save(s);
        return new SlotDto(s.getId(), ot, ownerId, sl, a.getId(), a.getUrl(), true);
    }

    @Transactional
    public void detach(Long slotId) {
        AssetSlot s = slots.findById(slotId).orElseThrow(() -> new NotFoundException("Asset slot", slotId));
        slots.delete(s);
    }

    @Transactional(readOnly = true)
    public List<SlotDto> slotsOf(String ownerType, Long ownerId) {
        String ot = ownerType == null ? "" : ownerType.toUpperCase(Locale.ROOT);
        List<AssetSlot> list = slots.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(ot, ownerId);
        if (list.isEmpty()) return List.of();
        var urls = assets.findByIdIn(list.stream().map(AssetSlot::getAssetId).toList()).stream()
                .collect(java.util.stream.Collectors.toMap(Asset::getId, Asset::getUrl));
        return list.stream().map(s -> new SlotDto(s.getId(), s.getOwnerType(), s.getOwnerId(), s.getSlot(), s.getAssetId(), urls.get(s.getAssetId()), s.isCurrent())).toList();
    }

    private AssetDto dto(Asset a) {
        List<String> tags = a.getTags() == null ? List.of() : json.mapper().readValue(a.getTags(), new tools.jackson.core.type.TypeReference<List<String>>() {});
        return new AssetDto(a.getId(), a.getStorageKey(), a.getUrl(), a.getContentType(), a.getSizeBytes(), a.getWidth(), a.getHeight(), a.getTitle(), a.getCollection(), tags);
    }

    private static String extension(String name, String type) {
        if (name != null && name.lastIndexOf('.') > 0) return name.substring(name.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        return switch (type) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "image/svg+xml" -> ".svg";
            default -> "";
        };
    }

    private static String stripExtension(String name) {
        if (name == null) return "asset";
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(0, i) : name;
    }
}
