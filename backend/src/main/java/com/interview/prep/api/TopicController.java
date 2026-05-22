package com.interview.prep.api;

import com.interview.prep.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/topics")
class TopicController {

    private final TopicRepository topics;
    private final TheoryEntryRepository entries;

    TopicController(TopicRepository topics, TheoryEntryRepository entries) {
        this.topics = topics;
        this.entries = entries;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<TopicNodeDto> tree() {
        List<Topic> all = topics.findAll();
        Map<Long, TopicNodeDto> byId = new LinkedHashMap<>();
        for (Topic t : all) {
            byId.put(t.getId(), new TopicNodeDto(t.getId(), t.getSlug(), t.getTitle(), t.getDifficulty(), new ArrayList<>(), new ArrayList<>()));
        }
        List<TopicNodeDto> roots = new ArrayList<>();
        for (Topic t : all) {
            TopicNodeDto node = byId.get(t.getId());
            if (t.getParent() == null) {
                roots.add(node);
            } else {
                TopicNodeDto parent = byId.get(t.getParent().getId());
                if (parent != null) parent.children().add(node);
            }
        }
        // attach entries grouped by topic
        for (TheoryEntry e : entries.findAll()) {
            TopicNodeDto node = byId.get(e.getTopic().getId());
            if (node != null) {
                node.entries().add(new TopicEntryDto(e.getSlug(), firstAvailableTitle(e)));
            }
        }
        roots.sort(Comparator.comparing(TopicNodeDto::title));
        return roots;
    }

    @GetMapping("/{slug}")
    @Transactional(readOnly = true)
    public ResponseEntity<EntryDto> entry(@PathVariable String slug,
                                          @RequestParam(defaultValue = "en") String locale) {
        // slug param uses path-style; controllers receive it raw, but path encoding
        // requires a different mapping if it contains slashes — see entryDeep below.
        return entries.findBySlug(slug)
                .map(e -> ResponseEntity.ok(toDto(e, locale)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-path/**")
    @Transactional(readOnly = true)
    public ResponseEntity<EntryDto> entryDeep(@RequestParam(defaultValue = "en") String locale,
                                              jakarta.servlet.http.HttpServletRequest req) {
        String path = req.getRequestURI();
        String prefix = req.getContextPath() + "/api/topics/by-path/";
        String slug = path.substring(prefix.length());
        return entries.findBySlug(slug)
                .map(e -> ResponseEntity.ok(toDto(e, locale)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private EntryDto toDto(TheoryEntry e, String localeRaw) {
        TheoryEntryBody.Locale requested = parseLocale(localeRaw);
        TheoryEntryBody body = pickBody(e, requested);
        boolean fellBack = body != null && body.getLocale() != requested;
        return new EntryDto(
                e.getSlug(),
                e.getTopic().getSlug(),
                body == null ? e.getSlug() : body.getTitle(),
                body == null ? "" : body.getBodyMd(),
                body == null ? "en" : body.getLocale().name(),
                fellBack,
                e.getDifficulty(),
                e.getSources(),
                e.getRelated(),
                e.getRoCompanies(),
                e.getBodies().stream().map(b -> b.getLocale().name()).collect(Collectors.toList())
        );
    }

    private TheoryEntryBody pickBody(TheoryEntry e, TheoryEntryBody.Locale wanted) {
        return e.getBodies().stream().filter(b -> b.getLocale() == wanted).findFirst()
                .or(() -> e.getBodies().stream().filter(b -> b.getLocale() == TheoryEntryBody.Locale.en).findFirst())
                .or(() -> e.getBodies().stream().findFirst())
                .orElse(null);
    }

    private TheoryEntryBody.Locale parseLocale(String raw) {
        try { return TheoryEntryBody.Locale.valueOf(raw.toLowerCase()); }
        catch (Exception ex) { return TheoryEntryBody.Locale.en; }
    }

    private String firstAvailableTitle(TheoryEntry e) {
        return e.getBodies().stream()
                .filter(b -> b.getLocale() == TheoryEntryBody.Locale.en)
                .map(TheoryEntryBody::getTitle)
                .findFirst()
                .orElseGet(() -> e.getBodies().stream().map(TheoryEntryBody::getTitle).findFirst().orElse(e.getSlug()));
    }
}
