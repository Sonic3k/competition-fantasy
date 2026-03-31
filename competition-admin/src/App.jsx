import { Routes, Route, Link, useParams, useNavigate } from 'react-router-dom'
import Universes from './pages/Universes'
import UniverseDetail from './pages/UniverseDetail'
import SeasonDetail from './pages/SeasonDetail'

const styles = {
  app: { display: 'flex', minHeight: '100vh', fontFamily: 'system-ui, sans-serif', margin: 0 },
  sidebar: { width: 220, background: '#1a1a2e', color: '#fff', padding: '20px 0' },
  sidebarTitle: { padding: '0 20px', fontSize: 18, fontWeight: 700, marginBottom: 20, color: '#e94560' },
  navLink: { display: 'block', padding: '10px 20px', color: '#ccc', textDecoration: 'none', fontSize: 14 },
  main: { flex: 1, padding: 30, background: '#f5f5f5' },
}

export default function App() {
  return (
    <div style={styles.app}>
      <nav style={styles.sidebar}>
        <div style={styles.sidebarTitle}>Fantasy Admin</div>
        <Link to="/" style={styles.navLink}>Universes</Link>
      </nav>
      <main style={styles.main}>
        <Routes>
          <Route path="/" element={<Universes />} />
          <Route path="/universes/:id" element={<UniverseDetail />} />
          <Route path="/seasons/:id" element={<SeasonDetail />} />
        </Routes>
      </main>
    </div>
  )
}
