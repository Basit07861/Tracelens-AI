import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";

import api from "../../api/client";
import "./AnalysisPanel.css";

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

function getEvidenceItems(responseData) {
  if (Array.isArray(responseData)) {
    return responseData;
  }

  if (Array.isArray(responseData?.content)) {
    return responseData.content;
  }

  return [];
}

function getHistoryItems(responseData) {
  if (Array.isArray(responseData)) {
    return responseData;
  }

  if (Array.isArray(responseData?.content)) {
    return responseData.content;
  }

  if (Array.isArray(responseData?.analyses)) {
    return responseData.analyses;
  }

  if (Array.isArray(responseData?.items)) {
    return responseData.items;
  }

  return [];
}

function shortenHash(value) {
  if (!value) {
    return "Not available";
  }

  if (value.length <= 30) {
    return value;
  }

  return `${value.slice(0, 14)}…${value.slice(-14)}`;
}

function renderBoolean(value) {
  if (value === true) {
    return "YES";
  }

  if (value === false) {
    return "NO";
  }

  return "NOT AVAILABLE";
}

function AnalysisList({
  title,
  registerCode,
  items,
  emptyText,
}) {
  return (
    <section className="analysis-list-panel">
      <header className="analysis-panel-header">
        <span>{title}</span>
        <span>{registerCode}</span>
      </header>

      {items.length === 0 ? (
        <div className="analysis-small-empty">
          {emptyText}
        </div>
      ) : (
        <ol className="analysis-numbered-list">
          {items.map((item, index) => (
            <li key={`${title}-${index}`}>
              <span>
                {String(index + 1).padStart(2, "0")}
              </span>

              <p>{item}</p>
            </li>
          ))}
        </ol>
      )}
    </section>
  );
}

