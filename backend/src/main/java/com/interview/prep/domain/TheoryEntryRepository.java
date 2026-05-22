package com.interview.prep.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TheoryEntryRepository extends JpaRepository<TheoryEntry, Long> {

    @EntityGraph(attributePaths = {"bodies", "topic"})
    Optional<TheoryEntry> findBySlug(String slug);
}
