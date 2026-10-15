package com.fantasy.competition.importer;

import com.fantasy.competition.common.AppProperties;
import com.fantasy.competition.common.BadRequestException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The committed import files (competition-api-v2/imports). Read from that folder when it exists on disk
 * (local runs, edits without a rebuild); otherwise from the copy packaged into the jar under imports/,
 * because the Railway image keeps only target/.
 */
@Component
public class ImportFiles {

    public record Entry(String path, long sizeBytes, Instant modifiedAt) {}

    private static final String ROOT = "imports/";

    private final Path dir;
    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(ImportFiles.class.getClassLoader());

    public ImportFiles(AppProperties props) {
        Path p = Paths.get(props.imports() == null ? "imports" : props.imports().dirOrDefault()).toAbsolutePath().normalize();
        this.dir = Files.isDirectory(p) ? p : null;
    }

    /** Where files are read from, for logs and the admin UI. */
    public String location() {
        return dir != null ? dir.toString() : "classpath:" + ROOT;
    }

    public List<Entry> list() throws IOException {
        Map<String, Entry> byPath = new LinkedHashMap<>();
        if (dir != null) {
            try (Stream<Path> walk = Files.walk(dir)) {
                for (Path p : walk.filter(f -> Files.isRegularFile(f) && f.toString().endsWith(".json")).toList()) {
                    String rel = dir.relativize(p).toString().replace('\\', '/');
                    if (rel.startsWith("uploads/")) continue;
                    byPath.put(rel, new Entry(rel, Files.size(p), Files.getLastModifiedTime(p).toInstant()));
                }
            }
        } else {
            for (Resource r : resolver.getResources("classpath*:" + ROOT + "**/*.json")) {
                String rel = relativePath(r);
                if (rel == null) continue;
                long modified = safeLastModified(r);
                byPath.putIfAbsent(rel, new Entry(rel, r.contentLength(), modified > 0 ? Instant.ofEpochMilli(modified) : null));
            }
        }
        List<Entry> out = new ArrayList<>(byPath.values());
        out.sort(Comparator.comparing(Entry::path));
        return out;
    }

    public String read(String relative) throws IOException {
        String rel = check(relative);
        if (dir != null) {
            Path p = dir.resolve(rel).normalize();
            if (!p.startsWith(dir) || !Files.isRegularFile(p)) throw new BadRequestException("Not an import file: " + relative);
            return Files.readString(p, StandardCharsets.UTF_8);
        }
        Resource r = resolver.getResource("classpath:" + ROOT + rel);
        if (!r.exists()) throw new BadRequestException("Not an import file: " + relative);
        return r.getContentAsString(StandardCharsets.UTF_8);
    }

    private static String check(String relative) {
        if (relative == null || relative.isBlank()) throw new BadRequestException("file is required");
        String rel = relative.trim().replace('\\', '/');
        if (rel.startsWith("/") || rel.contains("..") || !rel.endsWith(".json")) throw new BadRequestException("Not an import file: " + relative);
        return rel;
    }

    /** "imports/examples/a.json" inside a jar URL (…!/imports/…) or a classes folder (…/classes/imports/…) → "examples/a.json". */
    private static String relativePath(Resource r) throws IOException {
        String url = r.getURL().toString();
        int bang = url.lastIndexOf("!/");
        String tail = bang >= 0 ? url.substring(bang + 2) : url.substring(Math.max(0, url.lastIndexOf("/" + ROOT) + 1));
        return tail.startsWith(ROOT) ? tail.substring(ROOT.length()) : null;
    }

    private static long safeLastModified(Resource r) {
        try {
            return r.lastModified();
        } catch (IOException e) {
            return 0;
        }
    }
}
