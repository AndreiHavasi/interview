package com.interview.prep.content;

import com.interview.prep.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Stream;

/**
 * Scans the content/ directory and upserts topics + theory entries into the DB.
 *
 * Folder layout: content/&lt;taxonomy-path&gt;/&lt;entry-slug&gt;/{en.md, ro.md, meta.yaml}
 * The taxonomy path becomes a chain of Topic rows (one per segment).
 */
@Component
public class ContentLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ContentLoader.class);

    private final Path contentRoot;
    private final TopicRepository topics;
    private final TheoryEntryRepository entries;

    public ContentLoader(@Value("${interview.content.path}") String contentPath,
                         TopicRepository topics,
                         TheoryEntryRepository entries) {
        this.contentRoot = Paths.get(contentPath).toAbsolutePath().normalize();
        this.topics = topics;
        this.entries = entries;
    }

    @Override
    public void run(String... args) {
        reload();
    }

    public LoadResult reload() {
        if (!Files.isDirectory(contentRoot)) {
            log.warn("Content path {} does not exist; skipping load", contentRoot);
            return new LoadResult(0, 0, 0);
        }
        log.info("Loading content from {}", contentRoot);
        LoadResult totals = new LoadResult(0, 0, 0);
        try (Stream<Path> stream = Files.walk(contentRoot)) {
            List<Path> entryDirs = stream
                    .filter(p -> Files.isRegularFile(p.resolve("meta.yaml")))
                    .sorted()
                    .toList();
            for (Path dir : entryDirs) {
                LoadResult r = loadEntry(dir);
                totals = totals.add(r);
            }
        } catch (IOException e) {
            log.error("Failed to scan content directory", e);
        }
        log.info("Content load done: added={} updated={} unchanged={}",
                totals.added(), totals.updated(), totals.unchanged());
        return totals;
    }

    @Transactional
    protected LoadResult loadEntry(Path dir) {
        try {
            Path meta = dir.resolve("meta.yaml");
            Map<String, Object> data = parseYaml(meta);
            String relative = contentRoot.relativize(dir).toString().replace('\\', '/');
            String taxonomyPath = relative.substring(0, relative.lastIndexOf('/'));
            String entrySlug = relative;

            Topic topic = upsertTopicChain(taxonomyPath);
            String title = stringOrDefault(data.get("title"), entrySlug);
            Short difficulty = data.get("difficulty") instanceof Number n ? n.shortValue() : null;
            List<String> sources = stringList(data.get("sources"));
            List<String> related = stringList(data.get("related"));
            List<String> roCompanies = stringList(data.get("ro_companies"));

            TheoryEntry entry = entries.findBySlug(entrySlug).orElseGet(TheoryEntry::new);
            boolean isNew = entry.getId() == null;
            entry.setSlug(entrySlug);
            entry.setTopic(topic);
            entry.setDifficulty(difficulty);
            entry.setSources(sources);
            entry.setRelated(related);
            entry.setRoCompanies(roCompanies);
            entry.setUpdatedAt(OffsetDateTime.now());

            applyBody(entry, dir.resolve("en.md"), TheoryEntryBody.Locale.en, title);
            applyBody(entry, dir.resolve("ro.md"), TheoryEntryBody.Locale.ro, title);

            entries.save(entry);
            return isNew ? new LoadResult(1, 0, 0) : new LoadResult(0, 1, 0);
        } catch (Exception e) {
            log.error("Failed to load entry at {}", dir, e);
            return new LoadResult(0, 0, 1);
        }
    }

    private Topic upsertTopicChain(String taxonomyPath) {
        String[] segments = taxonomyPath.split("/");
        Topic parent = null;
        StringBuilder accum = new StringBuilder();
        for (String seg : segments) {
            if (accum.length() > 0) accum.append('/');
            accum.append(seg);
            String slug = accum.toString();
            Topic current = topics.findBySlug(slug).orElseGet(Topic::new);
            current.setSlug(slug);
            current.setTaxonomyPath(slug);
            if (current.getTitle() == null) current.setTitle(prettify(seg));
            current.setParent(parent);
            current.setUpdatedAt(OffsetDateTime.now());
            current = topics.save(current);
            parent = current;
        }
        return parent;
    }

    private void applyBody(TheoryEntry entry, Path file, TheoryEntryBody.Locale locale, String title) throws IOException {
        if (!Files.isRegularFile(file)) return;
        String md = Files.readString(file);
        TheoryEntryBody body = entry.getBodies().stream()
                .filter(b -> b.getLocale() == locale)
                .findFirst()
                .orElseGet(() -> {
                    TheoryEntryBody nb = new TheoryEntryBody();
                    nb.setEntry(entry);
                    nb.setLocale(locale);
                    entry.getBodies().add(nb);
                    return nb;
                });
        body.setTitle(title);
        body.setBodyMd(md);
        body.setUpdatedAt(OffsetDateTime.now());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYaml(Path file) throws IOException {
        try (var in = Files.newInputStream(file)) {
            Object o = new Yaml().load(in);
            return o instanceof Map ? (Map<String, Object>) o : Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object o) {
        if (o instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object item : list) if (item != null) out.add(item.toString());
            return out;
        }
        return new ArrayList<>();
    }

    private String stringOrDefault(Object o, String fallback) {
        return o == null ? fallback : o.toString();
    }

    private String prettify(String slug) {
        String[] words = slug.split("[-_]");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }

    public record LoadResult(int added, int updated, int unchanged) {
        LoadResult add(LoadResult o) {
            return new LoadResult(added + o.added, updated + o.updated, unchanged + o.unchanged);
        }
    }
}
