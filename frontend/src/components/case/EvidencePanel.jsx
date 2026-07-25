import { useCallback, useEffect, useRef, useState } from "react";

import api from "../../api/client";
import "./EvidencePanel.css";

const ALLOWED_EXTENSIONS = [".pdf", ".txt", ".csv", ".json"];
const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

function formatEnum(value) {
  if (!value) {
    return "Unknown";
  }

  return value
    .toLowerCase()
    .split("_")
    .map(
      (part) =>
        part.charAt(0).toUpperCase() + part.slice(1),
    )
    .join(" ");
}

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

function formatFileSize(bytes) {
  const numericBytes = Number(bytes);

  if (!Number.isFinite(numericBytes) || numericBytes < 0) {
    return "Unknown size";
  }

  if (numericBytes < 1024) {
    return `${numericBytes} B`;
  }

  if (numericBytes < 1024 * 1024) {
    return `${(numericBytes / 1024).toFixed(1)} KB`;
  }

  return `${(numericBytes / (1024 * 1024)).toFixed(2)} MB`;
}

function getFileExtension(fileName) {
  const lastDotIndex = fileName.lastIndexOf(".");

  if (lastDotIndex < 0) {
    return "";
  }

  return fileName
    .slice(lastDotIndex)
    .toLowerCase();
}

function getErrorMessage(error, fallbackMessage) {
  const responseData = error.response?.data;

  if (responseData?.message) {
    return responseData.message;
  }

  if (Array.isArray(responseData?.errors)) {
    return responseData.errors
      .map((item) => item.message || String(item))
      .join(" ");
  }

  if (
    responseData?.errors &&
    typeof responseData.errors === "object"
  ) {
    return Object.values(responseData.errors).join(" ");
  }

  if (!error.response) {
    return (
      "The TraceLens backend could not be reached. " +
      "Confirm that it is running on port 8080."
    );
  }

  return fallbackMessage;
}

function getEvidenceArray(responseData) {
  if (Array.isArray(responseData)) {
    return responseData;
  }

  if (Array.isArray(responseData?.content)) {
    return responseData.content;
  }

  return [];
}

