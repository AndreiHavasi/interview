package com.interview.prep.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "topic")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Topic parent;

    @Column(name = "taxonomy_path", nullable = false)
    private String taxonomyPath;

    @Column(nullable = false)
    private String title;

    private Short difficulty;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Topic> children = new ArrayList<>();

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public Topic getParent() { return parent; }
    public void setParent(Topic parent) { this.parent = parent; }
    public String getTaxonomyPath() { return taxonomyPath; }
    public void setTaxonomyPath(String taxonomyPath) { this.taxonomyPath = taxonomyPath; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Short getDifficulty() { return difficulty; }
    public void setDifficulty(Short difficulty) { this.difficulty = difficulty; }
    public List<Topic> getChildren() { return children; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
