package com.tracelens.investigation.entity;

import java.time.Instant;

import com.tracelens.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "investigation_cases",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_investigation_cases_case_number",
                        columnNames = "case_number"
                )
        },
        indexes = {
                @Index(
                        name = "idx_investigation_cases_owner",
                        columnList = "owner_id"
                ),
                @Index(
                        name = "idx_investigation_cases_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_investigation_cases_priority",
                        columnList = "priority"
                ),
                @Index(
                        name = "idx_investigation_cases_created_at",
                        columnList = "created_at"
                )
        }
)
public class InvestigationCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "case_number",
            nullable = false,
            length = 40
    )
    private String caseNumber;

    @Column(
            nullable = false,
            length = 150
    )
    private String title;

    /*
     * Stored as MySQL TEXT, but deliberately not marked with @Lob.
     * This keeps the Java attribute mapped as a searchable String.
     */
    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private CaseStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private CasePriority priority;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "owner_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_investigation_cases_owner"
            )
    )
    private User owner;

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

    public InvestigationCase() {
    }

    @PrePersist
    public void beforeInsert() {

        Instant currentTime = Instant.now();

        if (this.status == null) {
            this.status = CaseStatus.OPEN;
        }

        if (this.priority == null) {
            this.priority = CasePriority.MEDIUM;
        }

        this.createdAt = currentTime;
        this.updatedAt = currentTime;
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public CasePriority getPriority() {
        return priority;
    }

    public void setPriority(CasePriority priority) {
        this.priority = priority;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}