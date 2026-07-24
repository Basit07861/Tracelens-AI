import { useState } from "react";
import {
  Link,
  Navigate,
  useLocation,
  useNavigate,
} from "react-router-dom";

import TraceLensMark from "../components/TraceLensMark";
import { useAuth } from "../context/useAuth";

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

  return "Access was denied. Verify your email address and password.";
}

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated } = useAuth();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const registrationSuccess =
    location.state?.registrationSuccess || "";

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

  async function handleSubmit(event) {
    event.preventDefault();

    const email = formData.email.trim();

    if (!email || !formData.password) {
      setErrorMessage(
        "Both investigator email and password are required.",
      );
      return;
    }

    setIsSubmitting(true);
    setErrorMessage("");

    try {
      await login(email, formData.password);

      const attemptedLocation = location.state?.from;

      const destination = attemptedLocation
        ? `${attemptedLocation.pathname}${
            attemptedLocation.search || ""
          }${attemptedLocation.hash || ""}`
        : "/dashboard";

      navigate(destination, {
        replace: true,
        state: null,
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
            SECURE NODE / ONLINE
          </div>
        </header>

        <div className="auth-workspace">
          <section
            className="dossier-panel"
            aria-labelledby="dossier-heading"
          >
            <div className="classification-row">
              <span>CASE ACCESS DOSSIER</span>
              <span>AUTH/01</span>
            </div>

            <div className="dossier-heading">
              <p className="section-code">
                DIGITAL INVESTIGATION WORKSPACE
              </p>

              <h1 id="dossier-heading">
                Follow every trace.
                <br />
                Protect every fact.
              </h1>

              <p>
                A structured forensic workspace for preserving
                evidence, reconstructing events and reviewing
                AI-assisted intelligence.
              </p>
            </div>

            <div className="dossier-capabilities">
              <article>
                <span className="capability-number">01</span>
                <div>
                  <h2>Evidence Integrity</h2>
                  <p>
                    SHA-256 verification and duplicate evidence
                    protection.
                  </p>
                </div>
              </article>

              <article>
                <span className="capability-number">02</span>
                <div>
                  <h2>Intelligence Extraction</h2>
                  <p>
                    Identify entities, findings and chronological
                    events.
                  </p>
                </div>
              </article>

              <article>
                <span className="capability-number">03</span>
                <div>
                  <h2>Case Reconstruction</h2>
                  <p>
                    Organize evidence into an auditable investigation
                    record.
                  </p>
                </div>
              </article>
            </div>

            <div className="system-register">
              <div>
                <span>SYSTEM</span>
                <strong>OPERATIONAL</strong>
              </div>

              <div>
                <span>SESSION</span>
                <strong>ENCRYPTED</strong>
              </div>

              <div>
                <span>ACCESS</span>
                <strong>RESTRICTED</strong>
              </div>
            </div>

            <div className="dossier-stamp" aria-hidden="true">
              VERIFIED
            </div>
          </section>

          <section
            className="access-panel"
            aria-labelledby="login-heading"
          >
            <div className="access-panel-inner">
              <header className="access-heading">
                <div className="section-marker">
                  <span>ACCESS CONTROL</span>
                  <span>TL-AUTH-01</span>
                </div>

                <h2 id="login-heading">
                  Investigator sign in
                </h2>

                <p>
                  Enter your registered credentials to open the
                  investigation workspace.
                </p>
              </header>

              {registrationSuccess && (
                <div
                  className="system-message system-message-success"
                  role="status"
                >
                  <span>ACCOUNT CREATED</span>
                  {registrationSuccess}
                </div>
              )}

              {errorMessage && (
                <div
                  className="system-message system-message-error"
                  role="alert"
                >
                  <span>ACCESS ERROR</span>
                  {errorMessage}
                </div>
              )}

              <form
                className="forensic-form"
                onSubmit={handleSubmit}
                noValidate
              >
                <div className="forensic-field">
                  <div className="field-label-row">
                    <label htmlFor="login-email">
                      Investigator email
                    </label>
                    <span>FIELD 01</span>
                  </div>

                  <input
                    id="login-email"
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

                <div className="forensic-field">
                  <div className="field-label-row">
                    <label htmlFor="login-password">
                      Access password
                    </label>
                    <span>FIELD 02</span>
                  </div>

                  <div className="password-control">
                    <input
                      id="login-password"
                      name="password"
                      type={showPassword ? "text" : "password"}
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="Enter secure password"
                      autoComplete="current-password"
                      maxLength={64}
                      disabled={isSubmitting}
                      required
                    />

                    <button
                      type="button"
                      className="password-toggle"
                      onClick={() =>
                        setShowPassword((current) => !current)
                      }
                      disabled={isSubmitting}
                      aria-label={
                        showPassword
                          ? "Hide password"
                          : "Show password"
                      }
                    >
                      {showPassword ? "HIDE" : "SHOW"}
                    </button>
                  </div>
                </div>

                <button
                  className="forensic-action"
                  type="submit"
                  disabled={isSubmitting}
                >
                  <span>
                    {isSubmitting
                      ? "VERIFYING CREDENTIALS"
                      : "ACCESS WORKSPACE"}
                  </span>
                  <span aria-hidden="true">→</span>
                </button>
              </form>

              <div className="access-divider">
                <span>NEW INVESTIGATOR</span>
              </div>

              <p className="account-route">
                No registered account?{" "}
                <Link to="/register">
                  Create investigator profile
                </Link>
              </p>

              <p className="restricted-notice">
                AUTHORIZED PERSONNEL ONLY
                <br />
                All access attempts may be recorded for security and
                audit purposes.
              </p>
            </div>
          </section>
        </div>

        <footer className="auth-frame-footer">
          <span>TRACELENS AI / FORENSIC OPERATIONS</span>
          <span>BUILD NODE: LOCAL-5173</span>
          <span>CLASSIFICATION: INTERNAL</span>
        </footer>
      </div>
    </main>
  );
}