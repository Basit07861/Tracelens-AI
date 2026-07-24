import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";

import api from "../api/client";
import "./CasesPage.css";

const STATUS_OPTIONS = [
  "OPEN",
  "IN_PROGRESS",
  "COMPLETED",
  "ARCHIVED",
];

const PRIORITY_OPTIONS = [
  "LOW",
  "MEDIUM",
  "HIGH",
  "CRITICAL",
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

  return "Investigation cases could not be retrieved.";
}

export default function CasesPage() {
  const [formFilters, setFormFilters] = useState({
    keyword: "",
    status: "",
    priority: "",
  });

  const [appliedFilters, setAppliedFilters] = useState({
    keyword: "",
    status: "",
    priority: "",
  });

  const [pageData, setPageData] = useState(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [reloadKey, setReloadKey] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    async function retrieveCases() {
      try {
        const response = await api.get("/api/cases", {
          signal: controller.signal,
          params: {
            keyword: appliedFilters.keyword || undefined,
            status: appliedFilters.status || undefined,
            priority: appliedFilters.priority || undefined,
            page: pageNumber,
            size: 8,
            sortBy: "updatedAt",
            sortDirection: "desc",
          },
        });

        if (!controller.signal.aborted) {
          setPageData(response.data?.data || null);
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

    retrieveCases();

    return () => controller.abort();
  }, [appliedFilters, pageNumber, reloadKey]);

  const cases = useMemo(
    () => pageData?.content || [],
    [pageData],
  );

  function handleFilterChange(event) {
    const { name, value } = event.target;

    setFormFilters((currentFilters) => ({
      ...currentFilters,
      [name]: value,
    }));
  }

  function handleSearch(event) {
    event.preventDefault();

    setIsLoading(true);
    setErrorMessage("");
    setPageNumber(0);
    setAppliedFilters({
      keyword: formFilters.keyword.trim(),
      status: formFilters.status,
      priority: formFilters.priority,
    });

    if (
      pageNumber === 0 &&
      formFilters.keyword.trim() ===
        appliedFilters.keyword &&
      formFilters.status === appliedFilters.status &&
      formFilters.priority === appliedFilters.priority
    ) {
      setReloadKey((currentKey) => currentKey + 1);
    }
  }

  function handleClearFilters() {
    const emptyFilters = {
      keyword: "",
      status: "",
      priority: "",
    };

    setFormFilters(emptyFilters);
    setAppliedFilters(emptyFilters);
    setPageNumber(0);
    setIsLoading(true);
    setErrorMessage("");
    setReloadKey((currentKey) => currentKey + 1);
  }

  function handlePageChange(nextPage) {
    if (
      nextPage < 0 ||
      nextPage >= (pageData?.totalPages || 0)
    ) {
      return;
    }

    setIsLoading(true);
    setErrorMessage("");
    setPageNumber(nextPage);
  }

  function handleRetry() {
    setIsLoading(true);
    setErrorMessage("");
    setReloadKey((currentKey) => currentKey + 1);
  }

  return (
    <>
      <header className="command-heading cases-heading">
        <div>
          <p className="section-code">
            CASE FILE REGISTER / INVESTIGATIONS
          </p>

          <h1>Investigation cases</h1>

          <p>
            Search, classify and open case files belonging to
            the authenticated investigator.
          </p>
        </div>

        <Link
          className="forensic-primary-link"
          to="/cases/new"
        >
          <span>NEW CASE INTAKE</span>
          <span aria-hidden="true">＋</span>
        </Link>
      </header>

      <form
        className="case-filter-panel"
        onSubmit={handleSearch}
      >
        <div className="case-filter-field case-keyword-field">
          <label htmlFor="case-keyword">
            Search case register
          </label>

          <input
            id="case-keyword"
            name="keyword"
            type="search"
            value={formFilters.keyword}
            onChange={handleFilterChange}
            placeholder="Case number, title or description"
            maxLength={150}
          />
        </div>

        <div className="case-filter-field">
          <label htmlFor="case-status">Status</label>

          <select
            id="case-status"
            name="status"
            value={formFilters.status}
            onChange={handleFilterChange}
          >
            <option value="">All statuses</option>

            {STATUS_OPTIONS.map((status) => (
              <option value={status} key={status}>
                {formatEnum(status)}
              </option>
            ))}
          </select>
        </div>

        <div className="case-filter-field">
          <label htmlFor="case-priority">Priority</label>

          <select
            id="case-priority"
            name="priority"
            value={formFilters.priority}
            onChange={handleFilterChange}
          >
            <option value="">All priorities</option>

            {PRIORITY_OPTIONS.map((priority) => (
              <option value={priority} key={priority}>
                {formatEnum(priority)}
              </option>
            ))}
          </select>
        </div>

        <div className="case-filter-actions">
          <button
            className="case-filter-submit"
            type="submit"
          >
            SEARCH REGISTER
          </button>

          <button
            className="case-filter-clear"
            type="button"
            onClick={handleClearFilters}
          >
            CLEAR
          </button>
        </div>
      </form>

      <div className="case-register-summary">
        <div>
          <span>REGISTERED RECORDS</span>
          <strong>
            {pageData?.totalElements ?? "--"}
          </strong>
        </div>

        <div>
          <span>CURRENT PAGE</span>
          <strong>
            {pageData
              ? `${pageData.pageNumber + 1} / ${
                  pageData.totalPages || 1
                }`
              : "--"}
          </strong>
        </div>

        <div>
          <span>SORT ORDER</span>
          <strong>LAST UPDATED / DESCENDING</strong>
        </div>
      </div>

      {isLoading && (
        <div className="cases-state">
          <span className="dashboard-loader" />
          <p>Retrieving investigation case files...</p>
        </div>
      )}

      {!isLoading && errorMessage && (
        <div className="cases-state cases-error-state">
          <p className="section-code">
            CASE REGISTER ERROR
          </p>

          <h2>Case files unavailable</h2>
          <p>{errorMessage}</p>

          <button
            className="forensic-secondary-action"
            type="button"
            onClick={handleRetry}
          >
            RETRY CASE REGISTER
          </button>
        </div>
      )}

      {!isLoading &&
        !errorMessage &&
        cases.length === 0 && (
          <div className="cases-state">
            <span className="empty-crosshair" />

            <h2>No matching case files</h2>

            <p>
              Adjust the search filters or create a new
              investigation case.
            </p>

            <Link
              className="forensic-secondary-action"
              to="/cases/new"
            >
              CREATE INVESTIGATION CASE
            </Link>
          </div>
        )}

      {!isLoading &&
        !errorMessage &&
        cases.length > 0 && (
          <>
            <div className="case-file-grid">
              {cases.map((caseItem) => (
                <Link
                  className="case-file-card"
                  to={`/cases/${caseItem.id}`}
                  key={caseItem.id}
                >
                  <header className="case-file-card-header">
                    <span>{caseItem.caseNumber}</span>
                    <span>FILE/{caseItem.id}</span>
                  </header>

                  <div className="case-file-card-body">
                    <div className="case-file-badges">
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

                    <h2>{caseItem.title}</h2>

                    <p>
                      {caseItem.description ||
                        "No case description recorded."}
                    </p>
                  </div>

                  <footer className="case-file-card-footer">
                    <div>
                      <span>LAST UPDATED</span>
                      <strong>
                        {formatDate(caseItem.updatedAt)}
                      </strong>
                    </div>

                    <span
                      className="case-open-arrow"
                      aria-hidden="true"
                    >
                      →
                    </span>
                  </footer>
                </Link>
              ))}
            </div>

            <nav
              className="case-pagination"
              aria-label="Case register pages"
            >
              <button
                type="button"
                onClick={() =>
                  handlePageChange(pageNumber - 1)
                }
                disabled={pageData?.first}
              >
                ← PREVIOUS
              </button>

              <span>
                PAGE {(pageData?.pageNumber ?? 0) + 1} OF{" "}
                {pageData?.totalPages || 1}
              </span>

              <button
                type="button"
                onClick={() =>
                  handlePageChange(pageNumber + 1)
                }
                disabled={pageData?.last}
              >
                NEXT →
              </button>
            </nav>
          </>
        )}
    </>
  );
}