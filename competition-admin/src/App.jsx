import { useState } from 'react'
import { Routes, Route, Link, useLocation } from 'react-router-dom'
import Universes from './pages/Universes'
import UniverseDetail from './pages/UniverseDetail'
import SeasonDetail from './pages/SeasonDetail'
import ScriptsManager from './pages/ScriptsManager'

export default function App() {
  const [menuOpen, setMenuOpen] = useState(false)
  const location = useLocation()
  const close = () => setMenuOpen(false)

  return (
    <div className="app-layout">
      <div className="mobile-header">
        <button onClick={() => setMenuOpen(!menuOpen)} className="hamburger">☰</button>
        <span style={{ fontWeight: 700, color: '#e94560' }}>Fantasy Admin</span>
      </div>

      {menuOpen && <div className="overlay" onClick={close} />}

      <nav className={`sidebar ${menuOpen ? 'open' : ''}`}>
        <div style={{ padding: '0 20px', fontSize: 18, fontWeight: 700, marginBottom: 20, color: '#e94560' }}>Fantasy Admin</div>
        {[['/', 'Universes'], ['/scripts', 'Scripts']].map(([to, label]) => (
          <Link key={to} to={to} onClick={close}
            className={`nav-link ${location.pathname === to ? 'active' : ''}`}>
            {label}
          </Link>
        ))}
      </nav>

      <main className="main-content">
        <Routes>
          <Route path="/" element={<Universes />} />
          <Route path="/universes/:id" element={<UniverseDetail />} />
          <Route path="/seasons/:id" element={<SeasonDetail />} />
          <Route path="/scripts" element={<ScriptsManager />} />
        </Routes>
      </main>
    </div>
  )
}
