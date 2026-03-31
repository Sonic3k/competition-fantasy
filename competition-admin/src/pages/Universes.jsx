import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'

export default function Universes() {
  const [universes, setUniverses] = useState([])
  const [form, setForm] = useState({ name: '', description: '' })
  const [loading, setLoading] = useState(true)

  const load = () => api.get('/universes').then(r => { setUniverses(r.data); setLoading(false) })
  useEffect(() => { load() }, [])

  const create = async (e) => {
    e.preventDefault()
    if (!form.name.trim()) return
    await api.post('/universes', form)
    setForm({ name: '', description: '' })
    load()
  }

  const remove = async (id) => {
    if (!confirm('Delete this universe and ALL its data?')) return
    await api.delete(`/universes/${id}`)
    load()
  }

  if (loading) return <p>Loading...</p>

  return (
    <div>
      <h1 style={{ marginTop: 0 }}>Universes</h1>

      <form onSubmit={create} style={{ display: 'flex', gap: 10, marginBottom: 30 }}>
        <input placeholder="Universe name" value={form.name}
          onChange={e => setForm({ ...form, name: e.target.value })}
          style={inputStyle} />
        <input placeholder="Description" value={form.description}
          onChange={e => setForm({ ...form, description: e.target.value })}
          style={{ ...inputStyle, flex: 2 }} />
        <button type="submit" style={btnStyle}>Create</button>
      </form>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 16 }}>
        {universes.map(u => (
          <div key={u.id} style={cardStyle}>
            <Link to={`/universes/${u.id}`} style={{ textDecoration: 'none', color: '#1a1a2e' }}>
              <h3 style={{ margin: '0 0 8px' }}>{u.name}</h3>
              <p style={{ margin: 0, color: '#666', fontSize: 14 }}>{u.description || 'No description'}</p>
            </Link>
            <button onClick={() => remove(u.id)} style={deleteBtnStyle}>Delete</button>
          </div>
        ))}
        {universes.length === 0 && <p style={{ color: '#999' }}>No universes yet. Create one above!</p>}
      </div>
    </div>
  )
}

const inputStyle = { padding: '8px 12px', border: '1px solid #ddd', borderRadius: 6, fontSize: 14, flex: 1 }
const btnStyle = { padding: '8px 20px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 6, cursor: 'pointer', fontWeight: 600 }
const cardStyle = { background: '#fff', padding: 20, borderRadius: 10, boxShadow: '0 2px 8px rgba(0,0,0,0.08)', position: 'relative' }
const deleteBtnStyle = { position: 'absolute', top: 10, right: 10, background: 'none', border: 'none', color: '#ccc', cursor: 'pointer', fontSize: 12 }
