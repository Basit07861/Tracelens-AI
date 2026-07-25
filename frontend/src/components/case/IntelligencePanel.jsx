import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";

import api from "../../api/client";
import "./IntelligencePanel.css";

const ENTITY_TYPES = [
  "DATE",
  "DATE_TIME",
  "EMAIL_ADDRESS",
  "IP_ADDRESS",
  "MONEY",
  "ORGANIZATION",
  "PERSON",
  "PHONE_NUMBER",
  "TIME",
  "URL",
];

const TIMELINE_CERTAINTIES = [
  "OBSERVED",
  "INFERRED",
  "UNKNOWN",
];

const TIMELINE_PRECISIONS = [
  "DATE_TIME",
  "DATE",
  "TIME",
  "RANGE",
  "UNKNOWN",
];

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

function formatTimelineDate(value) {
  if (!value) {
    return "Unresolved date or time";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
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

function getCollection(responseData) {
  if (Array.isArray(responseData)) {
    return responseData;
  }

  if (Array.isArray(responseData?.content)) {
    return responseData.content;
  }

  if (Array.isArray(responseData?.items)) {
    return responseData.items;
  }

  if (Array.isArray(responseData?.runs)) {
    return responseData.runs;
  }

  return [];
}

function IntelligenceHistory({
  historyItems,
  selectedRunId,
  activeOperation,
  onOpenRun,
}) {
  return (
    <section className="intelligence-history-panel">
      <header className="intelligence-panel-header">
        <span>INTELLIGENCE RUN HISTORY</span>

        <span>
          {String(historyItems.length).padStart(
            2,
            "0",
          )}{" "}
          RECORDS
        </span>
      </header>

      {historyItems.length === 0 ? (
        <div className="intelligence-empty-small">
          No intelligence runs have been saved for
          this evidence.
        </div>
      ) : (
        <div className="intelligence-history-list">
          {historyItems.map((item) => (
            <button
              type="button"
              key={item.runId}
              onClick={() => onOpenRun(item)}
              disabled={Boolean(activeOperation)}
              className={
                Number(selectedRunId) ===
                Number(item.runId)
                  ? "intelligence-history-record intelligence-history-record-active"
                  : "intelligence-history-record"
              }
            >
              <span>RUN/{item.runId}</span>

              <strong>
                {formatEnum(item.status)}
              </strong>

              <span>
                {formatEnum(item.method)}
              </span>

              <span>
                {item.entityCount ?? 0} ENTITIES
              </span>

              <span>
                {item.timelineEventCount ?? 0} EVENTS
              </span>

              <span>
                {formatDate(
                  item.completedAt ||
                    item.requestedAt,
                )}
              </span>
            </button>
          ))}
        </div>
      )}
    </section>
  );
}

function EntityWorkspace({
  entities,
  entityType,
  onEntityTypeChange,
}) {
  const groupedEntities = useMemo(() => {
    return entities.reduce((groups, entity) => {
      const groupName =
        entity.entityType || "UNKNOWN";

      if (!groups[groupName]) {
        groups[groupName] = [];
      }

      groups[groupName].push(entity);

      return groups;
    }, {});
  }, [entities]);

  return (
    <>
      <section className="intelligence-filter-panel">
        <div>
          <label htmlFor="entity-type-filter">
            Entity classification
          </label>

          <select
            id="entity-type-filter"
            value={entityType}
            onChange={onEntityTypeChange}
          >
            <option value="">
              ALL ENTITY TYPES
            </option>

            {ENTITY_TYPES.map((type) => (
              <option value={type} key={type}>
                {formatEnum(type)}
              </option>
            ))}
          </select>
        </div>

        <span>
          {entities.length} MATCHING ENTITY RECORDS
        </span>
      </section>

      {entities.length === 0 && (
        <div className="intelligence-state">
          <span className="intelligence-scan-symbol">
            EN
          </span>

          <h2>No matching entities</h2>

          <p>
            The selected intelligence run did not
            contain entities matching this
            classification.
          </p>
        </div>
      )}

      {Object.entries(groupedEntities).map(
        ([groupName, groupItems]) => (
          <section
            className="entity-group-panel"
            key={groupName}
          >
            <header className="intelligence-panel-header">
              <span>{formatEnum(groupName)}</span>

              <span>
                {String(groupItems.length).padStart(
                  2,
                  "0",
                )}{" "}
                RECORDS
              </span>
            </header>

            <div className="entity-card-grid">
              {groupItems.map((entity) => (
                <article
                  className="entity-record-card"
                  key={entity.entityId}
                >
                  <header>
                    <span>
                      ENTITY/{entity.entityId}
                    </span>

                    <span>
                      {formatConfidence(
                        entity.confidence,
                      )}
                    </span>
                  </header>

                  <div className="entity-record-value">
                    {entity.displayValue}
                  </div>

                  <dl>
                    <div>
                      <dt>NORMALISED VALUE</dt>

                      <dd>
                        {entity.normalizedValue ||
                          "Not available"}
                      </dd>
                    </div>

                    <div>
                      <dt>OCCURRENCES</dt>

                      <dd>
                        {entity.occurrenceCount ?? 0}
                      </dd>
                    </div>

                    <div>
                      <dt>FIRST OFFSET</dt>

                      <dd>
                        {entity.firstCharacterOffset ??
                          "Not available"}
                      </dd>
                    </div>

                    <div>
                      <dt>LAST OFFSET</dt>

                      <dd>
                        {entity.lastCharacterOffset ??
                          "Not available"}
                      </dd>
                    </div>
                  </dl>

                  <p className="entity-context">
                    {entity.contextSnippet ||
                      "No surrounding evidence context was stored."}
                  </p>
                </article>
              ))}
            </div>
          </section>
        ),
      )}
    </>
  );
}

function TimelineWorkspace({
  timelineEvents,
  certainty,
  onCertaintyChange,
  temporalPrecision,
  onTemporalPrecisionChange,
}) {
  return (
    <>
      <section className="intelligence-filter-panel timeline-filter-panel">
        <div>
          <label htmlFor="timeline-certainty">
            Event certainty
          </label>

          <select
            id="timeline-certainty"
            value={certainty}
            onChange={onCertaintyChange}
          >
            <option value="">
              ALL CERTAINTY LEVELS
            </option>

            {TIMELINE_CERTAINTIES.map(
              (value) => (
                <option
                  value={value}
                  key={value}
                >
                  {formatEnum(value)}
                </option>
              ),
            )}
          </select>
        </div>

        <div>
          <label htmlFor="timeline-precision">
            Temporal precision
          </label>

          <select
            id="timeline-precision"
            value={temporalPrecision}
            onChange={
              onTemporalPrecisionChange
            }
          >
            <option value="">
              ALL TEMPORAL PRECISIONS
            </option>

            {TIMELINE_PRECISIONS.map(
              (value) => (
                <option
                  value={value}
                  key={value}
                >
                  {formatEnum(value)}
                </option>
              ),
            )}
          </select>
        </div>

        <span>
          {timelineEvents.length} MATCHING EVENTS
        </span>
      </section>

      {timelineEvents.length === 0 && (
        <div className="intelligence-state">
          <span className="intelligence-scan-symbol">
            TL
          </span>

          <h2>No matching timeline events</h2>

          <p>
            The selected intelligence run did not
            contain events matching these filters.
          </p>
        </div>
      )}

      {timelineEvents.length > 0 && (
        <section className="timeline-register-panel">
          <header className="intelligence-panel-header">
            <span>
              CHRONOLOGICAL EVENT REGISTER
            </span>

            <span>
              {String(
                timelineEvents.length,
              ).padStart(2, "0")}{" "}
              EVENTS
            </span>
          </header>

          <div className="timeline-event-list">
            {timelineEvents.map(
              (event, index) => (
                <article
                  className="timeline-event-record"
                  key={event.eventId}
                >
                  <div className="timeline-event-marker">
                    <span>
                      {String(
                        event.sequenceNumber ??
                          index + 1,
                      ).padStart(2, "0")}
                    </span>
                  </div>

                  <div className="timeline-event-content">
                    <header>
                      <div>
                        <span>
                          EVENT/{event.eventId}
                        </span>

                        <h3>{event.title}</h3>
                      </div>

                      <div className="timeline-event-badges">
                        <span
                          className={`timeline-certainty timeline-certainty-${String(
                            event.certainty ||
                              "unknown",
                          ).toLowerCase()}`}
                        >
                          {formatEnum(
                            event.certainty,
                          )}
                        </span>

                        <span className="timeline-precision">
                          {formatEnum(
                            event.temporalPrecision,
                          )}
                        </span>
                      </div>
                    </header>

                    <div className="timeline-date-register">
                      <div>
                        <span>
                          NORMALISED DATE/TIME
                        </span>

                        <strong>
                          {formatTimelineDate(
                            event.normalizedDateTime,
                          )}
                        </strong>
                      </div>

                      <div>
                        <span>
                          ORIGINAL EXPRESSION
                        </span>

                        <strong>
                          {event.temporalExpression ||
                            "Not available"}
                        </strong>
                      </div>
                    </div>

                    <p className="timeline-description">
                      {event.description}
                    </p>

                    {event.contextSnippet && (
                      <p className="timeline-context">
                        {event.contextSnippet}
                      </p>
                    )}

                    {Array.isArray(
                      event.involvedEntities,
                    ) &&
                      event.involvedEntities.length >
                        0 && (
                        <div className="timeline-entities">
                          <span>
                            INVOLVED ENTITIES
                          </span>

                          <div>
                            {event.involvedEntities.map(
                              (entity) => (
                                <span
                                  key={
                                    entity.entityId
                                  }
                                >
                                  {formatEnum(
                                    entity.entityType,
                                  )}
                                  :{" "}
                                  {
                                    entity.displayValue
                                  }
                                </span>
                              ),
                            )}
                          </div>
                        </div>
                      )}
                  </div>
                </article>
              ),
            )}
          </div>
        </section>
      )}
    </>
  );
}

export default function IntelligencePanel({
  caseId,
  view,
}) {
  const [evidenceItems, setEvidenceItems] =
    useState([]);

  const [
    selectedEvidenceId,
    setSelectedEvidenceId,
  ] = useState("");

  const [run, setRun] = useState(null);

  const [historyItems, setHistoryItems] =
    useState([]);

  const [entityType, setEntityType] =
    useState("");

  const [certainty, setCertainty] =
    useState("");

  const [
    temporalPrecision,
    setTemporalPrecision,
  ] = useState("");

  const [
    isLoadingEvidence,
    setIsLoadingEvidence,
  ] = useState(true);

  const [isLoadingRun, setIsLoadingRun] =
    useState(false);

  const [activeOperation, setActiveOperation] =
    useState("");

  const [errorMessage, setErrorMessage] =
    useState("");

  const [successMessage, setSuccessMessage] =
    useState("");

  const processedEvidence = useMemo(
    () =>
      evidenceItems.filter(
        (item) => item.status === "PROCESSED",
      ),
    [evidenceItems],
  );

  const selectedEvidence = useMemo(
    () =>
      processedEvidence.find(
        (item) =>
          String(item.id) ===
          String(selectedEvidenceId),
      ) || null,
    [processedEvidence, selectedEvidenceId],
  );

  const allEntities = useMemo(() => {
    if (!Array.isArray(run?.entities)) {
      return [];
    }

    return run.entities;
  }, [run]);

  const filteredEntities = useMemo(() => {
    if (!entityType) {
      return allEntities;
    }

    return allEntities.filter(
      (entity) =>
        entity.entityType === entityType,
    );
  }, [allEntities, entityType]);

  const allTimelineEvents = useMemo(() => {
    if (!Array.isArray(run?.timelineEvents)) {
      return [];
    }

    return [...run.timelineEvents].sort(
      (firstEvent, secondEvent) => {
        const firstValue =
          firstEvent.normalizedDateTime;

        const secondValue =
          secondEvent.normalizedDateTime;

        if (!firstValue && !secondValue) {
          return (
            (firstEvent.sequenceNumber ?? 0) -
            (secondEvent.sequenceNumber ?? 0)
          );
        }

        if (!firstValue) {
          return 1;
        }

        if (!secondValue) {
          return -1;
        }

        const firstTime = new Date(
          firstValue,
        ).getTime();

        const secondTime = new Date(
          secondValue,
        ).getTime();

        if (
          Number.isNaN(firstTime) ||
          Number.isNaN(secondTime)
        ) {
          return (
            (firstEvent.sequenceNumber ?? 0) -
            (secondEvent.sequenceNumber ?? 0)
          );
        }

        return firstTime - secondTime;
      },
    );
  }, [run]);

  const filteredTimelineEvents = useMemo(
    () =>
      allTimelineEvents.filter((event) => {
        const certaintyMatches =
          !certainty ||
          event.certainty === certainty;

        const precisionMatches =
          !temporalPrecision ||
          event.temporalPrecision ===
            temporalPrecision;

        return (
          certaintyMatches &&
          precisionMatches
        );
      }),
    [
      allTimelineEvents,
      certainty,
      temporalPrecision,
    ],
  );

  const loadEvidence = useCallback(
    async (signal) => {
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

      return getCollection(
        response.data?.data,
      );
    },
    [caseId],
  );

  const loadLatestRun = useCallback(
    async (evidenceId, signal) => {
      try {
        const response = await api.get(
          `/api/intelligence/evidence/${evidenceId}/runs/latest`,
          {
            signal,
          },
        );

        return response.data?.data || null;
      } catch (error) {
        if (error.response?.status === 404) {
          return null;
        }

        throw error;
      }
    },
    [],
  );

  const loadHistory = useCallback(
    async (evidenceId, signal) => {
      const response = await api.get(
        `/api/intelligence/evidence/${evidenceId}/runs`,
        {
          signal,
          params: {
            page: 0,
            size: 10,
          },
        },
      );

      return getCollection(
        response.data?.data,
      );
    },
    [],
  );

  useEffect(() => {
    const controller = new AbortController();

    async function retrieveEvidence() {
      try {
        const items = await loadEvidence(
          controller.signal,
        );

        if (controller.signal.aborted) {
          return;
        }

        const firstProcessedEvidence =
          items.find(
            (item) =>
              item.status === "PROCESSED",
          );

        setEvidenceItems(items);

        setSelectedEvidenceId(
          firstProcessedEvidence
            ? String(
                firstProcessedEvidence.id,
              )
            : "",
        );

        setIsLoadingRun(
          Boolean(firstProcessedEvidence),
        );
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
          setIsLoadingEvidence(false);
        }
      }
    }

    retrieveEvidence();

    return () => controller.abort();
  }, [loadEvidence]);

  useEffect(() => {
    if (!selectedEvidenceId) {
      return undefined;
    }

    const controller = new AbortController();

    async function retrieveRunWorkspace() {
      try {
        const [latestRun, runHistory] =
          await Promise.all([
            loadLatestRun(
              selectedEvidenceId,
              controller.signal,
            ),
            loadHistory(
              selectedEvidenceId,
              controller.signal,
            ),
          ]);

        if (controller.signal.aborted) {
          return;
        }

        setRun(latestRun);
        setHistoryItems(runHistory);
      } catch (error) {
        if (
          error.name !== "CanceledError" &&
          error.code !== "ERR_CANCELED" &&
          !controller.signal.aborted
        ) {
          setErrorMessage(
            getErrorMessage(
              error,
              "Intelligence records could not be retrieved.",
            ),
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoadingRun(false);
        }
      }
    }

    retrieveRunWorkspace();

    return () => controller.abort();
  }, [
    selectedEvidenceId,
    loadLatestRun,
    loadHistory,
  ]);

  function handleEvidenceChange(event) {
    const nextEvidenceId =
      event.target.value;

    setSelectedEvidenceId(nextEvidenceId);
    setRun(null);
    setHistoryItems([]);
    setEntityType("");
    setCertainty("");
    setTemporalPrecision("");
    setErrorMessage("");
    setSuccessMessage("");
    setIsLoadingRun(Boolean(nextEvidenceId));
  }

  function handleEntityTypeChange(event) {
    setEntityType(event.target.value);
    setErrorMessage("");
  }

  function handleCertaintyChange(event) {
    setCertainty(event.target.value);
    setErrorMessage("");
  }

  function handleTemporalPrecisionChange(
    event,
  ) {
    setTemporalPrecision(event.target.value);
    setErrorMessage("");
  }

  async function refreshHistory(
    evidenceId,
  ) {
    const items = await loadHistory(evidenceId);
    setHistoryItems(items);
  }

  async function handleGenerateIntelligence() {
    if (!selectedEvidence) {
      setErrorMessage(
        "Select a processed evidence file.",
      );

      return;
    }

    const hasPreviousRun =
      Boolean(run) || historyItems.length > 0;

    const endpoint = hasPreviousRun
      ? `/api/intelligence/evidence/${selectedEvidence.id}/runs/regenerate`
      : `/api/intelligence/evidence/${selectedEvidence.id}/runs`;

    setActiveOperation(
      hasPreviousRun
        ? "regenerate"
        : "generate",
    );

    setErrorMessage("");
    setSuccessMessage("");

    try {
      const response = await api.post(
        endpoint,
        undefined,
        {
          timeout: 120000,
        },
      );

      const generatedRun =
        response.data?.data || null;

      setRun(generatedRun);

      setEntityType("");
      setCertainty("");
      setTemporalPrecision("");

      await refreshHistory(
        selectedEvidence.id,
      );

      setSuccessMessage(
        hasPreviousRun
          ? "A new intelligence run was generated. Previous runs remain preserved."
          : "The first entity and timeline intelligence run completed successfully.",
      );
    } catch (error) {
      setErrorMessage(
        getErrorMessage(
          error,
          "The intelligence run could not be generated.",
        ),
      );
    } finally {
      setActiveOperation("");
    }
  }

  async function handleOpenHistoryItem(
    historyItem,
  ) {
    if (!historyItem?.runId) {
      return;
    }

    setActiveOperation(
      `history-${historyItem.runId}`,
    );

    setErrorMessage("");
    setSuccessMessage("");

    try {
      const response = await api.get(
        `/api/intelligence/runs/${historyItem.runId}`,
      );

      setRun(response.data?.data || null);

      setEntityType("");
      setCertainty("");
      setTemporalPrecision("");

      setSuccessMessage(
        `Intelligence run ${historyItem.runId} was opened from history.`,
      );
    } catch (error) {
      setErrorMessage(
        getErrorMessage(
          error,
          "The selected intelligence run could not be retrieved.",
        ),
      );
    } finally {
      setActiveOperation("");
    }
  }

  if (isLoadingEvidence) {
    return (
      <div className="intelligence-state">
        <span className="dashboard-loader" />

        <p>
          Retrieving processed evidence records...
        </p>
      </div>
    );
  }

  if (processedEvidence.length === 0) {
    return (
      <div className="intelligence-state">
        <span className="intelligence-scan-symbol">
          IX
        </span>

        <h2>No processed evidence available</h2>

        <p>
          Upload evidence and extract its text
          before generating entities and a
          timeline.
        </p>
      </div>
    );
  }

  return (
    <div className="intelligence-workspace">
      <section className="intelligence-command-panel">
        <header className="intelligence-panel-header">
          <span>
            INTELLIGENCE EXTRACTION COMMAND
          </span>

          <span>REGISTER TL-IX-01</span>
        </header>

        <div className="intelligence-command-body">
          <div className="intelligence-evidence-selector">
            <div>
              <label
                htmlFor={`intelligence-${view}`}
              >
                Processed evidence
              </label>

              <span>
                {processedEvidence.length} AVAILABLE
              </span>
            </div>

            <select
              id={`intelligence-${view}`}
              value={selectedEvidenceId}
              onChange={handleEvidenceChange}
              disabled={Boolean(activeOperation)}
            >
              {processedEvidence.map((item) => (
                <option
                  value={item.id}
                  key={item.id}
                >
                  {item.originalFileName} ·
                  Evidence/{item.id}
                </option>
              ))}
            </select>
          </div>

          <div className="intelligence-selected-evidence">
            <div>
              <span>SELECTED FILE</span>

              <strong>
                {selectedEvidence?.originalFileName}
              </strong>
            </div>

            <div>
              <span>FILE TYPE</span>

              <strong>
                {formatEnum(
                  selectedEvidence?.fileType,
                )}
              </strong>
            </div>

            <div>
              <span>PROCESSING</span>

              <strong>
                {formatEnum(
                  selectedEvidence?.status,
                )}
              </strong>
            </div>

            <div>
              <span>INTEGRITY</span>

              <strong>
                {formatEnum(
                  selectedEvidence?.integrityStatus,
                )}
              </strong>
            </div>
          </div>

          <button
            className="intelligence-generate-button"
            type="button"
            onClick={
              handleGenerateIntelligence
            }
            disabled={
              Boolean(activeOperation) ||
              isLoadingRun
            }
          >
            <span>
              {activeOperation === "generate"
                ? "GENERATING INTELLIGENCE"
                : activeOperation ===
                    "regenerate"
                  ? "REGENERATING INTELLIGENCE"
                  : run ||
                      historyItems.length > 0
                    ? "REGENERATE INTELLIGENCE"
                    : "GENERATE INTELLIGENCE"}
            </span>

            <span aria-hidden="true">→</span>
          </button>
        </div>
      </section>

      {successMessage && (
        <div
          className="system-message system-message-success"
          role="status"
        >
          <span>
            INTELLIGENCE OPERATION COMPLETE
          </span>

          {successMessage}
        </div>
      )}

      {errorMessage && (
        <div
          className="system-message system-message-error"
          role="alert"
        >
          <span>INTELLIGENCE ERROR</span>

          {errorMessage}
        </div>
      )}

      {isLoadingRun && (
        <div className="intelligence-state">
          <span className="dashboard-loader" />

          <p>
            Retrieving the latest intelligence
            run...
          </p>
        </div>
      )}

      {!isLoadingRun && !run && (
        <div className="intelligence-state">
          <span className="intelligence-scan-symbol">
            IX
          </span>

          <h2>No saved intelligence run</h2>

          <p>
            Generate the first hybrid intelligence
            run to extract contextual entities and
            reconstruct timeline events.
          </p>
        </div>
      )}

      {!isLoadingRun && run && (
        <>
          <section className="intelligence-run-panel">
            <header className="intelligence-run-header">
              <div>
                <span>
                  INTELLIGENCE RUN/{run.runId}
                </span>

                <h2>
                  {run.originalFileName ||
                    selectedEvidence?.originalFileName}
                </h2>
              </div>

              <div className="intelligence-run-badges">
                <span
                  className={`intelligence-status intelligence-status-${String(
                    run.status || "unknown",
                  ).toLowerCase()}`}
                >
                  {formatEnum(run.status)}
                </span>

                <span className="intelligence-method">
                  {formatEnum(run.method)}
                </span>
              </div>
            </header>

            {run.status === "FAILED" && (
              <div className="intelligence-failure">
                <span>SAFE FAILURE RECORD</span>

                <p>
                  {run.failureMessage ||
                    "The intelligence extraction run failed."}
                </p>
              </div>
            )}

            <dl className="intelligence-run-metadata">
              <div>
                <dt>ENTITIES</dt>
                <dd>{run.entityCount ?? 0}</dd>
              </div>

              <div>
                <dt>TIMELINE EVENTS</dt>

                <dd>
                  {run.timelineEventCount ?? 0}
                </dd>
              </div>

              <div>
                <dt>SOURCE ANALYSIS</dt>

                <dd>
                  {run.sourceAnalysisId
                    ? `Analysis/${run.sourceAnalysisId}`
                    : "Not linked"}
                </dd>
              </div>

              <div>
                <dt>HUMAN REVIEW</dt>

                <dd>
                  {run.humanReviewRequired
                    ? "REQUIRED"
                    : "NOT REPORTED"}
                </dd>
              </div>

              <div>
                <dt>PROVIDER</dt>

                <dd>
                  {run.provider ||
                    "Not available"}
                </dd>
              </div>

              <div>
                <dt>MODEL</dt>

                <dd>
                  {run.model || "Not available"}
                </dd>
              </div>

              <div>
                <dt>REQUESTED</dt>

                <dd>
                  {formatDate(run.requestedAt)}
                </dd>
              </div>

              <div>
                <dt>COMPLETED</dt>

                <dd>
                  {formatDate(run.completedAt)}
                </dd>
              </div>
            </dl>
          </section>

          {run.status === "COMPLETED" &&
            view === "entities" && (
              <EntityWorkspace
                entities={filteredEntities}
                entityType={entityType}
                onEntityTypeChange={
                  handleEntityTypeChange
                }
              />
            )}

          {run.status === "COMPLETED" &&
            view === "timeline" && (
              <TimelineWorkspace
                timelineEvents={
                  filteredTimelineEvents
                }
                certainty={certainty}
                onCertaintyChange={
                  handleCertaintyChange
                }
                temporalPrecision={
                  temporalPrecision
                }
                onTemporalPrecisionChange={
                  handleTemporalPrecisionChange
                }
              />
            )}

          <div className="intelligence-review-notice">
            <span>
              MANDATORY REVIEW NOTICE
            </span>

            <p>
              Extracted entities and reconstructed
              timeline events are investigative aids.
              Their identity, context, dates,
              relationships and conclusions must be
              independently verified.
            </p>
          </div>
        </>
      )}

      <IntelligenceHistory
        historyItems={historyItems}
        selectedRunId={run?.runId}
        activeOperation={activeOperation}
        onOpenRun={handleOpenHistoryItem}
      />
    </div>
  );
}