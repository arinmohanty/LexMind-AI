import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AppProviders } from "@/app/providers/AppProviders";
import { AppShell } from "@/app/layout/AppShell";
import { ProtectedRoute } from "@/routes/ProtectedRoute";
import { LoginPage } from "@/features/auth/LoginPage";
import { RegisterPage } from "@/features/auth/RegisterPage";
import { DashboardHome } from "@/features/dashboard/DashboardHome";
import { CasesListPage } from "@/features/cases/CasesListPage";
import { CreateCasePage } from "@/features/cases/CreateCasePage";
import { CaseWorkspace } from "@/features/dashboard/CaseWorkspace";
import { IracPage } from "@/features/dashboard/IracPage";
import { AnalyticsCenterPage } from "@/features/analytics/AnalyticsCenterPage";
import { LandingPage } from "@/pages/public/LandingPage";
import { NotFoundPage } from "@/pages/NotFoundPage";
import { PlaceholderPage } from "@/pages/PlaceholderPage";

export default function App() {
  return (
    <AppProviders>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/app" element={<AppShell />}>
              <Route index element={<Navigate to="/app/dashboard" replace />} />
              <Route path="dashboard" element={<DashboardHome />} />
              <Route path="cases" element={<CasesListPage />} />
              <Route path="cases/new" element={<CreateCasePage />} />
              <Route path="cases/:caseId/analysis" element={<CaseWorkspace />} />
              <Route path="cases/:caseId/irac" element={<IracPage />} />
              <Route path="research" element={<PlaceholderPage title="Research" />} />
              <Route path="analytics" element={<AnalyticsCenterPage />} />
              <Route path="notes" element={<PlaceholderPage title="Research Notes" />} />
              <Route path="settings" element={<PlaceholderPage title="Settings" />} />
              <Route path="admin/*" element={<PlaceholderPage title="Admin" />} />
            </Route>
          </Route>

          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </BrowserRouter>
    </AppProviders>
  );
}
