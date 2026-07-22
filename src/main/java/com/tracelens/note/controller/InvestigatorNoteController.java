package com.tracelens.note.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracelens.common.ApiResponse;
import com.tracelens.note.dto.CreateNoteRequest;
import com.tracelens.note.dto.NoteResponse;
import com.tracelens.note.dto.UpdateNoteRequest;
import com.tracelens.note.service.InvestigatorNoteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class InvestigatorNoteController {

    private final InvestigatorNoteService noteService;

    public InvestigatorNoteController(
            InvestigatorNoteService noteService
    ) {
        this.noteService = noteService;
    }

    /*
     * Creates an investigator note for one owned case.
     */
    @PostMapping("/cases/{caseId}/notes")
    public ResponseEntity<ApiResponse<NoteResponse>>
            createNote(

                    @PathVariable Long caseId,

                    @Valid
                    @RequestBody
                    CreateNoteRequest request,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        NoteResponse result =
                noteService.createNote(
                        caseId,
                        request,
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Investigator note created successfully",
                        result
                )
        );
    }

    /*
     * Retrieves all notes belonging to one owned case.
     */
    @GetMapping("/cases/{caseId}/notes")
    public ResponseEntity<
            ApiResponse<List<NoteResponse>>>
            getNotes(

                    @PathVariable Long caseId,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        List<NoteResponse> result =
                noteService.getNotes(
                        caseId,
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Investigator notes retrieved successfully",
                        result
                )
        );
    }

    /*
     * Updates both note content and pinned state.
     */
    @PutMapping("/notes/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponse>>
            updateNote(

                    @PathVariable Long noteId,

                    @Valid
                    @RequestBody
                    UpdateNoteRequest request,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        NoteResponse result =
                noteService.updateNote(
                        noteId,
                        request,
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Investigator note updated successfully",
                        result
                )
        );
    }

    /*
     * Deletes one owned investigator note.
     */
    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<ApiResponse<Void>>
            deleteNote(

                    @PathVariable Long noteId,

                    @AuthenticationPrincipal Jwt jwt
            ) {

        noteService.deleteNote(
                noteId,
                jwt.getSubject()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Investigator note deleted successfully",
                        null
                )
        );
    }
}