export default function EvidencePanel({ caseId }) {
  const fileInputRef = useRef(null);

  const [evidenceItems, setEvidenceItems] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [description, setDescription] = useState("");

  const [isLoading, setIsLoading] = useState(true);
  const [isUploading, setIsUploading] = useState(false);
  const [activeAction, setActiveAction] = useState(null);

  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const loadEvidence = useCallback(async (signal) => {
    const response = await api.get(
      `/api/cases/${caseId}/evidence`,
      {
        signal,
        params: {
          page: 0,
          size: 50,
          sortBy: "uploadedAt",
          sortDirection: "desc",
        },
      },
    );

    return getEvidenceArray(response.data?.data);
  }, [caseId]);

  useEffect(() => {
    const controller = new AbortController();

    async function retrieveEvidence() {
      try {
        const items = await loadEvidence(controller.signal);

        if (!controller.signal.aborted) {
          setEvidenceItems(items);
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
              "Evidence records could not be retrieved.",
            ),
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    retrieveEvidence();

    return () => controller.abort();
  }, [loadEvidence]);

  async function refreshEvidence() {
    const items = await loadEvidence();
    setEvidenceItems(items);
  }

  function handleFileChange(event) {
    const file = event.target.files?.[0] || null;

    setErrorMessage("");
    setSuccessMessage("");

    if (!file) {
      setSelectedFile(null);
      return;
    }

    const extension = getFileExtension(file.name);

    if (!ALLOWED_EXTENSIONS.includes(extension)) {
      event.target.value = "";
      setSelectedFile(null);
      setErrorMessage(
        "Select a PDF, TXT, CSV or JSON evidence file.",
      );
      return;
    }

    if (file.size === 0) {
      event.target.value = "";
      setSelectedFile(null);
      setErrorMessage("The selected evidence file is empty.");
      return;
    }

    if (file.size > MAX_FILE_SIZE_BYTES) {
      event.target.value = "";
      setSelectedFile(null);
      setErrorMessage(
        "The evidence file cannot exceed 10 MB.",
      );
      return;
    }

    setSelectedFile(file);
  }

  function resetUploadForm() {
    setSelectedFile(null);
    setDescription("");

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  }

  async function handleUpload(event) {
    event.preventDefault();

    if (!selectedFile) {
      setErrorMessage("Select an evidence file to upload.");
      return;
    }

    setIsUploading(true);
    setErrorMessage("");
    setSuccessMessage("");

    const formData = new FormData();

    formData.append("file", selectedFile);

    if (description.trim()) {
      formData.append("description", description.trim());
    }

    try {
      await api.post(
        `/api/cases/${caseId}/evidence`,
        formData,
      );

      await refreshEvidence();
      resetUploadForm();

      setSuccessMessage(
        "Evidence file uploaded and registered successfully.",
      );
    } catch (error) {
      setErrorMessage(
        getErrorMessage(
          error,
          "The evidence file could not be uploaded.",
        ),
      );
    } finally {
      setIsUploading(false);
    }
  }

  async function runEvidenceAction(
    evidenceId,
    actionName,
    endpoint,
    successText,
  ) {
    setActiveAction(`${actionName}-${evidenceId}`);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      await api.post(endpoint);
      await refreshEvidence();
      setSuccessMessage(successText);
    } catch (error) {
      setErrorMessage(
        getErrorMessage(
          error,
          "The evidence operation could not be completed.",
        ),
      );
    } finally {
      setActiveAction(null);
    }
  }

  async function handleDelete(evidenceItem) {
    const confirmed = window.confirm(
      `Delete evidence "${evidenceItem.originalFileName}"?\n\n` +
        "This removes the evidence record and its stored file. " +
        "This action cannot be undone.",
    );

    if (!confirmed) {
      return;
    }

    setActiveAction(`delete-${evidenceItem.id}`);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      await api.delete(
        `/api/evidence/${evidenceItem.id}`,
      );

      await refreshEvidence();

      setSuccessMessage(
        "Evidence file deleted successfully.",
      );
    } catch (error) {
      setErrorMessage(
        getErrorMessage(
          error,
          "The evidence file could not be deleted.",
        ),
      );
    } finally {
      setActiveAction(null);
    }
  }

  async function handleDownload(evidenceItem) {
    setActiveAction(`download-${evidenceItem.id}`);
    setErrorMessage("");

    try {
      const response = await api.get(
        `/api/evidence/${evidenceItem.id}/download`,
        {
          responseType: "blob",
        },
      );

      const objectUrl = URL.createObjectURL(
        response.data,
      );

      const downloadLink = document.createElement("a");

      downloadLink.href = objectUrl;
      downloadLink.download =
        evidenceItem.originalFileName || "evidence-file";
      document.body.appendChild(downloadLink);
      downloadLink.click();
      downloadLink.remove();

      URL.revokeObjectURL(objectUrl);
    } catch (error) {
      setErrorMessage(
        getErrorMessage(
          error,
          "The evidence file could not be downloaded.",
        ),
      );
    } finally {
      setActiveAction(null);
    }
  }

  return (
    <div className="evidence-workspace">
      <section className="evidence-upload-panel">
        <header className="evidence-panel-header">
          <span>EVIDENCE INTAKE</span>
          <span>FORM TL-EV-01</span>
        </header>

        <form
          className="evidence-upload-form"
          onSubmit={handleUpload}
        >
          <div className="evidence-drop-zone">
            <input
              ref={fileInputRef}
              id="evidence-file"
              type="file"
              accept=".pdf,.txt,.csv,.json"
              onChange={handleFileChange}
              disabled={isUploading}
            />

            <label htmlFor="evidence-file">
              <span className="evidence-upload-symbol">
                ＋
              </span>

              <strong>
                {selectedFile
                  ? selectedFile.name
                  : "Select evidence file"}
              </strong>

              <span>
                PDF, TXT, CSV or JSON · Maximum 10 MB
              </span>

              {selectedFile && (
                <small>
                  {formatFileSize(selectedFile.size)}
                </small>
              )}
            </label>
          </div>

          <div className="evidence-description-field">
            <div>
              <label htmlFor="evidence-description">
                Evidence description
              </label>

              <span>OPTIONAL</span>
            </div>

            <textarea
              id="evidence-description"
              value={description}
              onChange={(event) =>
                setDescription(event.target.value)
              }
              placeholder="Record the relevance, source or acquisition context."
              maxLength={500}
              disabled={isUploading}
            />

            <small>{description.length} / 500</small>
          </div>

          <button
            className="evidence-upload-button"
            type="submit"
            disabled={isUploading || !selectedFile}
          >
            <span>
              {isUploading
                ? "REGISTERING EVIDENCE"
                : "UPLOAD AND REGISTER"}
            </span>

            <span aria-hidden="true">→</span>
          </button>
        </form>
      </section>

      {successMessage && (
        <div
          className="system-message system-message-success"
          role="status"
        >
          <span>OPERATION COMPLETE</span>
          {successMessage}
        </div>
      )}

      {errorMessage && (
        <div
          className="system-message system-message-error"
          role="alert"
        >
          <span>EVIDENCE ERROR</span>
          {errorMessage}
        </div>
      )}

      <section className="evidence-register-panel">
        <header className="evidence-panel-header">
          <span>EVIDENCE REGISTER</span>

          <span>
            {String(evidenceItems.length).padStart(2, "0")} ITEMS
          </span>
        </header>

        {isLoading ? (
          <div className="evidence-empty-state">
            <span className="dashboard-loader" />
            <p>Retrieving secured evidence records...</p>
          </div>
        ) : evidenceItems.length === 0 ? (
          <div className="evidence-empty-state">
            <span className="empty-crosshair" />

            <h3>No evidence registered</h3>

            <p>
              Upload the first evidence item for this
              investigation case.
            </p>
          </div>
        ) : (
          <div className="evidence-list">
            {evidenceItems.map((evidenceItem) => {
              const isProcessing =
                activeAction?.endsWith(
                  `-${evidenceItem.id}`,
                );

              return (
                <article
                  className="evidence-record"
                  key={evidenceItem.id}
                >
                  <header className="evidence-record-header">
                    <div>
                      <span>
                        EVIDENCE/{evidenceItem.id}
                      </span>

                      <strong>
                        {evidenceItem.originalFileName}
                      </strong>
                    </div>

                    <div className="evidence-record-badges">
                      <span
                        className={`evidence-badge evidence-status-${evidenceItem.status.toLowerCase()}`}
                      >
                        {formatEnum(evidenceItem.status)}
                      </span>

                      <span
                        className={`evidence-badge integrity-${evidenceItem.integrityStatus.toLowerCase()}`}
                      >
                        {formatEnum(
                          evidenceItem.integrityStatus,
                        )}
                      </span>
                    </div>
                  </header>

                  <div className="evidence-record-body">
                    <dl className="evidence-metadata">
                      <div>
                        <dt>TYPE</dt>
                        <dd>
                          {formatEnum(evidenceItem.fileType)}
                        </dd>
                      </div>

                      <div>
                        <dt>SIZE</dt>
                        <dd>
                          {formatFileSize(
                            evidenceItem.fileSizeBytes,
                          )}
                        </dd>
                      </div>

                      <div>
                        <dt>CONTENT TYPE</dt>
                        <dd>
                          {evidenceItem.contentType ||
                            "Unknown"}
                        </dd>
                      </div>

                      <div>
                        <dt>UPLOADED</dt>
                        <dd>
                          {formatDate(
                            evidenceItem.uploadedAt,
                          )}
                        </dd>
                      </div>
                    </dl>

                    {evidenceItem.description && (
                      <p className="evidence-description">
                        {evidenceItem.description}
                      </p>
                    )}

                    <div className="evidence-hash">
                      <span>SHA-256</span>

                      <code>
                        {evidenceItem.sha256Hash ||
                          "Hash unavailable"}
                      </code>
                    </div>
                  </div>

                  <footer className="evidence-actions">
                    <button
                      type="button"
                      onClick={() =>
                        runEvidenceAction(
                          evidenceItem.id,
                          "verify",
                          `/api/evidence/${evidenceItem.id}/verify-integrity`,
                          "Evidence integrity verified successfully.",
                        )
                      }
                      disabled={isProcessing}
                    >
                      VERIFY INTEGRITY
                    </button>

                    <button
                      type="button"
                      onClick={() =>
                        runEvidenceAction(
                          evidenceItem.id,
                          "extract",
                          `/api/evidence/${evidenceItem.id}/extract-text`,
                          "Evidence text extracted successfully.",
                        )
                      }
                      disabled={
                        isProcessing ||
                        evidenceItem.status === "PROCESSING"
                      }
                    >
                      EXTRACT TEXT
                    </button>

                    <button
                      type="button"
                      onClick={() =>
                        handleDownload(evidenceItem)
                      }
                      disabled={isProcessing}
                    >
                      DOWNLOAD
                    </button>

                    <button
                      className="evidence-delete-button"
                      type="button"
                      onClick={() =>
                        handleDelete(evidenceItem)
                      }
                      disabled={isProcessing}
                    >
                      DELETE
                    </button>
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