package com.interview.prep.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "theory_entry")
public class TheoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false, unique = true)
    private String slug;

    private Short difficulty;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sources_json", nullable = false, columnDefinition = "jsonb")
    private List<String> sources = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "related_json", nullable = false, columnDefinition = "jsonb")
    private List<String> related = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ro_companies", nullable = false, columnDefinition = "jsonb")
    private List<String> roCompanies = new ArrayList<>();

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TheoryEntryBody> bodies = new ArrayList<>();

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public Short getDifficulty() { return difficulty; }
    public void setDifficulty(Short difficulty) { this.difficulty = difficulty; }
    public List<String> getSources() { return sources; }
    public void setSources(List<String> sources) { this.sources = sources; }
    public List<String> getRelated() { return related; }
    public void setRelated(List<String> related) { this.related = related; }
    public List<String> getRoCompanies() { return roCompanies; }
    public void setRoCompanies(List<String> roCompanies) { this.roCompanies = roCompanies; }
    public List<TheoryEntryBody> getBodies() { return bodies; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
