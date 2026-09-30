import { Navigate, Route, Routes } from 'react-router'
import { ViewerLayout } from './viewer/ViewerLayout'
import { HomePage } from './viewer/pages/HomePage'
import { UniversePage } from './viewer/pages/UniversePage'
import { CompetitionPage } from './viewer/pages/CompetitionPage'
import { SeasonPage } from './viewer/pages/SeasonPage'
import { TeamPage } from './viewer/pages/TeamPage'
import { NationPage } from './viewer/pages/NationPage'
import { AdminLayout, RequireAuth } from './admin/AdminLayout'
import { LoginPage } from './admin/pages/LoginPage'
import { AdminHome } from './admin/pages/AdminHome'
import { UniverseAdmin } from './admin/pages/UniverseAdmin'
import { TeamAdmin } from './admin/pages/TeamAdmin'
import { CompetitionAdmin } from './admin/pages/CompetitionAdmin'
import { SeasonWorkspace } from './admin/pages/SeasonWorkspace'
import { PresetsAdmin } from './admin/pages/PresetsAdmin'
import { ScriptManager } from './admin/pages/ScriptManager'
import { AssetsAdmin } from './admin/pages/AssetsAdmin'

export default function App() {
  return (
    <Routes>
      <Route element={<ViewerLayout />}>
        <Route index element={<HomePage />} />
        <Route path="u/:key" element={<UniversePage />} />
        <Route path="competitions/:id" element={<CompetitionPage />} />
        <Route path="seasons/:id" element={<SeasonPage />} />
        <Route path="teams/:id" element={<TeamPage />} />
        <Route path="nations/:id" element={<NationPage />} />
      </Route>
      <Route path="admin/login" element={<LoginPage />} />
      <Route path="admin" element={<RequireAuth />}>
        <Route element={<AdminLayout />}>
          <Route index element={<AdminHome />} />
          <Route path="universes/:key" element={<UniverseAdmin />} />
          <Route path="teams/:id" element={<TeamAdmin />} />
          <Route path="competitions/:id" element={<CompetitionAdmin />} />
          <Route path="seasons/:id" element={<SeasonWorkspace />} />
          <Route path="presets" element={<PresetsAdmin />} />
          <Route path="imports" element={<ScriptManager />} />
          <Route path="assets" element={<AssetsAdmin />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
