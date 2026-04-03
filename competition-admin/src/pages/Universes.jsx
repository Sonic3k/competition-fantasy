import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'
import ImageUpload from '../components/ImageUpload'

export default function Universes() {
  const [universes, setUniverses] = useState([])
  const [form, setForm] = useState({ name: '', description: '' })
  const [showForm, setShowForm] = useState(false)
  const [loading, setLoading] = useState(true)

  const load = () => api.get('/universes').then(r => { setUniverses(r.data); setLoading(false) })
  useEffect(() => { load() }, [])

  const create = async () => {
    if (!form.name.trim()) return alert('Enter a name')
    try {
      await api.post('/universes', form)
      setForm({ name: '', description: '' })
      setShowForm(false)
      load()
    } catch (err) {
      alert('Error: ' + (err.response?.data?.message || err.message))
    }
  }

  const remove = async (id) => {
    if (!confirm('Delete this universe and ALL its data?')) return
    await api.delete(`/universes/${id}`)
    load()
  }

  if (loading) return <p>Loading...</p>

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h1 style={{ margin: 0 }}>Universes</h1>
        <button onClick={() => setShowForm(!showForm)} style={primaryBtn}>
          {showForm ? '✕ Cancel' : '+ New Universe'}
        </button>
      </div>

      {showForm && (
        <div style={formCard}>
          <h3 style={{ margin: '0 0 16px', fontSize: 16 }}>Create New Universe</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            <input placeholder="Universe name *" value={form.name}
              onChange={e => setForm({ ...form, name: e.target.value })}
              style={inputStyle} />
            <input placeholder="Description (optional)" value={form.description}
              onChange={e => setForm({ ...form, description: e.target.value })}
              style={inputStyle} />
            <button onClick={create} style={primaryBtn}>Create Universe</button>
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 16 }}>
        {universes.map(u => (
          <div key={u.id} style={cardStyle}>
            <div style={{ display: 'flex', gap: 14, alignItems: 'center' }}>
              <ImageUpload endpoint={`/upload/universe/${u.id}`} currentUrl={u.avatarUrl} onUploaded={() => load()} size={56} />
              <Link to={`/universes/${u.id}`} style={{ textDecoration: 'none', color: '#1a1a2e', flex: 1 }}>
                <h3 style={{ margin: '0 0 4px' }}>{u.name}</h3>
                <p style={{ margin: 0, color: '#666', fontSize: 13 }}>{u.description || 'No description'}</p>
              </Link>
            </div>
            <button onClick={() => remove(u.id)} style={deleteBtnStyle}>✕</button>
          </div>
        ))}
        {universes.length === 0 && !showForm && (
          <p style={{ color: '#999' }}>No universes yet. Click "+ New Universe" to get started.</p>
        )}
      </div>
    </div>
  )
}

const inputStyle = { padding: '10px 14px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14, width: '100%' }
const primaryBtn = { padding: '10px 20px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600, fontSize: 14 }
const formCard = { background: '#fff', padding: 24, borderRadius: 12, boxShadow: '0 2px 12px rgba(0,0,0,0.08)', marginBottom: 24, maxWidth: 480 }
const cardStyle = { background: '#fff', padding: 20, borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.06)', position: 'relative', cursor: 'pointer', transition: 'box-shadow 0.2s' }
const deleteBtnStyle = { position: 'absolute', top: 12, right: 12, background: 'none', border: 'none', color: '#ccc', cursor: 'pointer', fontSize: 16 }
