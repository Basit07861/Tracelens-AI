import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";

import api from "../../api/client";
import "./ReportPanel.css";

function formatEnum(value) {
  if (!value) {
    return "Unknown";
  }

  return value
    .toLowerCase()
    .split("_")
    .map(
      (part) =>
        part.charAt(0).toUpperCase() +
        part.slice(1),
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

function formatBytes(value) {
  const number = Number(value);

  if (!Number.isFinite(number)) {
    return "Not available";
  }

  if (number < 1024) {
    return `${number} B`;
  }

  if (number < 1024 * 1024) {
    return `${(number / 1024).toFixed(1)} KB`;
  }

  return `${(
    number /
    (1024 * 1024)
  ).toFixed(1)} MB`;
}

function formatConfidence(value) {
  if (
    value === null ||
    value === undefined ||
    Number.isNaN(Number(value))
  ) {
    return "Not reported";
  }

  return `${Math.round(Number(value) * 100)}%`;
}

function getErrorMessage(error, fallbackMessage) {
  const responseData = error.response?.data;

  if (responseData?.message) {
    return responseData.message;
  }

  if (!error.response) {
    return (
      "The TraceLens backend could not be reached. " +
      "Confirm that it is running on port 8080."
    );
  }

  return fallbackMessage;
}

function safeArray(value) {
  return Array.isArray(value) ? value : [];
}

function ReportList({
  items,
  emptyMessage,
}) {
  if (items.length === 0) {
    return (
      <p className="report-empty-line">
        {emptyMessage}
      </p>
    );
  }

  return (
    <ol className="report-ordered-list">
      {items.map((item, index) => (
        <li key={`${item}-${index}`}>
          {item}
        </li>
      ))}
    </ol>
  );
}

export default function ReportPanel({ caseId }) {
  const [report, setReport] = useState(null);

  const [isLoading, setIsLoading] =
    useState(true);

  const [isRefreshing, setIsRefreshing] =
    useState(false);

  const [errorMessage, setErrorMessage] =
    useState("");

  const loadReport = useCallback(
    async (signal) => {
      const response = await api.get(
        `/api/cases/${caseId}/report`,
        {
          signal,
        },
      );

      return response.data?.data || null;
    },
    [caseId],
  );

  useEffect(() => {
    const controller = new AbortController();

    async function retrieveReport() {
      try {
        const retrievedReport =
          await loadReport(controller.signal);

        if (!controller.signal.aborted) {
          setReport(retrievedReport);
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
              "The final case report could not be generated.",
            ),
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    retrieveReport();

    return () => controller.abort();
  }, [loadReport]);

  const investigationCase = useMemo(
    () =>
      report?.investigationCase ||
      report?.case ||
      null,
    [report],
  );

  const evidenceItems = useMemo(
    () => safeArray(report?.evidence),
    [report],
  );

  const analyses = useMemo(
    () => safeArray(report?.analyses),
    [report],
  );

  const entities = useMemo(
    () => safeArray(report?.entities),
    [report],
  );

  const timeline = useMemo(
    () => safeArray(report?.timeline),
    [report],
  );

  const notes = useMemo(
    () => safeArray(report?.notes),
    [report],
  );

  async function handleRefresh() {
    setIsRefreshing(true);
    setErrorMessage("");

    try {
      const refreshedReport =
        await loadReport();

      setReport(refreshedReport);
    } catch (error) {
      setErrorMessage(
        getErrorMessage(
          error,
          "The report could not be refreshed.",
        ),
      );
    } finally {
      setIsRefreshing(false);
    }
  }

  function handlePrint() {
    window.print();
  }

  if (isLoading) {
    return (
      <div className="report-state">
        <span className="dashboard-loader" />

        <p>
          Assembling the final investigation report...
        </p>
      </div>
    );
  }

  if (errorMessage && !report) {
    return (
      <div className="report-state report-state-error">
        <span className="report-document-symbol">
          !
        </span>

        <h2>Report unavailable</h2>

        <p>{errorMessage}</p>

        <button
          className="forensic-secondary-action"
          type="button"
          onClick={handleRefresh}
          disabled={isRefreshing}
        >
          RETRY REPORT
        </button>
      </div>
    );
  }

  if (!report || !investigationCase) {
    return (
      <div className="report-state">
        <span className="report-document-symbol">
          RP
        </span>

        <h2>No report data available</h2>

        <p>
          The backend returned no aggregated case report.
        </p>
      </div>
    );
  }

  return (
    <div className="report-workspace">
      <section className="report-command-panel report-screen-only">
        <header className="report-panel-header">
          <span>FINAL REPORT COMMAND</span>
          <span>REGISTER TL-RP-01</span>
        </header>

        <div className="report-command-body">
          <div>
            <span>CASE FILE</span>

            <strong>
              {investigationCase.caseNumber}
            </strong>
          </div>

          <div>
            <span>REPORT GENERATED</span>

            <strong>
              {formatDate(report.generatedAt)}
            </strong>
          </div>

          <button
            className="report-refresh-button"
            type="button"
            onClick={handleRefresh}
            disabled={isRefreshing}
          >
            {isRefreshing
              ? "REFRESHING"
              : "REFRESH REPORT"}
          </button>

          <button
            className="report-print-button"
            type="button"
            onClick={handlePrint}
          >
            PRINT / SAVE AS PDF →
          </button>
        </div>
      </section>

      {errorMessage && (
        <div
          className="system-message system-message-error report-screen-only"
          role="alert"
        >
          <span>REPORT REFRESH ERROR</span>
          {errorMessage}
        </div>
      )}

      <article className="report-print-root">
        <header className="report-title-page">
          <div className="report-brand-line">
            <span>TRACELENS AI</span>
            <span>FINAL INVESTIGATION REPORT</span>
          </div>

          <p className="report-case-number">
            CASE FILE /{" "}
            {investigationCase.caseNumber}
          </p>

          <h1>{investigationCase.title}</h1>

          <p className="report-case-description">
            {investigationCase.description}
          </p>

          <div className="report-title-metadata">
            <div>
              <span>STATUS</span>
              <strong>
                {formatEnum(
                  investigationCase.status,
                )}
              </strong>
            </div>

            <div>
              <span>PRIORITY</span>
              <strong>
                {formatEnum(
                  investigationCase.priority,
                )}
              </strong>
            </div>

            <div>
              <span>INVESTIGATOR</span>
              <strong>
                {investigationCase.ownerName}
              </strong>
            </div>

            <div>
              <span>GENERATED</span>
              <strong>
                {formatDate(report.generatedAt)}
              </strong>
            </div>
          </div>
        </header>

        <section className="report-section">
          <header className="report-section-header">
            <span>01</span>
            <h2>Case Overview</h2>
          </header>

          <dl className="report-data-grid">
            <div>
              <dt>Case number</dt>
              <dd>
                {investigationCase.caseNumber}
              </dd>
            </div>

            <div>
              <dt>Status</dt>
              <dd>
                {formatEnum(
                  investigationCase.status,
                )}
              </dd>
            </div>

            <div>
              <dt>Priority</dt>
              <dd>
                {formatEnum(
                  investigationCase.priority,
                )}
              </dd>
            </div>

            <div>
              <dt>Owner</dt>
              <dd>
                {investigationCase.ownerName}
              </dd>
            </div>

            <div>
              <dt>Owner email</dt>
              <dd>
                {investigationCase.ownerEmail}
              </dd>
            </div>

            <div>
              <dt>Created</dt>
              <dd>
                {formatDate(
                  investigationCase.createdAt,
                )}
              </dd>
            </div>

            <div>
              <dt>Last updated</dt>
              <dd>
                {formatDate(
                  investigationCase.updatedAt,
                )}
              </dd>
            </div>
          </dl>
        </section>

        <section className="report-section">
          <header className="report-section-header">
            <span>02</span>
            <h2>Evidence Register</h2>

            <strong>
              {evidenceItems.length} RECORDS
            </strong>
          </header>

          {evidenceItems.length === 0 ? (
            <p className="report-empty-line">
              No evidence records were available.
            </p>
          ) : (
            <div className="report-card-list">
              {evidenceItems.map((evidence) => (
                <article
                  className="report-record-card"
                  key={evidence.id}
                >
                  <header>
                    <span>
                      EVIDENCE/{evidence.id}
                    </span>

                    <strong>
                      {evidence.originalFileName}
                    </strong>
                  </header>

                  <dl className="report-compact-grid">
                    <div>
                      <dt>Type</dt>
                      <dd>
                        {formatEnum(
                          evidence.fileType,
                        )}
                      </dd>
                    </div>

                    <div>
                      <dt>Status</dt>
                      <dd>
                        {formatEnum(
                          evidence.status,
                        )}
                      </dd>
                    </div>

                    <div>
                      <dt>Integrity</dt>
                      <dd>
                        {formatEnum(
                          evidence.integrityStatus,
                        )}
                      </dd>
                    </div>

                    <div>
                      <dt>Size</dt>
                      <dd>
                        {formatBytes(
                          evidence.fileSizeBytes,
                        )}
                      </dd>
                    </div>

                    <div>
                      <dt>Uploaded</dt>
                      <dd>
                        {formatDate(
                          evidence.uploadedAt,
                        )}
                      </dd>
                    </div>
                  </dl>

                  {evidence.description && (
                    <p>
                      {evidence.description}
                    </p>
                  )}

                  <div className="report-hash-line">
                    <span>SHA-256</span>
                    <code>
                      {evidence.sha256Hash ||
                        "Not available"}
                    </code>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="report-section">
          <header className="report-section-header">
            <span>03</span>
            <h2>AI Evidence Analyses</h2>

            <strong>
              {analyses.length} RECORDS
            </strong>
          </header>

          {analyses.length === 0 ? (
            <p className="report-empty-line">
              No saved AI analyses were available.
            </p>
          ) : (
            <div className="report-card-list">
              {analyses.map((analysis) => (
                <article
                  className="report-record-card report-analysis-card"
                  key={analysis.analysisId}
                >
                  <header>
                    <span>
                      ANALYSIS/
                      {analysis.analysisId}
                    </span>

                    <strong>
                      {analysis.originalFileName}
                    </strong>

                    <div>
                      <span
                        className={`report-badge report-risk-${String(
                          analysis.riskLevel ||
                            "unknown",
                        ).toLowerCase()}`}
                      >
                        {formatEnum(
                          analysis.riskLevel,
                        )}{" "}
                        RISK
                      </span>

                      <span className="report-badge">
                        {formatEnum(
                          analysis.status,
                        )}
                      </span>
                    </div>
                  </header>

                  {analysis.status ===
                  "COMPLETED" ? (
                    <>
                      <div className="report-analysis-summary">
                        <h3>Factual Summary</h3>
                        <p>{analysis.summary}</p>
                      </div>

                      <div className="report-three-column">
                        <div>
                          <h3>
                            Suspicious Findings
                          </h3>

                          <ReportList
                            items={safeArray(
                              analysis.suspiciousFindings,
                            )}
                            emptyMessage="No suspicious findings were recorded."
                          />
                        </div>

                        <div>
                          <h3>
                            Verification Actions
                          </h3>

                          <ReportList
                            items={safeArray(
                              analysis.recommendedActions,
                            )}
                            emptyMessage="No verification actions were recorded."
                          />
                        </div>

                        <div>
                          <h3>Limitations</h3>

                          <ReportList
                            items={safeArray(
                              analysis.limitations,
                            )}
                            emptyMessage="No limitations were recorded."
                          />
                        </div>
                      </div>
                    </>
                  ) : (
                    <p className="report-failure-line">
                      {analysis.failureMessage ||
                        "This analysis did not complete successfully."}
                    </p>
                  )}

                  <dl className="report-compact-grid">
                    <div>
                      <dt>Provider</dt>
                      <dd>
                        {analysis.provider ||
                          "Not available"}
                      </dd>
                    </div>

                    <div>
                      <dt>Model</dt>
                      <dd>
                        {analysis.model ||
                          "Not available"}
                      </dd>
                    </div>

                    <div>
                      <dt>Human review</dt>
                      <dd>
                        {analysis.humanReviewRequired
                          ? "Required"
                          : "Not reported"}
                      </dd>
                    </div>

                    <div>
                      <dt>Completed</dt>
                      <dd>
                        {formatDate(
                          analysis.completedAt,
                        )}
                      </dd>
                    </div>
                  </dl>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="report-section">
          <header className="report-section-header">
            <span>04</span>
            <h2>Extracted Entities</h2>

            <strong>
              {entities.length} RECORDS
            </strong>
          </header>

          {entities.length === 0 ? (
            <p className="report-empty-line">
              No extracted entities were available.
            </p>
          ) : (
            <div className="report-entity-grid">
              {entities.map((entity) => (
                <article
                  className="report-entity-card"
                  key={entity.entityId}
                >
                  <span>
                    {formatEnum(entity.entityType)}
                  </span>

                  <h3>{entity.displayValue}</h3>

                  <p>
                    {entity.contextSnippet ||
                      "No stored evidence context."}
                  </p>

                  <div>
                    <span>
                      OCCURRENCES:{" "}
                      {entity.occurrenceCount ?? 0}
                    </span>

                    <span>
                      CONFIDENCE:{" "}
                      {formatConfidence(
                        entity.confidence,
                      )}
                    </span>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="report-section">
          <header className="report-section-header">
            <span>05</span>
            <h2>Investigation Timeline</h2>

            <strong>
              {timeline.length} EVENTS
            </strong>
          </header>

          {timeline.length === 0 ? (
            <p className="report-empty-line">
              No timeline events were available.
            </p>
          ) : (
            <div className="report-timeline">
              {timeline.map((event, index) => (
                <article
                  className="report-timeline-event"
                  key={event.eventId}
                >
                  <div>
                    {String(
                      event.sequenceNumber ??
                        index + 1,
                    ).padStart(2, "0")}
                  </div>

                  <section>
                    <header>
                      <div>
                        <span>
                          EVENT/{event.eventId}
                        </span>

                        <h3>{event.title}</h3>
                      </div>

                      <div>
                        <span className="report-badge">
                          {formatEnum(
                            event.certainty,
                          )}
                        </span>

                        <span className="report-badge">
                          {formatEnum(
                            event.temporalPrecision,
                          )}
                        </span>
                      </div>
                    </header>

                    <dl className="report-compact-grid">
                      <div>
                        <dt>
                          Normalised date/time
                        </dt>

                        <dd>
                          {formatDate(
                            event.normalizedDateTime,
                          )}
                        </dd>
                      </div>

                      <div>
                        <dt>
                          Original expression
                        </dt>

                        <dd>
                          {event.temporalExpression ||
                            "Not available"}
                        </dd>
                      </div>
                    </dl>

                    <p>{event.description}</p>

                    {event.contextSnippet && (
                      <blockquote>
                        {event.contextSnippet}
                      </blockquote>
                    )}

                    {safeArray(
                      event.involvedEntities,
                    ).length > 0 && (
                      <div className="report-involved-entities">
                        {safeArray(
                          event.involvedEntities,
                        ).map((entity) => (
                          <span
                            key={entity.entityId}
                          >
                            {formatEnum(
                              entity.entityType,
                            )}
                            : {entity.displayValue}
                          </span>
                        ))}
                      </div>
                    )}
                  </section>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="report-section">
          <header className="report-section-header">
            <span>06</span>
            <h2>Investigator Notes</h2>

            <strong>
              {notes.length} NOTES
            </strong>
          </header>

          {notes.length === 0 ? (
            <p className="report-empty-line">
              No investigator notes were available.
            </p>
          ) : (
            <div className="report-notes-list">
              {notes.map((note) => {
                const noteId =
                  note.id ?? note.noteId;

                return (
                  <article
                    className={
                      note.pinned
                        ? "report-note report-note-pinned"
                        : "report-note"
                    }
                    key={noteId}
                  >
                    <header>
                      <span>NOTE/{noteId}</span>

                      <strong>
                        {note.authorName ||
                          "Authenticated investigator"}
                      </strong>

                      {note.pinned && (
                        <span className="report-badge">
                          PINNED
                        </span>
                      )}
                    </header>

                    <p>{note.content}</p>

                    <footer>
                      Created{" "}
                      {formatDate(note.createdAt)}
                      {" · "}
                      Updated{" "}
                      {formatDate(note.updatedAt)}
                    </footer>
                  </article>
                );
              })}
            </div>
          )}
        </section>

        <footer className="report-final-notice">
          <span>
            MANDATORY INDEPENDENT VERIFICATION
          </span>

          <p>
            {report.disclaimer ||
              "AI-generated findings are investigative aids and must be independently verified."}
          </p>

          <small>
            Report generated by TraceLens AI on{" "}
            {formatDate(report.generatedAt)}.
          </small>
        </footer>
      </article>
    </div>
  );
}