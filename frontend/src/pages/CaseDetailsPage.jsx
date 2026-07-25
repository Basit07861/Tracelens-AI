import { useEffect, useState } from "react";
import {
  Link,
  useLocation,
  useParams,
} from "react-router-dom";

import api from "../api/client";
import AnalysisPanel from "../components/case/AnalysisPanel";
import EntitiesPanel from "../components/case/EntitiesPanel";
import EvidencePanel from "../components/case/EvidencePanel";
import NotesPanel from "../components/case/NotesPanel";
import ReportPanel from "../components/case/ReportPanel";
import TimelinePanel from "../components/case/TimelinePanel";
import "./CasesPage.css";

const CASE_TABS = [
  {
    id: "overview",
    label: "OVERVIEW",
    enabled: true,
  },
  {
    id: "evidence",
    label: "EVIDENCE",
    enabled: true,
  },
  {
    id: "findings",
    label: "AI FINDINGS",
    enabled: true,
  },
  {
    id: "entities",
    label: "ENTITIES",
    enabled: true,
  },
  {
    id: "timeline",
    label: "TIMELINE",
    enabled: true,
  },
  {
    id: "notes",
    label: "NOTES",
    enabled: true,
  },
  {
    id: "report",
    label: "FINAL REPORT",
    enabled: true,
  },
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

function CaseOverview({ caseData }) {
  return (
    <>
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
              <dd>
                {formatEnum(caseData.status)}
              </dd>
            </div>

            <div>
              <dt>PRIORITY</dt>
              <dd>
                {formatEnum(caseData.priority)}
              </dd>
            </div>

            <div>
              <dt>INVESTIGATOR</dt>
              <dd>{caseData.ownerName}</dd>
            </div>

            <div>
              <dt>INVESTIGATOR EMAIL</dt>
              <dd>{caseData.ownerEmail}</dd>
            </div>

            <div>
              <dt>CREATED</dt>
              <dd>
                {formatDate(caseData.createdAt)}
              </dd>
            </div>

            <div>
              <dt>LAST UPDATED</dt>
              <dd>
                {formatDate(caseData.updatedAt)}
              </dd>
            </div>
          </dl>
        </aside>
      </div>

      <div className="case-details-placeholder">
        The complete investigation workspace is active.
        Upload and process evidence, review AI findings,
        inspect entities and timeline events, add manual
        notes and generate the final printable report.
      </div>
    </>
  );
}

export default function CaseDetailsPage() {
  const { caseId } = useParams();
  const location = useLocation();

  const [caseData, setCaseData] =
    useState(null);

  const [activeTab, setActiveTab] =
    useState("overview");

  const [errorMessage, setErrorMessage] =
    useState("");

  const [isLoading, setIsLoading] =
    useState(true);

  const [reloadKey, setReloadKey] =
    useState(0);

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
          setCaseData(
            response.data?.data || null,
          );
        }
      } catch (error) {
        if (
          error.name !== "CanceledError" &&
          error.code !== "ERR_CANCELED" &&
          !controller.signal.aborted
        ) {
          setErrorMessage(
            getErrorMessage(error),
          );
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

    setReloadKey(
      (currentKey) => currentKey + 1,
    );
  }

  function handleTabChange(tab) {
    if (!tab.enabled) {
      return;
    }

    setActiveTab(tab.id);
  }

  if (isLoading) {
    return (
      <div className="cases-state">
        <span className="dashboard-loader" />

        <p>
          Opening secured investigation case file...
        </p>
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
        {CASE_TABS.map((tab) => (
          <button
            className={
              activeTab === tab.id
                ? "case-details-tab case-details-tab-active"
                : "case-details-tab"
            }
            type="button"
            key={tab.id}
            disabled={!tab.enabled}
            aria-selected={
              activeTab === tab.id
            }
            onClick={() =>
              handleTabChange(tab)
            }
          >
            {tab.label}
          </button>
        ))}
      </nav>

      {activeTab === "overview" && (
        <CaseOverview caseData={caseData} />
      )}

      {activeTab === "evidence" && (
        <div style={{ marginTop: "14px" }}>
          <EvidencePanel caseId={caseId} />
        </div>
      )}

      {activeTab === "findings" && (
        <div style={{ marginTop: "14px" }}>
          <AnalysisPanel caseId={caseId} />
        </div>
      )}

      {activeTab === "entities" && (
        <div style={{ marginTop: "14px" }}>
          <EntitiesPanel caseId={caseId} />
        </div>
      )}

      {activeTab === "timeline" && (
        <div style={{ marginTop: "14px" }}>
          <TimelinePanel caseId={caseId} />
        </div>
      )}

      {activeTab === "notes" && (
        <div style={{ marginTop: "14px" }}>
          <NotesPanel caseId={caseId} />
        </div>
      )}

      {activeTab === "report" && (
        <div style={{ marginTop: "14px" }}>
          <ReportPanel caseId={caseId} />
        </div>
      )}
    </>
  );
}