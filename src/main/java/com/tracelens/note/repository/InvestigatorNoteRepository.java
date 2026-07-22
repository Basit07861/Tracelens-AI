package com.tracelens.note.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelens.note.entity.InvestigatorNote;

public interface InvestigatorNoteRepository
        extends JpaRepository<InvestigatorNote, Long> {

    /*
     * Retrieves notes only when the authenticated user
     * owns the investigation case.
     *
     * Pinned notes appear first. Within each group,
     * newest notes appear first.
     */
    @EntityGraph(
            attributePaths = {
                    "investigationCase",
                    "author"
            }
    )
    List<InvestigatorNote>
            findAllByInvestigationCaseIdAndInvestigationCaseOwnerEmailIgnoreCaseOrderByPinnedDescCreatedAtDesc(
                    Long caseId,
                    String ownerEmail
            );

    /*
     * Secure note lookup for update and delete.
     *
     * A note belonging to another investigator is treated
     * the same as a missing note.
     */
    @EntityGraph(
            attributePaths = {
                    "investigationCase",
                    "author"
            }
    )
    Optional<InvestigatorNote>
            findByIdAndInvestigationCaseOwnerEmailIgnoreCase(
                    Long noteId,
                    String ownerEmail
            );

    long countByInvestigationCaseId(
            Long caseId
    );

    long countByInvestigationCaseIdAndPinnedTrue(
            Long caseId
    );
}