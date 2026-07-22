package com.tracelens.note.entity;

import java.time.Instant;

import com.tracelens.investigation.entity.InvestigationCase;
import com.tracelens.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(
        name = "investigator_notes",
        indexes = {
                @Index(
                        name = "idx_note_case_created",
                        columnList = "case_id, created_at"
                ),
                @Index(
                        name = "idx_note_case_pinned_created",
                        columnList = "case_id, pinned, created_at"
                ),
                @Index(
                        name = "idx_note_author",
                        columnList = "author_id"
                )
        }
)
public class InvestigatorNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Optimistic locking prevents one note update from
     * silently overwriting another simultaneous update.
     */
    @Version
    private Long version;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "case_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_note_investigation_case"
            )
    )
    private InvestigationCase investigationCase;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "author_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_note_author"
            )
    )
    private User author;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;

    @Column(nullable = false)
    private boolean pinned;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    public InvestigatorNote() {
    }

    @PrePersist
    public void beforeInsert() {

        Instant currentTime = Instant.now();

        createdAt = currentTime;
        updatedAt = currentTime;

        content = normalizeContent(content);
    }

    @PreUpdate
    public void beforeUpdate() {

        updatedAt = Instant.now();
        content = normalizeContent(content);
    }

    private String normalizeContent(
            String value
    ) {

        return value == null
                ? null
                : value.strip();
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public InvestigationCase getInvestigationCase() {
        return investigationCase;
    }

    public void setInvestigationCase(
            InvestigationCase investigationCase
    ) {
        this.investigationCase = investigationCase;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(
            User author
    ) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(
            String content
    ) {
        this.content = content;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(
            boolean pinned
    ) {
        this.pinned = pinned;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}