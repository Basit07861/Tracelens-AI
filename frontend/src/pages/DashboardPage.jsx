import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import api from "../api/client";
import "./DashboardPage.css";

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

  return "Dashboard intelligence could not be retrieved.";
}

function calculatePercentage(count, total) {
  if (!total) {
    return 0;
  }

  return Math.min(
    100,
    Math.round((Number(count) / Number(total)) * 100),
  );
}

async function requestDashboard(signal) {
  const response = await api.get("/api/dashboard", {
    signal,
  });

  const dashboardData = response.data?.data;

  if (!dashboardData) {
    throw new Error(
      "The server returned an invalid dashboard response.",
    );
  }

  return dashboardData;
}

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const controller = new AbortController();

    async function retrieveDashboard() {
      try {
        const dashboardData = await requestDashboard(
          controller.signal,
        );

        if (!controller.signal.aborted) {
          setDashboard(dashboardData);
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

    retrieveDashboard();

    return () => controller.abort();
  }, []);

  async function handleRetry() {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const dashboardData = await requestDashboard();

      setDashboard(dashboardData);
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  }

  if (isLoading) {
    return (
      <div className="dashboard-state">
        <span className="dashboard-loader" />

        <p>Retrieving investigation analytics...</p>
      </div>
    );
  }

  if (errorMessage) {
    return (
      <div className="dashboard-state dashboard-error-state">
        <p className="section-code">
          DASHBOARD CONNECTION ERROR
        </p>

        <h1>Analytics unavailable</h1>

        <p>{errorMessage}</p>

        <button
          className="forensic-secondary-action"
          type="button"
          onClick={handleRetry}
        >
          RETRY DASHBOARD CONNECTION
        </button>
      </div>
    );
  }

  const statusData = dashboard?.casesByStatus || [];
  const priorityData = dashboard?.casesByPriority || [];
  const recentCases =
    dashboard?.recentlyUpdatedCases || [];

  return (
    <>
      <header className="command-heading">
        <div>
          <p className="section-code">
            OPERATIONAL OVERVIEW / DASHBOARD
          </p>

          <h1>Investigation command centre</h1>

          <p>
            Live operational statistics for the authenticated
            investigator’s cases, evidence and forensic
            intelligence.
          </p>
        </div>

        <div className="command-classification">
          <span>DATA ACCESS</span>
          <strong>OWNER RESTRICTED</strong>
        </div>
      </header>

      <div className="dossier-counter-grid dashboard-metric-grid">
        <article className="dossier-counter">
          <div>
            <span>TOTAL CASES</span>
            <span>METRIC/01</span>
          </div>

          <strong>{dashboard.totalCases}</strong>
          <p>Registered investigations</p>
        </article>

        <article className="dossier-counter">
          <div>
            <span>OPEN CASES</span>
            <span>METRIC/02</span>
          </div>

          <strong>{dashboard.openCases}</strong>
          <p>New active case files</p>
        </article>

        <article className="dossier-counter">
          <div>
            <span>IN PROGRESS</span>
            <span>METRIC/03</span>
          </div>

          <strong>{dashboard.inProgressCases}</strong>
          <p>Cases under investigation</p>
        </article>

        <article className="dossier-counter">
          <div>
            <span>EVIDENCE</span>
            <span>METRIC/04</span>
          </div>

          <strong>{dashboard.totalEvidence}</strong>
          <p>Total registered evidence</p>
        </article>

        <article className="dossier-counter">
          <div>
            <span>PROCESSED</span>
            <span>METRIC/05</span>
          </div>

          <strong>{dashboard.processedEvidence}</strong>
          <p>Extracted evidence items</p>
        </article>

        <article className="dossier-counter risk-counter">
          <div>
            <span>HIGH RISK</span>
            <span>METRIC/06</span>
          </div>

          <strong>{dashboard.highRiskAnalyses}</strong>
          <p>Flagged AI analyses</p>
        </article>
      </div>

      <div className="dashboard-analysis-grid">
        <article className="command-panel dashboard-breakdown">
          <header>
            <span>CASE STATUS REGISTER</span>
            <span>REGISTER A</span>
          </header>

          <div className="breakdown-list">
            {statusData.map((item) => {
              const percentage = calculatePercentage(
                item.count,
                dashboard.totalCases,
              );

              return (
                <div
                  className="breakdown-item"
                  key={item.status}
                >
                  <div className="breakdown-heading">
                    <span>{formatEnum(item.status)}</span>
                    <strong>{item.count}</strong>
                  </div>

                  <div
                    className="breakdown-track"
                    aria-label={`${formatEnum(
                      item.status,
                    )}: ${item.count}`}
                  >
                    <span
                      style={{
                        width: `${percentage}%`,
                      }}
                    />
                  </div>
                </div>
              );
            })}
          </div>

          <footer className="panel-summary">
            <span>
              COMPLETED: {dashboard.completedCases}
            </span>

            <span>
              ARCHIVED: {dashboard.archivedCases}
            </span>
          </footer>
        </article>

        <article className="command-panel dashboard-breakdown">
          <header>
            <span>CASE PRIORITY REGISTER</span>
            <span>REGISTER B</span>
          </header>

          <div className="priority-register">
            {priorityData.map((item) => (
              <div
                className={`priority-row priority-${item.priority.toLowerCase()}`}
                key={item.priority}
              >
                <span>{formatEnum(item.priority)}</span>
                <strong>{item.count}</strong>
              </div>
            ))}
          </div>
        </article>
      </div>

      <article className="command-panel recent-cases-panel">
        <header>
          <span>RECENTLY UPDATED CASE FILES</span>
          <span>MAXIMUM 05 RECORDS</span>
        </header>

        {recentCases.length === 0 ? (
          <div className="command-empty-state">
            <span className="empty-crosshair" />

            <h2>No recent case records</h2>

            <p>
              Create an investigation case to begin building
              the operational register.
            </p>

            <Link
              className="forensic-secondary-action"
              to="/cases/new"
            >
              CREATE FIRST CASE
            </Link>
          </div>
        ) : (
          <div className="recent-case-list">
            {recentCases.map((caseItem) => (
              <Link
                className="recent-case-record"
                to={`/cases/${caseItem.id}`}
                key={caseItem.id}
              >
                <div className="case-record-identity">
                  <span>{caseItem.caseNumber}</span>
                  <strong>{caseItem.title}</strong>
                </div>

                <div className="case-record-classification">
                  <span
                    className={`case-badge status-${caseItem.status.toLowerCase()}`}
                  >
                    {formatEnum(caseItem.status)}
                  </span>

                  <span
                    className={`case-badge priority-${caseItem.priority.toLowerCase()}`}
                  >
                    {formatEnum(caseItem.priority)}
                  </span>
                </div>

                <div className="case-record-date">
                  <span>LAST UPDATED</span>

                  <strong>
                    {formatDate(caseItem.updatedAt)}
                  </strong>
                </div>

                <span
                  className="case-record-arrow"
                  aria-hidden="true"
                >
                  →
                </span>
              </Link>
            ))}
          </div>
        )}
      </article>
    </>
  );
}