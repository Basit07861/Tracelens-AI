import { useState } from "react";
import {
  Link,
  Navigate,
  useNavigate,
} from "react-router-dom";

import TraceLensMark from "../components/TraceLensMark";
import { useAuth } from "../context/useAuth";

const PASSWORD_PATTERN =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,64}$/;

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

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
    return "The TraceLens server could not be reached. Confirm that the backend is running.";
  }

  return "The investigator account could not be created.";
}

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register, isAuthenticated } = useAuth();

  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const [showPasswords, setShowPasswords] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

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
    const fullName = formData.fullName.trim();
    const email = formData.email.trim();

    if (fullName.length < 2 || fullName.length > 100) {
      return "Full name must contain between 2 and 100 characters.";
    }

    if (
      !EMAIL_PATTERN.test(email) ||
      email.length > 150
    ) {
      return "Enter a valid investigator email address.";
    }

    if (!PASSWORD_PATTERN.test(formData.password)) {
      return (
        "Password must contain 8 to 64 characters with uppercase, " +
        "lowercase, number and one permitted symbol: @$!%*?&."
      );
    }

    if (formData.password !== formData.confirmPassword) {
      return "Password confirmation does not match.";
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
      await register({
        fullName: formData.fullName.trim(),
        email: formData.email.trim(),
        password: formData.password,
      });

      navigate("/login", {
        replace: true,
        state: {
          registrationSuccess:
            "Investigator account created successfully. Sign in to continue.",
        },
      });
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="forensic-auth">
      <div className="auth-frame">
        <header className="auth-frame-header">
          <div className="brand-lockup">
            <TraceLensMark />

            <div>
              <p className="brand-name">TRACE//LENS</p>
              <p className="brand-subtitle">
                AI FORENSIC INVESTIGATION SYSTEM
              </p>
            </div>
          </div>

          <div className="security-level">
            <span className="status-indicator" />
            PERSONNEL ENROLMENT
          </div>
        </header>

        <div className="auth-workspace">
          <section
            className="dossier-panel registration-dossier"
            aria-labelledby="registration-dossier-heading"
          >
            <div className="classification-row">
              <span>PERSONNEL DOSSIER</span>
              <span>REG/02</span>
            </div>

            <div className="dossier-heading">
              <p className="section-code">
                INVESTIGATOR ENROLMENT
              </p>

              <h1 id="registration-dossier-heading">
                Enter the evidence room.
              </h1>

              <p>
                Create a secured investigator profile for managing
                cases, evidence and forensic intelligence.
              </p>
            </div>

            <div className="registration-checklist">
              <p>ENROLMENT REQUIREMENTS</p>

              <ul>
                <li>
                  <span>01</span>
                  Verified investigator identity
                </li>
                <li>
                  <span>02</span>
                  Unique registered email address
                </li>
                <li>
                  <span>03</span>
                  Strong access password
                </li>
                <li>
                  <span>04</span>
                  Controlled workspace authorization
                </li>
              </ul>
            </div>

            <div className="case-file-preview">
              <div className="case-file-preview-header">
                <span>PROFILE RECORD</span>
                <span>PENDING</span>
              </div>

              <div className="case-file-line">
                <span>ROLE</span>
                <strong>INVESTIGATOR</strong>
              </div>

              <div className="case-file-line">
                <span>ACCESS LEVEL</span>
                <strong>STANDARD</strong>
              </div>

              <div className="case-file-line">
                <span>STATUS</span>
                <strong>AWAITING CREATION</strong>
              </div>
            </div>

            <div
              className="dossier-stamp registration-stamp"
              aria-hidden="true"
            >
              ENROL
            </div>
          </section>

          <section
            className="access-panel"
            aria-labelledby="register-heading"
          >
            <div className="access-panel-inner register-panel-inner">
              <header className="access-heading">
                <div className="section-marker">
                  <span>PROFILE CREATION</span>
                  <span>TL-REG-02</span>
                </div>

                <h2 id="register-heading">
                  Register investigator
                </h2>

                <p>
                  All fields are required to create a TraceLens
                  investigation profile.
                </p>
              </header>

              {errorMessage && (
                <div
                  className="system-message system-message-error"
                  role="alert"
                >
                  <span>VALIDATION ERROR</span>
                  {errorMessage}
                </div>
              )}

              <form
                className="forensic-form registration-form"
                onSubmit={handleSubmit}
                noValidate
              >
                <div className="forensic-field">
                  <div className="field-label-row">
                    <label htmlFor="register-name">
                      Full legal name
                    </label>
                    <span>FIELD 01</span>
                  </div>

                  <input
                    id="register-name"
                    name="fullName"
                    type="text"
                    value={formData.fullName}
                    onChange={handleChange}
                    placeholder="Enter investigator name"
                    autoComplete="name"
                    minLength={2}
                    maxLength={100}
                    disabled={isSubmitting}
                    required
                  />
                </div>

                <div className="forensic-field">
                  <div className="field-label-row">
                    <label htmlFor="register-email">
                      Investigator email
                    </label>
                    <span>FIELD 02</span>
                  </div>

                  <input
                    id="register-email"
                    name="email"
                    type="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="investigator@example.com"
                    autoComplete="email"
                    maxLength={150}
                    disabled={isSubmitting}
                    required
                  />
                </div>

                <div className="registration-password-grid">
                  <div className="forensic-field">
                    <div className="field-label-row">
                      <label htmlFor="register-password">
                        Access password
                      </label>
                      <span>FIELD 03</span>
                    </div>

                    <input
                      id="register-password"
                      name="password"
                      type={showPasswords ? "text" : "password"}
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="Create password"
                      autoComplete="new-password"
                      minLength={8}
                      maxLength={64}
                      disabled={isSubmitting}
                      required
                    />
                  </div>

                  <div className="forensic-field">
                    <div className="field-label-row">
                      <label htmlFor="register-confirm-password">
                        Confirm password
                      </label>
                      <span>FIELD 04</span>
                    </div>

                    <input
                      id="register-confirm-password"
                      name="confirmPassword"
                      type={showPasswords ? "text" : "password"}
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      placeholder="Repeat password"
                      autoComplete="new-password"
                      minLength={8}
                      maxLength={64}
                      disabled={isSubmitting}
                      required
                    />
                  </div>
                </div>

                <div className="password-instructions">
                  <p>PASSWORD STANDARD</p>
                  <span>8–64 characters</span>
                  <span>Uppercase and lowercase</span>
                  <span>At least one number</span>
                  <span>One symbol: @$!%*?&</span>
                </div>

                <label className="show-password-control">
                  <input
                    type="checkbox"
                    checked={showPasswords}
                    onChange={(event) =>
                      setShowPasswords(event.target.checked)
                    }
                    disabled={isSubmitting}
                  />
                  Show password values
                </label>

                <button
                  className="forensic-action"
                  type="submit"
                  disabled={isSubmitting}
                >
                  <span>
                    {isSubmitting
                      ? "CREATING PROFILE"
                      : "CREATE INVESTIGATOR PROFILE"}
                  </span>
                  <span aria-hidden="true">→</span>
                </button>
              </form>

              <div className="access-divider">
                <span>EXISTING INVESTIGATOR</span>
              </div>

              <p className="account-route">
                Already registered?{" "}
                <Link to="/login">
                  Return to secure sign in
                </Link>
              </p>
            </div>
          </section>
        </div>

        <footer className="auth-frame-footer">
          <span>TRACELENS AI / PERSONNEL CONTROL</span>
          <span>REGISTRATION CHANNEL: SECURE</span>
          <span>CLASSIFICATION: INTERNAL</span>
        </footer>
      </div>
    </main>
  );
}