export default function AnalysisPanel({ caseId }) {
  const [evidenceItems, setEvidenceItems] = useState([]);

  const [
    selectedEvidenceId,
    setSelectedEvidenceId,
  ] = useState("");

  const [analysis, setAnalysis] = useState(null);
  const [historyItems, setHistoryItems] = useState([]);

  const [
    isLoadingEvidence,
    setIsLoadingEvidence,
  ] = useState(true);

  const [
    isLoadingAnalysis,
    setIsLoadingAnalysis,
  ] = useState(true);

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

      return getEvidenceItems(response.data?.data);
    },
    [caseId],
  );

  const loadAnalysisHistory = useCallback(
    async (evidenceId, signal) => {
      const response = await api.get(
        `/api/ai/evidence/${evidenceId}/analyses`,
        {
          signal,
          params: {
            page: 0,
            size: 10,
          },
        },
      );

      return getHistoryItems(response.data?.data);
    },
    [],
  );

  const loadLatestAnalysis = useCallback(
    async (evidenceId, signal) => {
      try {
        const response = await api.get(
          `/api/ai/evidence/${evidenceId}/analyses/latest`,
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

        setEvidenceItems(items);

        const firstProcessedEvidence = items.find(
          (item) => item.status === "PROCESSED",
        );

        if (firstProcessedEvidence) {
          setSelectedEvidenceId(
            String(firstProcessedEvidence.id),
          );

          setIsLoadingAnalysis(true);
        } else {
          setIsLoadingAnalysis(false);
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

          setIsLoadingAnalysis(false);
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

    async function retrieveAnalysisWorkspace() {
      try {
        const [latestResult, historyResult] =
          await Promise.all([
            loadLatestAnalysis(
              selectedEvidenceId,
              controller.signal,
            ),
            loadAnalysisHistory(
              selectedEvidenceId,
              controller.signal,
            ),
          ]);

        if (controller.signal.aborted) {
          return;
        }

        setAnalysis(latestResult);
        setHistoryItems(historyResult);
      } catch (error) {
        if (
          error.name !== "CanceledError" &&
          error.code !== "ERR_CANCELED" &&
          !controller.signal.aborted
        ) {
          setErrorMessage(
            getErrorMessage(
              error,
              "AI analysis records could not be retrieved.",
            ),
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoadingAnalysis(false);
        }
      }
    }

    retrieveAnalysisWorkspace();

    return () => controller.abort();
  }, [
    selectedEvidenceId,
    loadLatestAnalysis,
    loadAnalysisHistory,
  ]);

  async function refreshHistory(evidenceId) {
    const items = await loadAnalysisHistory(evidenceId);
    setHistoryItems(items);
  }

  function handleEvidenceChange(event) {
    setSelectedEvidenceId(event.target.value);
    setAnalysis(null);
    setHistoryItems([]);
    setErrorMessage("");
    setSuccessMessage("");
    setIsLoadingAnalysis(true);
  }

  async function handleGenerateAnalysis() {
    if (!selectedEvidence) {
      setErrorMessage(
        "Select a processed evidence file.",
      );

      return;
    }

    const hasPreviousAnalysis =
      Boolean(analysis) || historyItems.length > 0;

    const endpoint = hasPreviousAnalysis
      ? `/api/ai/evidence/${selectedEvidence.id}/analyses/regenerate`
      : `/api/ai/evidence/${selectedEvidence.id}/analyses`;

    setActiveOperation(
      hasPreviousAnalysis
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

      const generatedAnalysis =
        response.data?.data || null;

      setAnalysis(generatedAnalysis);

      await refreshHistory(selectedEvidence.id);

      setSuccessMessage(
        hasPreviousAnalysis
          ? "A new AI analysis was generated. Previous analysis records remain preserved."
          : "The first persistent AI evidence analysis was generated successfully.",
      );
    } catch (error) {
      setErrorMessage(
        getErrorMessage(
          error,
          "The AI analysis could not be generated.",
        ),
      );
    } finally {
      setActiveOperation("");
    }
  }

  async function handleOpenHistoryItem(historyItem) {
    if (!historyItem?.analysisId) {
      return;
    }

    setActiveOperation(
      `history-${historyItem.analysisId}`,
    );

    setErrorMessage("");
    setSuccessMessage("");

    try {
      const response = await api.get(
        `/api/ai/analyses/${historyItem.analysisId}`,
      );

      setAnalysis(response.data?.data || null);

      setSuccessMessage(
        `Analysis record ${historyItem.analysisId} was opened from history.`,
      );
    } catch (error) {
      setErrorMessage(
        getErrorMessage(
          error,
          "The selected analysis record could not be retrieved.",
        ),
      );
    } finally {
      setActiveOperation("");
    }
  }

  if (isLoadingEvidence) {
    return (
      <div className="analysis-state">
        <span className="dashboard-loader" />

        <p>
          Retrieving processed evidence records...
        </p>
      </div>
    );
  }

  if (processedEvidence.length === 0) {
    return (
      <div className="analysis-state">
        <span className="empty-crosshair" />

        <h2>No processed evidence available</h2>

        <p>
          Open the Evidence tab, upload a supported file
          and extract its text before requesting an AI
          analysis.
        </p>
      </div>
    );
  }

  return (
    <div className="analysis-workspace">
      <section className="analysis-command-panel">
        <header className="analysis-panel-header">
          <span>AI ANALYSIS COMMAND</span>
          <span>REGISTER TL-AI-01</span>
        </header>

        <div className="analysis-command-body">
          <div className="analysis-evidence-selector">
            <div>
              <label htmlFor="analysis-evidence">
                Processed evidence
              </label>

              <span>
                {processedEvidence.length} AVAILABLE
              </span>
            </div>

            <select
              id="analysis-evidence"
              value={selectedEvidenceId}
              onChange={handleEvidenceChange}
              disabled={Boolean(activeOperation)}
            >
              {processedEvidence.map((item) => (
                <option
                  value={item.id}
                  key={item.id}
                >
                  {item.originalFileName} · Evidence/
                  {item.id}
                </option>
              ))}
            </select>
          </div>

          <div className="analysis-selected-file">
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
              <span>PROCESSING STATE</span>

              <strong>
                {formatEnum(selectedEvidence?.status)}
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
            className="analysis-generate-button"
            type="button"
            onClick={handleGenerateAnalysis}
            disabled={
              Boolean(activeOperation) ||
              isLoadingAnalysis
            }
          >
            <span>
              {activeOperation === "generate"
                ? "GENERATING FIRST ANALYSIS"
                : activeOperation === "regenerate"
                  ? "REGENERATING ANALYSIS"
                  : analysis ||
                      historyItems.length > 0
                    ? "REGENERATE AI ANALYSIS"
                    : "GENERATE AI ANALYSIS"}
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
          <span>ANALYSIS OPERATION COMPLETE</span>
          {successMessage}
        </div>
      )}

      {errorMessage && (
        <div
          className="system-message system-message-error"
          role="alert"
        >
          <span>AI ANALYSIS ERROR</span>
          {errorMessage}
        </div>
      )}

      {isLoadingAnalysis && (
        <div className="analysis-state">
          <span className="dashboard-loader" />

          <p>
            Retrieving the latest saved AI analysis...
          </p>
        </div>
      )}

      {!isLoadingAnalysis && !analysis && (
        <div className="analysis-state">
          <span className="analysis-scan-symbol">
            AI
          </span>

          <h2>No saved analysis</h2>

          <p>
            Generate the first persistent analysis for the
            selected processed evidence file.
          </p>
        </div>
      )}

      {!isLoadingAnalysis && analysis && (
        <>
          <section className="analysis-result-panel">
            <header className="analysis-result-header">
              <div>
                <span>
                  ANALYSIS/{analysis.analysisId}
                </span>

                <h2>
                  {analysis.originalFileName ||
                    selectedEvidence?.originalFileName}
                </h2>
              </div>

              <div className="analysis-result-badges">
                <span
                  className={`analysis-status analysis-status-${String(
                    analysis.status || "unknown",
                  ).toLowerCase()}`}
                >
                  {formatEnum(analysis.status)}
                </span>

                <span
                  className={`analysis-risk analysis-risk-${String(
                    analysis.riskLevel || "unknown",
                  ).toLowerCase()}`}
                >
                  {formatEnum(
                    analysis.riskLevel || "UNKNOWN",
                  )}{" "}
                  RISK
                </span>
              </div>
            </header>

            {analysis.status === "FAILED" && (
              <div className="analysis-failure-register">
                <span>SAFE FAILURE RECORD</span>

                <p>
                  {analysis.failureMessage ||
                    "The AI analysis attempt failed."}
                </p>
              </div>
            )}

            {analysis.status === "COMPLETED" && (
              <>
                <div className="analysis-summary-section">
                  <p className="analysis-section-code">
                    FACTUAL SUMMARY
                  </p>

                  <p className="analysis-summary-text">
                    {analysis.summary}
                  </p>
                </div>

                <div className="analysis-information-grid">
                  <div>
                    <span>
                      INFORMATION SUFFICIENT
                    </span>

                    <strong>
                      {renderBoolean(
                        analysis.sufficientInformation,
                      )}
                    </strong>
                  </div>

                  <div>
                    <span>
                      HUMAN REVIEW REQUIRED
                    </span>

                    <strong>
                      {renderBoolean(
                        analysis.humanReviewRequired,
                      )}
                    </strong>
                  </div>

                  <div>
                    <span>PROVIDER</span>

                    <strong>
                      {analysis.provider ||
                        "Not available"}
                    </strong>
                  </div>

                  <div>
                    <span>MODEL</span>

                    <strong>
                      {analysis.model ||
                        "Not available"}
                    </strong>
                  </div>
                </div>
              </>
            )}
          </section>

          {analysis.status === "COMPLETED" && (
            <div className="analysis-result-grid">
              <AnalysisList
                title="SUSPICIOUS FINDINGS"
                registerCode="REGISTER A"
                items={
                  analysis.suspiciousFindings || []
                }
                emptyText="No suspicious indicators were recorded."
              />

              <AnalysisList
                title="RECOMMENDED VERIFICATION ACTIONS"
                registerCode="REGISTER B"
                items={
                  analysis.recommendedActions || []
                }
                emptyText="No verification actions were returned."
              />
            </div>
          )}

          {analysis.status === "COMPLETED" && (
            <AnalysisList
              title="ANALYSIS LIMITATIONS"
              registerCode="REGISTER C"
              items={analysis.limitations || []}
              emptyText="No additional limitations were recorded."
            />
          )}

          <section className="analysis-metadata-panel">
            <header className="analysis-panel-header">
              <span>ANALYSIS METADATA</span>
              <span>REGISTER D</span>
            </header>

            <dl className="analysis-metadata-grid">
              <div>
                <dt>REQUESTED</dt>

                <dd>
                  {formatDate(analysis.requestedAt)}
                </dd>
              </div>

              <div>
                <dt>STARTED</dt>

                <dd>
                  {formatDate(analysis.startedAt)}
                </dd>
              </div>

              <div>
                <dt>COMPLETED</dt>

                <dd>
                  {formatDate(analysis.completedAt)}
                </dd>
              </div>

              <div>
                <dt>PROMPT VERSION</dt>

                <dd>
                  {analysis.promptVersion ||
                    "Not available"}
                </dd>
              </div>

              <div>
                <dt>SCHEMA VERSION</dt>

                <dd>
                  {analysis.responseSchemaVersion ||
                    "Not available"}
                </dd>
              </div>

              <div>
                <dt>PROMPT TOKENS</dt>

                <dd>
                  {analysis.promptTokens ??
                    "Not reported"}
                </dd>
              </div>

              <div>
                <dt>COMPLETION TOKENS</dt>

                <dd>
                  {analysis.completionTokens ??
                    "Not reported"}
                </dd>
              </div>

              <div>
                <dt>TOTAL TOKENS</dt>

                <dd>
                  {analysis.totalTokens ??
                    "Not reported"}
                </dd>
              </div>

              <div>
                <dt>PHYSICAL EVIDENCE HASH</dt>

                <dd
                  title={
                    analysis.sourceEvidenceSha256
                  }
                >
                  {shortenHash(
                    analysis.sourceEvidenceSha256,
                  )}
                </dd>
              </div>

              <div>
                <dt>EXTRACTED TEXT HASH</dt>

                <dd
                  title={analysis.sourceTextSha256}
                >
                  {shortenHash(
                    analysis.sourceTextSha256,
                  )}
                </dd>
              </div>
            </dl>
          </section>

          <div className="analysis-disclaimer">
            <span>MANDATORY REVIEW NOTICE</span>

            <p>
              AI-generated findings are investigative aids
              and must be independently verified. They do
              not establish guilt, legal liability or a
              final investigation conclusion.
            </p>
          </div>
        </>
      )}

      <section className="analysis-history-panel">
        <header className="analysis-panel-header">
          <span>ANALYSIS HISTORY</span>

          <span>
            {String(historyItems.length).padStart(
              2,
              "0",
            )}{" "}
            RECORDS
          </span>
        </header>

        {historyItems.length === 0 ? (
          <div className="analysis-small-empty">
            No previous analysis records are available for
            this evidence.
          </div>
        ) : (
          <div className="analysis-history-list">
            {historyItems.map((historyItem) => (
              <button
                type="button"
                key={historyItem.analysisId}
                onClick={() =>
                  handleOpenHistoryItem(historyItem)
                }
                disabled={Boolean(activeOperation)}
                className={
                  analysis?.analysisId ===
                  historyItem.analysisId
                    ? "analysis-history-item analysis-history-item-active"
                    : "analysis-history-item"
                }
              >
                <span>
                  ANALYSIS/{historyItem.analysisId}
                </span>

                <strong>
                  {formatEnum(historyItem.status)}
                </strong>

                <span>
                  {formatEnum(
                    historyItem.riskLevel ||
                      "UNKNOWN",
                  )}{" "}
                  RISK
                </span>

                <span>
                  {formatDate(
                    historyItem.completedAt ||
                      historyItem.requestedAt,
                  )}
                </span>
              </button>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}