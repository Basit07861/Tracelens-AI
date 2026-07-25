import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";

import api from "../../api/client";
import "./NotesPanel.css";

const MAXIMUM_NOTE_LENGTH = 5000;

function formatDate(value) {
  if (!value) {
    return "Not available";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Not available";
  }

  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

function getErrorMessage(error, fallbackMessage) {
  const responseData = error.response?.data;

  if (responseData?.message) {
    return responseData.message;
  }

  if (Array.isArray(responseData?.errors)) {
    return responseData.errors
      .map(
        (item) =>
          item.message || String(item),
      )
      .join(" ");
  }

  if (
    responseData?.errors &&
    typeof responseData.errors === "object"
  ) {
    return Object.values(
      responseData.errors,
    ).join(" ");
  }

  if (!error.response) {
    return (
      "The TraceLens backend could not be reached. " +
      "Confirm that it is running on port 8080."
    );
  }

  return fallbackMessage;
}

function getNoteId(note) {
  return note?.id ?? note?.noteId ?? null;
}

function sortNotes(notes) {
  return [...notes].sort((first, second) => {
    if (first.pinned !== second.pinned) {
      return first.pinned ? -1 : 1;
    }

    const firstDate = new Date(
      first.createdAt || 0,
    ).getTime();

    const secondDate = new Date(
      second.createdAt || 0,
    ).getTime();

    return secondDate - firstDate;
  });
}

export default function NotesPanel({ caseId }) {
  const [notes, setNotes] = useState([]);

  const [content, setContent] = useState("");
  const [pinned, setPinned] = useState(false);

  const [editingNoteId, setEditingNoteId] =
    useState(null);

  const [isLoading, setIsLoading] =
    useState(true);

  const [activeOperation, setActiveOperation] =
    useState("");

  const [errorMessage, setErrorMessage] =
    useState("");

  const [successMessage, setSuccessMessage] =
    useState("");

  const orderedNotes = useMemo(
    () => sortNotes(notes),
    [notes],
  );

  const loadNotes = useCallback(
    async (signal) => {
      const response = await api.get(
        `/api/cases/${caseId}/notes`,
        {
          signal,
        },
      );

      return Array.isArray(response.data?.data)
        ? response.data.data
        : [];
    },
    [caseId],
  );

  useEffect(() => {
    const controller = new AbortController();

    async function retrieveNotes() {
      try {
        const retrievedNotes = await loadNotes(
          controller.signal,
        );

        if (!controller.signal.aborted) {
          setNotes(retrievedNotes);
        }
      } catch (error) {
        if (
          error.name !== "CanceledError" &&
          error.code !== "ERR_CANCELED" &&
          !controller.signal.aborted
        ) {
          setErrorMessage(
            getErrorMessage(
              error,
              "Investigator notes could not be retrieved.",
            ),
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    retrieveNotes();

    return () => controller.abort();
  }, [loadNotes]);

  async function refreshNotes() {
    const retrievedNotes = await loadNotes();
    setNotes(retrievedNotes);
  }

  function resetForm() {
    setContent("");
    setPinned(false);
    setEditingNoteId(null);
  }

  function handleEdit(note) {
    const noteId = getNoteId(note);

    if (!noteId) {
      setErrorMessage(
        "The selected note does not have a valid identifier.",
      );

      return;
    }

    setEditingNoteId(noteId);
    setContent(note.content || "");
    setPinned(Boolean(note.pinned));
    setErrorMessage("");
    setSuccessMessage("");

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }

  function handleCancelEdit() {
    resetForm();
    setErrorMessage("");
    setSuccessMessage("");
  }

  async function handleSubmit(event) {
    event.preventDefault();

    const normalizedContent = content.trim();

    if (!normalizedContent) {
      setErrorMessage(
        "Note content is required.",
      );

      return;
    }

    if (
      normalizedContent.length >
      MAXIMUM_NOTE_LENGTH
    ) {
      setErrorMessage(
        `Note content cannot exceed ${MAXIMUM_NOTE_LENGTH} characters.`,
      );

      return;
    }

    const isEditing = Boolean(editingNoteId);

    setActiveOperation(
      isEditing ? "update" : "create",
    );

    setErrorMessage("");
    setSuccessMessage("");

    try {
      const payload = {
        content: normalizedContent,
        pinned,
      };

      if (isEditing) {
        await api.put(
          `/api/notes/${editingNoteId}`,
          payload,
        );
      } else {
        await api.post(
          `/api/cases/${caseId}/notes`,
          payload,
        );
      }

      await refreshNotes();
      resetForm();

      setSuccessMessage(
        isEditing
          ? "Investigator note updated successfully."
          : "Investigator note added to the case file.",
      );
    } catch (error) {
      setErrorMessage(
        getErrorMessage(
          error,
          isEditing
            ? "The investigator note could not be updated."
            : "The investigator note could not be created.",
        ),
      );
    } finally {
      setActiveOperation("");
    }
  }

  async function handleTogglePin(note) {
    const noteId = getNoteId(note);

    if (!noteId) {
      setErrorMessage(
        "The selected note does not have a valid identifier.",
      );

      return;
    }

    setActiveOperation(`pin-${noteId}`);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      await api.put(`/api/notes/${noteId}`, {
        content: note.content,
        pinned: !note.pinned,
      });

      await refreshNotes();

      setSuccessMessage(
        note.pinned
          ? "The note was removed from the pinned register."
          : "The note was added to the pinned register.",
      );

      if (
        Number(editingNoteId) ===
        Number(noteId)
      ) {
        resetForm();
      }
    } catch (error) {
      setErrorMessage(
        getErrorMessage(
          error,
          "The note pin state could not be updated.",
        ),
      );
    } finally {
      setActiveOperation("");
    }
  }

  async function handleDelete(note) {
    const noteId = getNoteId(note);

    if (!noteId) {
      setErrorMessage(
        "The selected note does not have a valid identifier.",
      );

      return;
    }

    const confirmed = window.confirm(
      "Delete this investigator note permanently?",
    );

    if (!confirmed) {
      return;
    }

    setActiveOperation(`delete-${noteId}`);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      await api.delete(`/api/notes/${noteId}`);

      await refreshNotes();

      if (
        Number(editingNoteId) ===
        Number(noteId)
      ) {
        resetForm();
      }

      setSuccessMessage(
        "Investigator note deleted successfully.",
      );
    } catch (error) {
      setErrorMessage(
        getErrorMessage(
          error,
          "The investigator note could not be deleted.",
        ),
      );
    } finally {
      setActiveOperation("");
    }
  }

  return (
    <div className="notes-workspace">
      <section className="notes-editor-panel">
        <header className="notes-panel-header">
          <span>
            {editingNoteId
              ? `EDIT NOTE/${editingNoteId}`
              : "INVESTIGATOR NOTE ENTRY"}
          </span>

          <span>REGISTER TL-NT-01</span>
        </header>

        <form
          className="notes-editor-form"
          onSubmit={handleSubmit}
        >
          <div className="notes-content-field">
            <div>
              <label htmlFor="investigator-note">
                Investigator observation
              </label>

              <span>
                {content.length} /{" "}
                {MAXIMUM_NOTE_LENGTH}
              </span>
            </div>

            <textarea
              id="investigator-note"
              value={content}
              maxLength={MAXIMUM_NOTE_LENGTH}
              onChange={(event) =>
                setContent(event.target.value)
              }
              disabled={Boolean(activeOperation)}
              placeholder="Record observations, verification results, follow-up requirements or investigative conclusions."
            />
          </div>

          <label className="notes-pin-control">
            <input
              type="checkbox"
              checked={pinned}
              onChange={(event) =>
                setPinned(event.target.checked)
              }
              disabled={Boolean(activeOperation)}
            />

            <span className="notes-checkbox-mark" />

            <span>
              Pin this note to the priority register
            </span>
          </label>

          <div className="notes-editor-actions">
            {editingNoteId && (
              <button
                className="notes-secondary-button"
                type="button"
                onClick={handleCancelEdit}
                disabled={Boolean(activeOperation)}
              >
                CANCEL EDIT
              </button>
            )}

            <button
              className="notes-primary-button"
              type="submit"
              disabled={
                Boolean(activeOperation) ||
                !content.trim()
              }
            >
              <span>
                {activeOperation === "create"
                  ? "SAVING NOTE"
                  : activeOperation === "update"
                    ? "UPDATING NOTE"
                    : editingNoteId
                      ? "UPDATE NOTE"
                      : "ADD NOTE"}
              </span>

              <span aria-hidden="true">→</span>
            </button>
          </div>
        </form>
      </section>

      {successMessage && (
        <div
          className="system-message system-message-success"
          role="status"
        >
          <span>NOTE OPERATION COMPLETE</span>
          {successMessage}
        </div>
      )}

      {errorMessage && (
        <div
          className="system-message system-message-error"
          role="alert"
        >
          <span>NOTE OPERATION ERROR</span>
          {errorMessage}
        </div>
      )}

      <section className="notes-register-panel">
        <header className="notes-panel-header">
          <span>CASE NOTE REGISTER</span>

          <span>
            {String(orderedNotes.length).padStart(
              2,
              "0",
            )}{" "}
            NOTES
          </span>
        </header>

        {isLoading && (
          <div className="notes-state">
            <span className="dashboard-loader" />

            <p>
              Retrieving investigator notes...
            </p>
          </div>
        )}

        {!isLoading &&
          orderedNotes.length === 0 && (
            <div className="notes-state">
              <span className="notes-empty-symbol">
                NT
              </span>

              <h2>No investigator notes</h2>

              <p>
                Add the first manual observation,
                verification result or follow-up
                requirement for this case.
              </p>
            </div>
          )}

        {!isLoading &&
          orderedNotes.length > 0 && (
            <div className="notes-list">
              {orderedNotes.map((note) => {
                const noteId = getNoteId(note);

                return (
                  <article
                    className={
                      note.pinned
                        ? "note-record note-record-pinned"
                        : "note-record"
                    }
                    key={noteId}
                  >
                    <header className="note-record-header">
                      <div>
                        <span>
                          NOTE/{noteId}
                        </span>

                        <strong>
                          {note.authorName ||
                            "Authenticated investigator"}
                        </strong>
                      </div>

                      <div className="note-record-badges">
                        {note.pinned && (
                          <span className="note-pinned-badge">
                            PINNED
                          </span>
                        )}

                        <span>
                          {formatDate(
                            note.createdAt,
                          )}
                        </span>
                      </div>
                    </header>

                    <p className="note-record-content">
                      {note.content}
                    </p>

                    <footer className="note-record-footer">
                      <div>
                        <span>LAST UPDATED</span>

                        <strong>
                          {formatDate(
                            note.updatedAt,
                          )}
                        </strong>
                      </div>

                      <div className="note-record-actions">
                        <button
                          type="button"
                          onClick={() =>
                            handleTogglePin(note)
                          }
                          disabled={Boolean(
                            activeOperation,
                          )}
                        >
                          {note.pinned
                            ? "UNPIN"
                            : "PIN"}
                        </button>

                        <button
                          type="button"
                          onClick={() =>
                            handleEdit(note)
                          }
                          disabled={Boolean(
                            activeOperation,
                          )}
                        >
                          EDIT
                        </button>

                        <button
                          className="note-delete-button"
                          type="button"
                          onClick={() =>
                            handleDelete(note)
                          }
                          disabled={Boolean(
                            activeOperation,
                          )}
                        >
                          DELETE
                        </button>
                      </div>
                    </footer>
                  </article>
                );
              })}
            </div>
          )}
      </section>
    </div>
  );
}