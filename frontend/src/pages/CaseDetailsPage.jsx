import { useEffect, useState } from "react";
import {
  Link,
  useLocation,
  useParams,
} from "react-router-dom";

import api from "../api/client";
import "./CasesPage.css";

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

function getErrorMessage(error) {
  if (error.response?.data?.message) {
    return error.response.data.message;
  }

  if (!error.response) {
    return (
      "The TraceLens backend could not be reached. " +
      "Confirm that it is running on port 8080."
    );
  }

  return "The investigation case could not be retrieved.";
}

export default function CaseDetailsPage() {
  const { caseId } = useParams();
  const location = useLocation();

  const [caseData, setCaseData] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    const controller = new AbortController();

    async function retrieveCase() {
      try {
        const response = await api.get(
          `/api/cases/${caseId}`,
          {
            signal: controller.signal,
          },
        );

        if (!controller.signal.aborted) {
          setCaseData(response.data?.data || null);
        }
      } catch (error) {
        if (
          error.name !== "CanceledError" &&
          error.code !== "ERR_CANCELED" &&
          !controller.signal.aborted
        ) {
          setErrorMessage(getErrorMessage(error));
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    retrieveCase();

    return () => controller.abort();
  }, [caseId, reloadKey]);

  function handleRetry() {
    setIsLoading(true);
    setErrorMessage("");
    setReloadKey((currentKey) => currentKey + 1);
  }

  if (isLoading) {
    return (
      <div className="cases-state">
        <span className="dashboard-loader" />
        <p>Opening secured investigation case file...</p>
      </div>
    );
  }

  if (errorMessage || !caseData) {
    return (
      <div className="cases-state cases-error-state">
        <p className="section-code">
          CASE FILE ACCESS ERROR
        </p>

        <h2>Case file unavailable</h2>

        <p>
          {errorMessage ||
            "The server returned no case information."}
        </p>

        <button
          className="forensic-secondary-action"
          type="button"
          onClick={handleRetry}
        >
          RETRY CASE FILE
        </button>

        <Link
          className="forensic-secondary-action"
          to="/cases"
        >
          RETURN TO CASE REGISTER
        </Link>
      </div>
    );
  }

  return (
    <>
      <header className="command-heading cases-heading">
        <div>
          <p className="section-code">
            CASE FILE / {caseData.caseNumber}
          </p>

          <h1>{caseData.title}</h1>

          <p>
            Secured investigation record owned by the
            authenticated investigator.
          </p>
        </div>

        <div className="case-details-toolbar">
          <Link
            className="case-cancel-link"
            to="/cases"
          >
            ← CASE REGISTER
          </Link>
        </div>
      </header>

      {location.state?.successMessage && (
        <div
          className="system-message system-message-success"
          role="status"
          style={{ marginTop: "20px" }}
        >
          <span>CASE CREATED</span>
          {location.state.successMessage}
        </div>
      )}

      <nav
        className="case-details-tabs"
        aria-label="Case workspace sections"
      >
        <button
          className="case-details-tab case-details-tab-active"
          type="button"
        >
          OVERVIEW
        </button>

        <button
          className="case-details-tab"
          type="button"
          disabled
        >
          EVIDENCE
        </button>

        <button
          className="case-details-tab"
          type="button"
          disabled
        >
          AI FINDINGS
        </button>

        <button
          className="case-details-tab"
          type="button"
          disabled
        >
          ENTITIES
        </button>

        <button
          className="case-details-tab"
          type="button"
          disabled
        >
          TIMELINE
        </button>

        <button
          className="case-details-tab"
          type="button"
          disabled
        >
          NOTES
        </button>

        <button
          className="case-details-tab"
          type="button"
          disabled
        >
          FINAL REPORT
        </button>
      </nav>

      <div className="case-overview-grid">
        <section className="case-overview-panel">
          <header className="panel-register-header">
            <span>INVESTIGATION OVERVIEW</span>
            <span>REGISTER A</span>
          </header>

          <div className="case-overview-body">
            <div className="case-file-badges">
              <span
                className={`case-badge status-${caseData.status.toLowerCase()}`}
              >
                {formatEnum(caseData.status)}
              </span>

              <span
                className={`case-badge priority-${caseData.priority.toLowerCase()}`}
              >
                {formatEnum(caseData.priority)}
              </span>
            </div>

            <h2>{caseData.title}</h2>

            <p className="case-overview-description">
              {caseData.description}
            </p>
          </div>
        </section>

        <aside className="case-overview-panel">
          <header className="panel-register-header">
            <span>CASE METADATA</span>
            <span>REGISTER B</span>
          </header>

          <dl className="case-metadata-register">
            <div>
              <dt>CASE NUMBER</dt>
              <dd>{caseData.caseNumber}</dd>
            </div>

            <div>
              <dt>STATUS</dt>
              <dd>{formatEnum(caseData.status)}</dd>
            </div>

            <div>
              <dt>PRIORITY</dt>
              <dd>{formatEnum(caseData.priority)}</dd>
            </div>

            <div>
              <dt>INVESTIGATOR</dt>
              <dd>{caseData.ownerName}</dd>
            </div>

            <div>
              <dt>CREATED</dt>
              <dd>{formatDate(caseData.createdAt)}</dd>
            </div>

            <div>
              <dt>LAST UPDATED</dt>
              <dd>{formatDate(caseData.updatedAt)}</dd>
            </div>
          </dl>
        </aside>
      </div>

      <div className="case-details-placeholder">
        Evidence, AI findings, entities, timeline, notes and
        final-report tabs will become active during Day 13.
      </div>
    </>
  );
}