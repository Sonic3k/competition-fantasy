package com.fantasy.competition.script;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fantasy.competition.entity.MediaFile;
import com.fantasy.competition.entity.Nation;
import com.fantasy.competition.repository.MediaFileRepository;
import com.fantasy.competition.repository.NationRepository;
import com.fantasy.competition.service.StorageService;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Component
public class FetchAOE2EmblemsScript implements ImportScript {

    private final NationRepository nationRepo;
    private final MediaFileRepository mediaRepo;
    private final StorageService storage;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

    // Map nation name → wiki filename
    private static final Map<String, String> EMBLEM_MAP = new LinkedHashMap<>() {{
        put("Britons", "Britons_emblem_AoE2.png");
        put("Byzantines", "Byzantines_emblem_AoE2.png");
        put("Celts", "Celts_emblem_AoE2.png");
        put("Chinese", "Chinese_emblem_AoE2.png");
        put("Franks", "Franks_emblem_AoE2.png");
        put("Goths", "Goths_emblem_AoE2.png");
        put("Japanese", "Japanese_emblem_AoE2.png");
        put("Mongols", "Mongols_emblem_AoE2.png");
        put("Persians", "Persians_emblem_AoE2.png");
        put("Saracens", "Saracens_emblem_AoE2.png");
        put("Teutons", "Teutons_emblem_AoE2.png");
        put("Turks", "Turks_emblem_AoE2.png");
        put("Vikings", "Vikings_emblem_AoE2.png");
        put("Aztecs", "Aztecs_emblem_AoE2.png");
        put("Huns", "Huns_emblem_AoE2.png");
        put("Koreans", "Koreans_emblem_AoE2.png");
        put("Maya", "Mayans_emblem_AoE2.png");
        put("Spanish", "Spanish_emblem_AoE2.png");
        put("Inca", "Incas_emblem_AoE2.png");
        put("Italians", "Italians_emblem_AoE2.png");
        put("Magyars", "Magyars_emblem_AoE2.png");
        put("Slavs", "Slavs_emblem_AoE2.png");
        put("Berbers", "Berbers_emblem_AoE2.png");
        put("Ethiopians", "Ethiopians_emblem_AoE2.png");
        put("Malians", "Malians_emblem_AoE2.png");
        put("Portuguese", "Portuguese_emblem_AoE2.png");
        put("Burmese", "Burmese_emblem_AoE2.png");
        put("Khmer", "Khmer_emblem_AoE2.png");
        put("Malay", "Malay_emblem_AoE2.png");
        put("Vietnamese", "Vietnamese_emblem_AoE2.png");
        put("Bulgarians", "Bulgarians_emblem_AoE2.png");
        put("Cumans", "Cumans_emblem_AoE2.png");
        put("Lithuanians", "Lithuanians_emblem_AoE2.png");
        put("Tatars", "Tatars_emblem_AoE2.png");
        put("Burgundians", "Burgundians_emblem_AoE2.png");
        put("Sicilians", "Sicilians_emblem_AoE2.png");
        put("Bohemians", "Bohemians_emblem_AoE2.png");
        put("Poles", "Poles_emblem_AoE2.png");
        put("Bengalis", "Bengalis_emblem_AoE2.png");
        put("Dravidians", "Dravidians_emblem_AoE2.png");
        put("Gurjaras", "Gurjaras_emblem_AoE2.png");
        put("Hindustanis", "Hindustanis_emblem_AoE2.png");
        put("Romans", "Romans_emblem_AoE2.png");
        put("Armenians", "Armenians_emblem_AoE2.png");
        put("Georgians", "Georgians_emblem_AoE2.png");
        put("Jurchens", "Jurchens_emblem_AoE2.png");
        put("Khitans", "Khitans_emblem_AoE2.png");
    }};

    public FetchAOE2EmblemsScript(NationRepository nationRepo, MediaFileRepository mediaRepo, StorageService storage) {
        this.nationRepo = nationRepo;
        this.mediaRepo = mediaRepo;
        this.storage = storage;
    }

    @Override public String getId() { return "fetch-aoe2-emblems"; }
    @Override public String getName() { return "Fetch AOE2 Civilization Emblems"; }
    @Override public String getDescription() { return "Downloads 47 civilization emblems from AOE2 Wiki → uploads to B2 → links as nation flags."; }
    @Override public String getIcon() { return "🏛️"; }

    @Override
    public void execute() {
        List<Nation> nations = nationRepo.findAll();
        int success = 0, skip = 0, fail = 0;

        for (var entry : EMBLEM_MAP.entrySet()) {
            String nationName = entry.getKey();
            String wikiFile = entry.getValue();

            Nation nation = nations.stream().filter(n -> n.getName().equals(nationName)).findFirst().orElse(null);
            if (nation == null) { skip++; continue; }
            if (nation.getFlagMedia() != null) { skip++; continue; } // already has emblem

            try {
                // Step 1: Get image URL from Fandom API
                String apiUrl = "https://ageofempires.fandom.com/api.php?action=query&titles=File:" +
                    wikiFile + "&prop=imageinfo&iiprop=url&format=json";
                HttpRequest req = HttpRequest.newBuilder().uri(URI.create(apiUrl))
                    .header("User-Agent", "CompetitionFantasy/1.0").GET().build();
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

                JsonNode root = mapper.readTree(resp.body());
                JsonNode pages = root.get("query").get("pages");
                String imageUrl = null;
                for (Iterator<JsonNode> it = pages.elements(); it.hasNext(); ) {
                    JsonNode page = it.next();
                    if (page.has("imageinfo")) {
                        imageUrl = page.get("imageinfo").get(0).get("url").asText();
                    }
                }
                if (imageUrl == null) { fail++; continue; }

                // Step 2: Download image
                HttpRequest imgReq = HttpRequest.newBuilder().uri(URI.create(imageUrl))
                    .header("User-Agent", "CompetitionFantasy/1.0").GET().build();
                HttpResponse<byte[]> imgResp = http.send(imgReq, HttpResponse.BodyHandlers.ofByteArray());
                byte[] imageBytes = imgResp.body();

                // Step 3: Upload to B2
                String path = "competition-fantasy/nations/" + nation.getId() + "/emblem.png";
                String cdnUrl = storage.uploadBytes(path, imageBytes, "image/png");

                // Step 4: Create MediaFile
                MediaFile mf = new MediaFile();
                mf.setFilename(wikiFile);
                mf.setCdnUrl(cdnUrl);
                mf.setContentType("image/png");
                mf.setFileSize((long) imageBytes.length);
                mf.setSourceType(MediaFile.SourceType.UPLOAD);
                mf = mediaRepo.save(mf);

                // Step 5: Link to nation
                nation.setFlagMedia(mf);
                nation.setFlagUrl(cdnUrl);
                nationRepo.save(nation);

                success++;
                Thread.sleep(500); // rate limit wiki
            } catch (Exception e) {
                fail++;
            }
        }

        if (fail > 0) throw new RuntimeException("Done: " + success + " OK, " + skip + " skipped, " + fail + " failed");
    }
}
