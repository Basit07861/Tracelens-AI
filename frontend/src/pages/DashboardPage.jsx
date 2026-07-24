import TraceLensMark from "../components/TraceLensMark";
import { useAuth } from "../context/useAuth";

export default function DashboardPage() {
  const { user, logout } = useAuth();

  return (
    <main className="command-centre">
      <header className="command-topbar">
        <div className="brand-lockup">
          <TraceLensMark />

          <div>
            <p className="brand-name">TRACE//LENS</p>
            <p className="brand-subtitle">
              INVESTIGATION COMMAND CENTRE
            </p>
          </div>
        </div>

        <div className="command-user">
          <div>
            <span>ACTIVE INVESTIGATOR</span>
            <strong>
              {user?.fullName || "Investigator"}
            </strong>
          </div>

          <button
            className="command-signout"
            type="button"
            onClick={logout}
          >
            TERMINATE SESSION
          </button>
        </div>
      </header>

      <div className="command-body">
        <aside className="command-sidebar">
          <div className="sidebar-section">
            <p>WORKSPACE</p>

            <button
              className="sidebar-link sidebar-link-active"
              type="button"
            >
              <span>01</span>
              Command centre
            </button>

            <button className="sidebar-link" type="button">
              <span>02</span>
              Investigation cases
            </button>

            <button className="sidebar-link" type="button">
              <span>03</span>
              New case intake
            </button>
          </div>

          <div className="sidebar-register">
            <p>SECURITY REGISTER</p>

            <div>
              <span className="status-indicator" />
              Authentication valid
            </div>

            <div>
              <span className="status-indicator" />
              Backend connected
            </div>

            <div>
              <span className="status-indicator status-amber" />
              Analytics pending
            </div>
          </div>
        </aside>

        <section className="command-content">
          <header className="command-heading">
            <div>
              <p className="section-code">
                OPERATIONAL OVERVIEW / DASHBOARD
              </p>

              <h1>
                Investigation command centre
              </h1>

              <p>
                Secure authentication is active. Live case analytics
                will be connected in the next checkpoint.
              </p>
            </div>

            <div className="command-classification">
              <span>SESSION STATUS</span>
              <strong>AUTHORIZED</strong>
            </div>
          </header>

          <div className="dossier-counter-grid">
            <article className="dossier-counter">
              <div>
                <span>CASES</span>
                <span>METRIC/01</span>
              </div>

              <strong>--</strong>
              <p>Total investigations</p>
            </article>

            <article className="dossier-counter">
              <div>
                <span>OPEN</span>
                <span>METRIC/02</span>
              </div>

              <strong>--</strong>
              <p>Active case files</p>
            </article>

            <article className="dossier-counter">
              <div>
                <span>EVIDENCE</span>
                <span>METRIC/03</span>
              </div>

              <strong>--</strong>
              <p>Registered evidence items</p>
            </article>

            <article className="dossier-counter risk-counter">
              <div>
                <span>HIGH RISK</span>
                <span>METRIC/04</span>
              </div>

              <strong>--</strong>
              <p>Flagged intelligence reports</p>
            </article>
          </div>

          <div className="command-panel-grid">
            <article className="command-panel">
              <header>
                <span>RECENT CASE FILES</span>
                <span>REGISTER A</span>
              </header>

              <div className="command-empty-state">
                <span className="empty-crosshair" />
                <h2>Analytics connection pending</h2>
                <p>
                  Recent case files will appear here after the
                  dashboard API is connected.
                </p>
              </div>
            </article>

            <article className="command-panel">
              <header>
                <span>WORKSPACE ACCESS</span>
                <span>REGISTER B</span>
              </header>

              <dl className="investigator-record">
                <div>
                  <dt>INVESTIGATOR</dt>
                  <dd>
                    {user?.fullName || "Not available"}
                  </dd>
                </div>

                <div>
                  <dt>EMAIL</dt>
                  <dd>{user?.email || "Not available"}</dd>
                </div>

                <div>
                  <dt>ROLE</dt>
                  <dd>{user?.role || "INVESTIGATOR"}</dd>
                </div>

                <div>
                  <dt>ACCOUNT</dt>
                  <dd>
                    {user?.active === false
                      ? "RESTRICTED"
                      : "ACTIVE"}
                  </dd>
                </div>
              </dl>
            </article>
          </div>
        </section>
      </div>
    </main>
  );
}