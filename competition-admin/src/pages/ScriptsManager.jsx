import { useState, useEffect } from 'react'
import api from '../api/client'

export default function ScriptsManager() {
  const [scripts, setScripts] = useState([])
  const [running, setRunning] = useState(null)

  const load = () => api.get('/scripts').then(r => setScripts(r.data))
  useEffect(() => { load() }, [])

  const run = async (id) => {
    if (!confirm('Run this script?')) return
    setRunning(id)
    try {
      const r = await api.post(`/scripts/${id}/run`)
      if (r.data.status === 'FAILED') alert('Failed: ' + r.data.error)
      load()
    } catch (err) { alert('Error: ' + err.message) }
    setRunning(null)
  }

  const statusColor = { EXECUTED: '#2ecc71', FAILED: '#e74c3c' }

  return (
    <div>
      <h1 style={{ margin: '0 0 8px' }}>Scripts</h1>
      <p style={{ color: '#888', marginTop: 0, marginBottom: 24, fontSize: 14 }}>One-off data imports and migrations. Each script is defined in code.</p>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
        {scripts.map(s => (
          <div key={s.id} style={cardStyle}>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 6 }}>
                <span style={{ fontSize: 20 }}>{s.icon}</span>
                <strong style={{ fontSize: 15 }}>{s.name}</strong>
                {s.lastStatus && (
                  <span style={{ fontSize: 11, color: statusColor[s.lastStatus] || '#999', fontWeight: 600 }}>
                    {s.lastStatus}
                  </span>
                )}
              </div>
              <p style={{ margin: 0, color: '#888', fontSize: 13, lineHeight: 1.5 }}>{s.description}</p>
              {s.lastExecutedAt && <p style={{ margin: '6px 0 0', color: '#aaa', fontSize: 11 }}>Last run: {new Date(s.lastExecutedAt).toLocaleString()}</p>}
              {s.lastError && <p style={{ margin: '6px 0 0', color: '#e74c3c', fontSize: 12 }}>{s.lastError}</p>}
            </div>
            <button onClick={() => run(s.id)} disabled={running === s.id}
              style={runBtn}>
              {running === s.id ? '...' : '▶ Run'}
            </button>
          </div>
        ))}
        {scripts.length === 0 && <p style={{ color: '#999' }}>No scripts defined.</p>}
      </div>
    </div>
  )
}

const cardStyle = { display: 'flex', alignItems: 'center', gap: 20, background: '#fff', padding: '18px 22px', borderRadius: 12, border: '1px solid #eee' }
const runBtn = { padding: '10px 24px', background: '#1a1a2e', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600, fontSize: 14, whiteSpace: 'nowrap' }
