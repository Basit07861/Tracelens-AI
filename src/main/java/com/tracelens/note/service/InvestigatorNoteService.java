package com.tracelens.note.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracelens.exception.CaseNotFoundException;
import com.tracelens.exception.InvalidRequestException;
import com.tracelens.exception.NoteNotFoundException;
import com.tracelens.exception.UserNotFoundException;
import com.tracelens.investigation.entity.InvestigationCase;
import com.tracelens.investigation.repository.InvestigationCaseRepository;
import com.tracelens.note.dto.CreateNoteRequest;
import com.tracelens.note.dto.NoteResponse;
import com.tracelens.note.dto.UpdateNoteRequest;
import com.tracelens.note.entity.InvestigatorNote;
import com.tracelens.note.repository.InvestigatorNoteRepository;
import com.tracelens.user.entity.User;
import com.tracelens.user.repository.UserRepository;

@Service
public class InvestigatorNoteService {

    private static final int MAXIMUM_CONTENT_LENGTH = 5000;

    private final InvestigatorNoteRepository noteRepository;

    private final InvestigationCaseRepository caseRepository;

    private final UserRepository userRepository;

    public InvestigatorNoteService(
            InvestigatorNoteRepository noteRepository,
            InvestigationCaseRepository caseRepository,
            UserRepository userRepository
    ) {
        this.noteRepository = noteRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
    }

    /*
     * Creates a note for an investigation case owned by
     * the currently authenticated investigator.
     *
     * The client never supplies the case owner or note author.
     * Both are resolved securely from the JWT email.
     */
    @Transactional
    public NoteResponse createNote(
            Long caseId,
            CreateNoteRequest request,
            String authenticatedEmail
    ) {

        validateCreateRequest(request);

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        InvestigationCase investigationCase =
                findOwnedCase(
                        caseId,
                        normalizedEmail
                );

        User author =
                userRepository
                        .findByEmailIgnoreCase(
                                normalizedEmail
                        )
                        .orElseThrow(
                                () -> new UserNotFoundException(
                                        "User account was not found"
                                )
                        );

        InvestigatorNote note =
                new InvestigatorNote();

        note.setInvestigationCase(investigationCase);
        note.setAuthor(author);

        note.setContent(
                normalizeContent(request.content())
        );

        note.setPinned(request.pinned());

        InvestigatorNote savedNote =
                noteRepository.saveAndFlush(note);

        return mapToResponse(savedNote);
    }

    /*
     * Retrieves all notes for one owned investigation case.
     *
     * The repository returns pinned notes first and then
     * orders notes from newest to oldest.
     */
    @Transactional(readOnly = true)
    public List<NoteResponse> getNotes(
            Long caseId,
            String authenticatedEmail
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        InvestigationCase investigationCase =
                findOwnedCase(
                        caseId,
                        normalizedEmail
                );

        return noteRepository
                .findAllByInvestigationCaseIdAndInvestigationCaseOwnerEmailIgnoreCaseOrderByPinnedDescCreatedAtDesc(
                        investigationCase.getId(),
                        normalizedEmail
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * Updates the note content and its pinned state.
     *
     * An unowned note is returned as not found so the API
     * does not reveal that another investigator owns it.
     */
    @Transactional
    public NoteResponse updateNote(
            Long noteId,
            UpdateNoteRequest request,
            String authenticatedEmail
    ) {

        validateUpdateRequest(request);

        InvestigatorNote note =
                findOwnedNote(
                        noteId,
                        authenticatedEmail
                );

        note.setContent(
                normalizeContent(request.content())
        );

        note.setPinned(request.pinned());

        InvestigatorNote savedNote =
                noteRepository.saveAndFlush(note);

        return mapToResponse(savedNote);
    }

    /*
     * Deletes one owned investigator note.
     */
    @Transactional
    public void deleteNote(
            Long noteId,
            String authenticatedEmail
    ) {

        InvestigatorNote note =
                findOwnedNote(
                        noteId,
                        authenticatedEmail
                );

        noteRepository.delete(note);
        noteRepository.flush();
    }

    private InvestigationCase findOwnedCase(
            Long caseId,
            String normalizedEmail
    ) {

        if (caseId == null || caseId <= 0) {
            throw new CaseNotFoundException(
                    "Investigation case was not found"
            );
        }

        return caseRepository
                .findByIdAndOwnerEmailIgnoreCase(
                        caseId,
                        normalizedEmail
                )
                .orElseThrow(
                        () -> new CaseNotFoundException(
                                "Investigation case was not found"
                        )
                );
    }

    private InvestigatorNote findOwnedNote(
            Long noteId,
            String authenticatedEmail
    ) {

        if (noteId == null || noteId <= 0) {
            throw new NoteNotFoundException(
                    "Investigator note was not found"
            );
        }

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        return noteRepository
                .findByIdAndInvestigationCaseOwnerEmailIgnoreCase(
                        noteId,
                        normalizedEmail
                )
                .orElseThrow(
                        () -> new NoteNotFoundException(
                                "Investigator note was not found"
                        )
                );
    }

    private void validateCreateRequest(
            CreateNoteRequest request
    ) {

        if (request == null) {
            throw new InvalidRequestException(
                    "Note request is required"
            );
        }

        normalizeContent(request.content());
    }

    private void validateUpdateRequest(
            UpdateNoteRequest request
    ) {

        if (request == null) {
            throw new InvalidRequestException(
                    "Note request is required"
            );
        }

        normalizeContent(request.content());
    }

    private String normalizeContent(
            String content
    ) {

        if (content == null || content.isBlank()) {
            throw new InvalidRequestException(
                    "Note content is required"
            );
        }

        String normalizedContent =
                content.strip();

        if (
                normalizedContent.length()
                > MAXIMUM_CONTENT_LENGTH
        ) {
            throw new InvalidRequestException(
                    "Note content cannot exceed "
                    + MAXIMUM_CONTENT_LENGTH
                    + " characters"
            );
        }

        return normalizedContent;
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidRequestException(
                    "Authenticated user is unavailable"
            );
        }

        return email
                .strip()
                .toLowerCase(Locale.ROOT);
    }

    private NoteResponse mapToResponse(
            InvestigatorNote note
    ) {

        return new NoteResponse(
                note.getId(),
                note.getInvestigationCase().getId(),
                note.getAuthor().getId(),
                note.getAuthor().getFullName(),
                note.getContent(),
                note.isPinned(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}