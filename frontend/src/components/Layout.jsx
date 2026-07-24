import { useState } from "react";
import {
  NavLink,
  Outlet,
  useNavigate,
} from "react-router-dom";

import TraceLensMark from "./TraceLensMark";
import { useAuth } from "../context/useAuth";

function getNavLinkClass({ isActive }) {
  return isActive
    ? "sidebar-link sidebar-link-active"
    : "sidebar-link";
}

export default function Layout() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const [isMenuOpen, setIsMenuOpen] = useState(false);

  function closeMobileMenu() {
    setIsMenuOpen(false);
  }

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

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

        <button
          className="mobile-menu-button"
          type="button"
          onClick={() =>
            setIsMenuOpen((currentValue) => !currentValue)
          }
          aria-expanded={isMenuOpen}
          aria-controls="command-navigation"
        >
          {isMenuOpen ? "CLOSE MENU" : "OPEN MENU"}
        </button>

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
            onClick={handleLogout}
          >
            TERMINATE SESSION
          </button>
        </div>
      </header>

      <div className="command-body">
        <aside
          id="command-navigation"
          className={
            isMenuOpen
              ? "command-sidebar command-sidebar-open"
              : "command-sidebar"
          }
        >
          <nav
            className="sidebar-section"
            aria-label="Primary navigation"
          >
            <p>WORKSPACE</p>

            <NavLink
              className={getNavLinkClass}
              to="/dashboard"
              onClick={closeMobileMenu}
            >
              <span>01</span>
              Command centre
            </NavLink>

            <NavLink
              className={getNavLinkClass}
              to="/cases"
              onClick={closeMobileMenu}
            >
              <span>02</span>
              Investigation cases
            </NavLink>

            <NavLink
              className={getNavLinkClass}
              to="/cases/new"
              onClick={closeMobileMenu}
            >
              <span>03</span>
              New case intake
            </NavLink>
          </nav>

          <div className="sidebar-register">
            <p>SECURITY REGISTER</p>

            <div>
              <span className="status-indicator" />
              Investigator authenticated
            </div>

            <div>
              <span className="status-indicator" />
              JWT session active
            </div>

            <div>
              <span className="status-indicator status-amber" />
              Restricted workspace
            </div>
          </div>
        </aside>

        <section className="command-content">
          <Outlet />
        </section>
      </div>
    </main>
  );
}