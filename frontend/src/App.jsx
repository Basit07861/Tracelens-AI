import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";
import { AuthProvider } from "./context/AuthContext";
import CaseDetailsPage from "./pages/CaseDetailsPage";
import CasesPage from "./pages/CasesPage";
import DashboardPage from "./pages/DashboardPage";
import LoginPage from "./pages/LoginPage";
import NewCasePage from "./pages/NewCasePage";
import RegisterPage from "./pages/RegisterPage";

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route
            path="/register"
            element={<RegisterPage />}
          />

          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route
                path="/dashboard"
                element={<DashboardPage />}
              />

              <Route
                path="/cases"
                element={<CasesPage />}
              />

              <Route
                path="/cases/new"
                element={<NewCasePage />}
              />

              <Route
                path="/cases/:caseId"
                element={<CaseDetailsPage />}
              />
            </Route>
          </Route>

          <Route
            path="/"
            element={<Navigate to="/dashboard" replace />}
          />

          <Route
            path="*"
            element={<Navigate to="/dashboard" replace />}
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}