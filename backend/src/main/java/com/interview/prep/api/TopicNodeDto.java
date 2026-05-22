package com.interview.prep.api;

import java.util.List;

public record TopicNodeDto(
        Long id,
        String slug,
        String title,
        Short difficulty,
        List<TopicNodeDto> children,
        List<TopicEntryDto> entries
) {}
