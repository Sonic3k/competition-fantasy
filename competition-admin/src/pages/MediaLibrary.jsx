import { useState, useEffect } from 'react'
import api from '../api/client'

export default function MediaLibrary() {
  const [collections, setCollections] = useState([])
  const [selectedCollection, setSelectedCollection] = useState(null)
  const [media, setMedia] = useState([])
  const [tags, setTags] = useState([])
  const [collectionForm, setCollectionForm] = useState({ name: '', type: 'GENERAL', description: '' })
  const [showForm, setShowForm] = useState(false)
  const [parentId, setParentId] = useState(null)
  const [breadcrumb, setBreadcrumb] = useState([])

  const loadCollections = async (pid) => {
    const params = pid ? `?parentId=${pid}` : ''
    const r = await api.get(`/collections${params}`)
    setCollections(r.data)
  }

  const loadMedia = async (collId) => {
    if (!collId) { setMedia([]); return }
    const r = await api.get(`/media?collectionId=${collId}`)
    setMedia(r.data)
  }

  const loadTags = async () => {
    const r = await api.get('/tags')
    setTags(r.data)
  }

  useEffect(() => { loadCollections(null); loadTags() }, [])

  const navigate = async (col) => {
    if (col) {
      setParentId(col.id)
      setSelectedCollection(col)
      setBreadcrumb(prev => [...prev, col])
      await loadCollections(col.id)
      await loadMedia(col.id)
    } else {
      setParentId(null)
      setSelectedCollection(null)
      setBreadcrumb([])
      await loadCollections(null)
      setMedia([])
    }
  }

  const goBack = async (idx) => {
    if (idx < 0) { navigate(null); return }
    const col = breadcrumb[idx]
    setBreadcrumb(breadcrumb.slice(0, idx + 1))
    setParentId(col.id)
    setSelectedCollection(col)
    await loadCollections(col.id)
    await loadMedia(col.id)
  }

  const createCollection = async (e) => {
    e.preventDefault()
    await api.post('/collections', { ...collectionForm, parentId })
    setCollectionForm({ name: '', type: 'GENERAL', description: '' })
    setShowForm(false)
    loadCollections(parentId)
  }

  const uploadFile = async (e) => {
    const file = e.target.files[0]
    if (!file || !selectedCollection) return
    const form = new FormData()
    form.append('file', file)
    if (selectedCollection) form.append('collectionId', selectedCollection.id)
    await api.post('/media/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
    loadMedia(selectedCollection.id)
    e.target.value = ''
  }

  const deleteMedia = async (id) => {
    if (!confirm('Delete this file?')) return
    await api.delete(`/media/${id}`)
    loadMedia(selectedCollection?.id)
  }

  const deleteCollection = async (id) => {
    if (!confirm('Delete this collection?')) return
    await api.delete(`/collections/${id}`)
    loadCollections(parentId)
  }

  return (
    <div>
      <h1 style={{ marginBottom: 8 }}>Media Library</h1>

      {/* Breadcrumb */}
      <div style={{ display: 'flex', gap: 6, alignItems: 'center', marginBottom: 16, fontSize: 13, flexWrap: 'wrap' }}>
        <span onClick={() => navigate(null)} style={crumbStyle}>All</span>
        {breadcrumb.map((b, i) => (
          <span key={b.id}>
            <span style={{ color: '#ccc' }}>/</span>
            <span onClick={() => goBack(i)} style={crumbStyle}>{b.name}</span>
          </span>
        ))}
      </div>

      {/* Actions */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 16, flexWrap: 'wrap' }}>
        <button onClick={() => setShowForm(!showForm)} style={btnStyle}>{showForm ? 'Cancel' : '+ Collection'}</button>
        {selectedCollection && (
          <label style={{ ...btnStyle, background: '#2ecc71' }}>
            Upload File
            <input type="file" onChange={uploadFile} accept="image/*" style={{ display: 'none' }} />
          </label>
        )}
      </div>

      {showForm && (
        <form onSubmit={createCollection} style={{ display: 'flex', gap: 8, marginBottom: 16, flexWrap: 'wrap' }}>
          <input placeholder="Collection name" value={collectionForm.name} onChange={e => setCollectionForm({ ...collectionForm, name: e.target.value })} style={inputStyle} required />
          <select value={collectionForm.type} onChange={e => setCollectionForm({ ...collectionForm, type: e.target.value })} style={inputStyle}>
            {['GENERAL', 'LOGO', 'JERSEY', 'STADIUM', 'MATCH_POSTER', 'FLAG', 'ART'].map(t => <option key={t} value={t}>{t}</option>)}
          </select>
          <button type="submit" style={btnStyle}>Create</button>
        </form>
      )}

      {/* Collections grid */}
      {collections.length > 0 && (
        <div style={{ marginBottom: 20 }}>
          <div style={{ fontSize: 12, color: '#999', marginBottom: 8, fontWeight: 600 }}>COLLECTIONS</div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
            {collections.map(c => (
              <div key={c.id} onClick={() => navigate(c)}
                style={{ padding: '12px 16px', background: '#fff', borderRadius: 8, border: '1px solid #eee', cursor: 'pointer', minWidth: 140, position: 'relative' }}>
                <div style={{ fontWeight: 600, fontSize: 14 }}>{c.name}</div>
                <div style={{ fontSize: 11, color: '#999' }}>
                  {c.type !== 'GENERAL' && <span style={typeBadge}>{c.type}</span>}
                  {c.childCount > 0 && <span>{c.childCount} sub</span>}
                  {c.mediaCount > 0 && <span> · {c.mediaCount} files</span>}
                </div>
                <button onClick={(e) => { e.stopPropagation(); deleteCollection(c.id) }}
                  style={{ position: 'absolute', top: 4, right: 8, background: 'none', border: 'none', color: '#ddd', cursor: 'pointer' }}>×</button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Media grid */}
      {media.length > 0 && (
        <div>
          <div style={{ fontSize: 12, color: '#999', marginBottom: 8, fontWeight: 600 }}>FILES ({media.length})</div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(140px, 1fr))', gap: 10 }}>
            {media.map(m => (
              <div key={m.id} style={{ background: '#fff', borderRadius: 8, border: '1px solid #eee', overflow: 'hidden', position: 'relative' }}>
                <img src={m.cdnUrl} alt={m.filename} style={{ width: '100%', height: 120, objectFit: 'cover' }} />
                <div style={{ padding: '6px 8px' }}>
                  <div style={{ fontSize: 11, color: '#666', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{m.filename}</div>
                  <div style={{ display: 'flex', gap: 4, marginTop: 4, flexWrap: 'wrap' }}>
                    {m.tags?.map(t => <span key={t.id} style={{ fontSize: 9, padding: '1px 5px', borderRadius: 3, background: t.color || '#eee', color: '#fff' }}>{t.name}</span>)}
                  </div>
                  {m.sourceType === 'AI_GENERATED' && <span style={{ fontSize: 9, color: '#e94560' }}>AI</span>}
                </div>
                <button onClick={() => deleteMedia(m.id)}
                  style={{ position: 'absolute', top: 4, right: 4, background: 'rgba(0,0,0,0.5)', border: 'none', color: '#fff', borderRadius: '50%', width: 20, height: 20, cursor: 'pointer', fontSize: 11 }}>×</button>
              </div>
            ))}
          </div>
        </div>
      )}

      {collections.length === 0 && media.length === 0 && (
        <p style={{ color: '#999' }}>Empty. Create a collection or navigate to one to upload files.</p>
      )}
    </div>
  )
}

const inputStyle = { padding: '6px 10px', border: '1px solid #ddd', borderRadius: 6, fontSize: 13 }
const btnStyle = { padding: '8px 16px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 6, cursor: 'pointer', fontWeight: 600, fontSize: 13 }
const crumbStyle = { cursor: 'pointer', color: '#e94560', fontWeight: 500 }
const typeBadge = { display: 'inline-block', padding: '1px 5px', borderRadius: 3, fontSize: 9, fontWeight: 600, background: '#f0e0ff', color: '#7b2d8e', marginRight: 6 }
