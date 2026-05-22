package com.interview.prep.api;

import java.util.List;

public record EntryDto(
        String slug,
        String topicSlug,
        String title,
        String bodyMd,
        String locale,
        boolean fellBack,
        Short difficulty,
        List<String> sources,
        List<String> related,
        List<String> roCompanies,
        List<String> availableLocales
) {}
