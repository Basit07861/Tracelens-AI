package com.tracelens.evidence.entity;

import java.time.Instant;

import com.tracelens.investigation.entity.InvestigationCase;

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
        name = "evidence_files",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_evidence_files_stored_file_name",
                        columnNames = "stored_file_name"
                ),
                @UniqueConstraint(
                        name = "uk_evidence_files_case_sha256",
                        columnNames = {
                                "case_id",
                                "sha256_hash"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_evidence_files_case",
                        columnList = "case_id"
                ),
                @Index(
                        name = "idx_evidence_files_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_evidence_files_uploaded_at",
                        columnList = "uploaded_at"
                ),
                @Index(
                        name = "idx_evidence_files_sha256",
                        columnList = "sha256_hash"
                ),
                @Index(
                        name = "idx_evidence_files_integrity_status",
                        columnList = "integrity_status"
                )
        }
)
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "original_file_name",
            nullable = false,
            length = 255
    )
    private String originalFileName;

    @Column(
            name = "stored_file_name",
            nullable = false,
            length = 255
    )
    private String storedFileName;

    @Column(
            name = "storage_relative_path",
            nullable = false,
            length = 500
    )
    private String storageRelativePath;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "file_type",
            nullable = false,
            length = 20
    )
    private EvidenceFileType fileType;

    @Column(
            name = "content_type",
            nullable = false,
            length = 150
    )
    private String contentType;

    @Column(
            name = "file_size_bytes",
            nullable = false
    )
    private long fileSizeBytes;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private EvidenceStatus status;

    /*
     * SHA-256 represented as lowercase hexadecimal text.
     *
     * SHA-256 produces 32 bytes:
     * 32 bytes × 2 hexadecimal characters = 64 characters.
     *
     * Nullable for safe migration of evidence uploaded before
     * the integrity feature was introduced.
     */
    @Column(
            name = "sha256_hash",
            length = 64
    )
    private String sha256Hash;

    /*
     * Nullable for existing development records.
     * New evidence will receive a value during upload.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "integrity_status",
            length = 30
    )
    private EvidenceIntegrityStatus integrityStatus;

    @Column(
            name = "last_integrity_verified_at"
    )
    private Instant lastIntegrityVerifiedAt;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "case_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_evidence_files_case"
            )
    )
    private InvestigationCase investigationCase;

    @Column(
            name = "uploaded_at",
            nullable = false,
            updatable = false
    )
    private Instant uploadedAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    public Evidence() {
    }

    @PrePersist
    public void beforeInsert() {

        Instant currentTime = Instant.now();

        if (this.status == null) {
            this.status = EvidenceStatus.UPLOADED;
        }

        if (this.integrityStatus == null) {
            this.integrityStatus =
                    EvidenceIntegrityStatus.NOT_VERIFIED;
        }

        this.uploadedAt = currentTime;
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

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(
            String originalFileName
    ) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(
            String storedFileName
    ) {
        this.storedFileName = storedFileName;
    }

    public String getStorageRelativePath() {
        return storageRelativePath;
    }

    public void setStorageRelativePath(
            String storageRelativePath
    ) {
        this.storageRelativePath = storageRelativePath;
    }

    public EvidenceFileType getFileType() {
        return fileType;
    }

    public void setFileType(
            EvidenceFileType fileType
    ) {
        this.fileType = fileType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(
            String contentType
    ) {
        this.contentType = contentType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(
            long fileSizeBytes
    ) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public EvidenceStatus getStatus() {
        return status;
    }

    public void setStatus(
            EvidenceStatus status
    ) {
        this.status = status;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public void setSha256Hash(
            String sha256Hash
    ) {
        this.sha256Hash = sha256Hash;
    }

    public EvidenceIntegrityStatus getIntegrityStatus() {
        return integrityStatus;
    }

    public void setIntegrityStatus(
            EvidenceIntegrityStatus integrityStatus
    ) {
        this.integrityStatus = integrityStatus;
    }

    public Instant getLastIntegrityVerifiedAt() {
        return lastIntegrityVerifiedAt;
    }

    public void setLastIntegrityVerifiedAt(
            Instant lastIntegrityVerifiedAt
    ) {
        this.lastIntegrityVerifiedAt =
                lastIntegrityVerifiedAt;
    }

    public InvestigationCase getInvestigationCase() {
        return investigationCase;
    }

    public void setInvestigationCase(
            InvestigationCase investigationCase
    ) {
        this.investigationCase = investigationCase;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}