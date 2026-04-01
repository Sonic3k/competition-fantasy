import { useState, useEffect } from 'react'
import api from '../api/client'

export default function ScriptsManager() {
  const [scripts, setScripts] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', description: '', sqlContent: '' })
  const [expanded, setExpanded] = useState(null)
  const [executing, setExecuting] = useState(null)

  const load = () => api.get('/scripts').then(r => setScripts(r.data))
  useEffect(() => { load() }, [])

  const create = async () => {
    if (!form.name.trim() || !form.sqlContent.trim()) return alert('Name and SQL required')
    try {
      await api.post('/scripts', form)
      setForm({ name: '', description: '', sqlContent: '' })
      setShowForm(false)
      load()
    } catch (err) { alert('Error: ' + err.message) }
  }

  const execute = async (id) => {
    if (!confirm('Execute this script? This cannot be undone.')) return
    setExecuting(id)
    try {
      await api.post(`/scripts/${id}/execute`)
      load()
    } catch (err) { alert('Error: ' + err.message) }
    setExecuting(null)
  }

  const remove = async (id) => {
    if (!confirm('Delete this script?')) return
    await api.delete(`/scripts/${id}`)
    load()
  }

  const statusColor = { PENDING: '#f39c12', EXECUTED: '#2ecc71', FAILED: '#e74c3c' }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h1 style={{ margin: 0 }}>Scripts Manager</h1>
        <button onClick={() => setShowForm(!showForm)} style={primaryBtn}>
          {showForm ? '✕ Cancel' : '+ New Script'}
        </button>
      </div>

      {showForm && (
        <div style={formCard}>
          <h3 style={{ margin: '0 0 16px', fontSize: 16 }}>Create Import Script</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            <input placeholder="Script name *" value={form.name}
              onChange={e => setForm({ ...form, name: e.target.value })} style={inputStyle} />
            <input placeholder="Description (optional)" value={form.description}
              onChange={e => setForm({ ...form, description: e.target.value })} style={inputStyle} />
            <textarea placeholder="SQL content *" value={form.sqlContent}
              onChange={e => setForm({ ...form, sqlContent: e.target.value })}
              style={{ ...inputStyle, minHeight: 200, fontFamily: 'monospace', fontSize: 13 }} />
            <button onClick={create} style={primaryBtn}>Save Script</button>
          </div>
        </div>
      )}

      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {scripts.map(s => (
          <div key={s.id} style={{ background: '#fff', borderRadius: 10, border: '1px solid #eee', overflow: 'hidden' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 18px', cursor: 'pointer' }}
              onClick={() => setExpanded(expanded === s.id ? null : s.id)}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <span style={{ display: 'inline-block', width: 10, height: 10, borderRadius: '50%', background: statusColor[s.status] }} />
                <strong>{s.name}</strong>
                <span style={{ fontSize: 12, color: '#999' }}>{s.status}</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <button onClick={(e) => { e.stopPropagation(); execute(s.id) }}
                    disabled={executing === s.id}
                    style={{ ...primaryBtn, fontSize: 12, padding: '6px 14px', background: s.status === 'EXECUTED' ? '#27ae60' : s.status === 'FAILED' ? '#e67e22' : '#e94560' }}>
                    {executing === s.id ? 'Executing...' : s.status === 'EXECUTED' ? 'Re-execute' : s.status === 'FAILED' ? 'Retry' : 'Execute'}
                </button>
                <button onClick={(e) => { e.stopPropagation(); remove(s.id) }}
                    style={{ background: 'none', border: 'none', color: '#ccc', cursor: 'pointer', fontSize: 16 }}>✕</button>
                <span style={{ color: '#ccc' }}>{expanded === s.id ? '▾' : '▸'}</span>
              </div>
            </div>

            {expanded === s.id && (
              <div style={{ padding: '0 18px 18px', borderTop: '1px solid #f0f0f0' }}>
                {s.description && <p style={{ color: '#666', fontSize: 13, margin: '12px 0 8px' }}>{s.description}</p>}
                {s.errorMessage && <div style={{ background: '#fdf0f0', padding: '10px 14px', borderRadius: 6, color: '#c0392b', fontSize: 13, marginTop: 10 }}>{s.errorMessage}</div>}
                {s.executedAt && <p style={{ color: '#999', fontSize: 12, margin: '8px 0' }}>Executed: {new Date(s.executedAt).toLocaleString()}</p>}
                <pre style={{ background: '#f8f9fa', padding: 14, borderRadius: 6, fontSize: 12, overflow: 'auto', maxHeight: 400, margin: '10px 0 0' }}>{s.sqlContent}</pre>
              </div>
            )}
          </div>
        ))}
        {scripts.length === 0 && <p style={{ color: '#999' }}>No scripts yet. Click "+ New Script" to create one.</p>}
      </div>
    </div>
  )
}

const inputStyle = { padding: '10px 14px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14, width: '100%' }
const primaryBtn = { padding: '10px 20px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600, fontSize: 14 }
const formCard = { background: '#fff', padding: 24, borderRadius: 12, boxShadow: '0 2px 12px rgba(0,0,0,0.08)', marginBottom: 24 }
