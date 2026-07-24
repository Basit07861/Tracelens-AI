import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import api from "../api/client";
import "./CasesPage.css";

const PRIORITY_OPTIONS = [
  "LOW",
  "MEDIUM",
  "HIGH",
  "CRITICAL",
];

function formatEnum(value) {
  return value
    .toLowerCase()
    .split("_")
    .map(
      (part) =>
        part.charAt(0).toUpperCase() + part.slice(1),
    )
    .join(" ");
}

function getErrorMessage(error) {
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

  return "The investigation case could not be created.";
}

export default function NewCasePage() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    title: "",
    description: "",
    priority: "MEDIUM",
  });

  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }));

    if (errorMessage) {
      setErrorMessage("");
    }
  }

  function validateForm() {
    const title = formData.title.trim();
    const description = formData.description.trim();

    if (!title) {
      return "A case title is required.";
    }

    if (title.length > 150) {
      return "The case title cannot exceed 150 characters.";
    }

    if (!description) {
      return "A case description is required.";
    }

    return "";
  }

  async function handleSubmit(event) {
    event.preventDefault();

    const validationError = validateForm();

    if (validationError) {
      setErrorMessage(validationError);
      return;
    }

    setIsSubmitting(true);
    setErrorMessage("");

    try {
      const response = await api.post("/api/cases", {
        title: formData.title.trim(),
        description: formData.description.trim(),
        priority: formData.priority,
      });

      const createdCase = response.data?.data;

      if (!createdCase?.id) {
        throw new Error(
          "The server returned an invalid case response.",
        );
      }

      navigate(`/cases/${createdCase.id}`, {
        replace: true,
        state: {
          successMessage:
            "Investigation case created successfully.",
        },
      });
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <>
      <header className="command-heading cases-heading">
        <div>
          <p className="section-code">
            CASE INTAKE / NEW INVESTIGATION
          </p>

          <h1>Open a new case file</h1>

          <p>
            Record the initial investigation scope and assign
            an operational priority.
          </p>
        </div>

        <Link
          className="case-cancel-link"
          to="/cases"
        >
          ← RETURN TO REGISTER
        </Link>
      </header>

      <div className="case-intake-shell">
        <section className="case-intake-panel">
          <header className="panel-register-header">
            <span>CASE INTAKE FORM</span>
            <span>FORM TL-CI-01</span>
          </header>

          {errorMessage && (
            <div
              className="system-message system-message-error"
              role="alert"
              style={{ margin: "20px 26px 0" }}
            >
              <span>INTAKE ERROR</span>
              {errorMessage}
            </div>
          )}

          <form
            className="case-intake-form"
            onSubmit={handleSubmit}
            noValidate
          >
            <div className="case-form-field">
              <div className="case-form-label">
                <label htmlFor="case-title">
                  Investigation title
                </label>

                <span>FIELD 01 / REQUIRED</span>
              </div>

              <input
                id="case-title"
                name="title"
                type="text"
                value={formData.title}
                onChange={handleChange}
                placeholder="Example: Suspicious Invoice Investigation"
                maxLength={150}
                disabled={isSubmitting}
                required
              />

              <span className="case-character-count">
                {formData.title.length} / 150
              </span>
            </div>

            <div className="case-form-field">
              <div className="case-form-label">
                <label htmlFor="case-description">
                  Investigation description
                </label>

                <span>FIELD 02 / REQUIRED</span>
              </div>

              <textarea
                id="case-description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                placeholder="Record the allegation, known facts, initial scope and investigation purpose."
                disabled={isSubmitting}
                required
              />
            </div>

            <div className="case-form-field">
              <div className="case-form-label">
                <label htmlFor="case-priority">
                  Operational priority
                </label>

                <span>FIELD 03 / REQUIRED</span>
              </div>

              <select
                id="case-priority"
                name="priority"
                value={formData.priority}
                onChange={handleChange}
                disabled={isSubmitting}
              >
                {PRIORITY_OPTIONS.map((priority) => (
                  <option
                    value={priority}
                    key={priority}
                  >
                    {formatEnum(priority)}
                  </option>
                ))}
              </select>
            </div>

            <div className="case-form-actions">
              <Link
                className="case-cancel-link"
                to="/cases"
              >
                CANCEL INTAKE
              </Link>

              <button
                className="case-submit-button"
                type="submit"
                disabled={isSubmitting}
              >
                <span>
                  {isSubmitting
                    ? "CREATING CASE FILE"
                    : "CREATE CASE FILE"}
                </span>

                <span aria-hidden="true">→</span>
              </button>
            </div>
          </form>
        </section>

        <aside className="case-guidance-panel">
          <header className="panel-register-header">
            <span>INTAKE GUIDANCE</span>
            <span>REGISTER B</span>
          </header>

          <div className="case-guidance-body">
            <p>CASE CREATION STANDARD</p>

            <ul className="case-guidance-list">
              <li>
                <span>01</span>
                Use a concise title that identifies the main
                investigation subject.
              </li>

              <li>
                <span>02</span>
                Record facts and scope without adding unsupported
                conclusions.
              </li>

              <li>
                <span>03</span>
                Choose priority according to operational urgency
                and potential impact.
              </li>

              <li>
                <span>04</span>
                Evidence and intelligence can be added after the
                case file is created.
              </li>
            </ul>

            <div className="case-default-record">
              <div>
                <span>INITIAL STATUS</span>
                <strong>OPEN</strong>
              </div>

              <div>
                <span>DEFAULT PRIORITY</span>
                <strong>MEDIUM</strong>
              </div>

              <div>
                <span>OWNERSHIP</span>
                <strong>JWT INVESTIGATOR</strong>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </>
  );
}