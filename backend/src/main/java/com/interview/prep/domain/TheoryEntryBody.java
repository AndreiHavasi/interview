package com.interview.prep.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "theory_entry_body",
        uniqueConstraints = @UniqueConstraint(name = "uq_entry_locale", columnNames = {"theory_entry_id", "locale"}))
public class TheoryEntryBody {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theory_entry_id", nullable = false)
    private TheoryEntry entry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Locale locale;

    @Column(nullable = false)
    private String title;

    @Column(name = "body_md", nullable = false, columnDefinition = "text")
    private String bodyMd;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public enum Locale { en, ro }

    public Long getId() { return id; }
    public TheoryEntry getEntry() { return entry; }
    public void setEntry(TheoryEntry entry) { this.entry = entry; }
    public Locale getLocale() { return locale; }
    public void setLocale(Locale locale) { this.locale = locale; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBodyMd() { return bodyMd; }
    public void setBodyMd(String bodyMd) { this.bodyMd = bodyMd; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
