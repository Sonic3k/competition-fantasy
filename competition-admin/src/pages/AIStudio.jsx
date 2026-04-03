import { useState, useEffect } from 'react'
import api from '../api/client'

export default function AIStudio() {
  const [tab, setTab] = useState('generate')
  const [templates, setTemplates] = useState([])
  const [jobs, setJobs] = useState([])
  const [collections, setCollections] = useState([])
  const [status, setStatus] = useState({})

  const load = async () => {
    const [t, j, c, s] = await Promise.all([
      api.get('/ai/templates'), api.get('/ai/jobs'),
      api.get('/collections'), api.get('/ai/status')
    ])
    setTemplates(t.data); setJobs(j.data); setCollections(c.data); setStatus(s.data)
  }
  useEffect(() => { load() }, [])

  return (
    <div>
      <h1 style={{ marginBottom: 4 }}>AI Studio</h1>
      <p style={{ color: status.grokConfigured ? '#2ecc71' : '#e74c3c', fontSize: 12, marginBottom: 16 }}>
        Grok: {status.grokConfigured ? '✓ Connected' : '✗ Not configured'}
      </p>

      <div style={{ display: 'flex', gap: 0, marginBottom: 20, borderBottom: '2px solid #eee' }}>
        {['generate', 'templates', 'history'].map(t => (
          <button key={t} onClick={() => setTab(t)}
            style={{ padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer', fontWeight: 600,
              borderBottom: tab === t ? '2px solid #e94560' : '2px solid transparent', color: tab === t ? '#e94560' : '#999' }}>
            {t === 'generate' ? 'Generate' : t === 'templates' ? 'Templates' : 'History'}
          </button>
        ))}
      </div>

      {tab === 'generate' && <GenerateTab templates={templates} collections={collections} onDone={load} />}
      {tab === 'templates' && <TemplatesTab templates={templates} collections={collections} onDone={load} />}
      {tab === 'history' && <HistoryTab jobs={jobs} onDone={load} />}
    </div>
  )
}

function GenerateTab({ templates, collections, onDone }) {
  const [mode, setMode] = useState('custom')
  const [prompt, setPrompt] = useState('')
  const [templateId, setTemplateId] = useState('')
  const [variables, setVariables] = useState({})
  const [collectionId, setCollectionId] = useState('')
  const [n, setN] = useState(1)
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)

  const selectedTemplate = templates.find(t => t.id === templateId)
  const templateVars = selectedTemplate ? [...selectedTemplate.templateText.matchAll(/\{(\w+)\}/g)].map(m => m[1]) : []

  const generate = async () => {
    setLoading(true); setResult(null)
    try {
      const body = { n, collectionId: collectionId || null }
      if (mode === 'custom') { body.prompt = prompt }
      else { body.templateId = templateId; body.variables = variables }
      const r = await api.post('/ai/generate', body)
      setResult(r.data)
    } catch (err) { alert('Error: ' + (err.response?.data?.error || err.message)) }
    setLoading(false)
    onDone()
  }

  const saveResult = async (resultId) => {
    try {
      await api.post(`/ai/results/${resultId}/save${collectionId ? '?collectionId=' + collectionId : ''}`)
      // Refresh job
      if (result?.id) {
        const r = await api.get(`/ai/jobs/${result.id}`)
        setResult(r.data)
      }
      onDone()
    } catch (err) { alert('Save failed: ' + err.message) }
  }

  return (
    <div>
      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        <button onClick={() => setMode('custom')} style={{ ...chip, background: mode === 'custom' ? '#e94560' : '#fff', color: mode === 'custom' ? '#fff' : '#333' }}>Custom Prompt</button>
        <button onClick={() => setMode('template')} style={{ ...chip, background: mode === 'template' ? '#e94560' : '#fff', color: mode === 'template' ? '#fff' : '#333' }}>From Template</button>
      </div>

      {mode === 'custom' ? (
        <textarea value={prompt} onChange={e => setPrompt(e.target.value)} placeholder="Enter your prompt..." rows={4}
          style={{ width: '100%', padding: 12, border: '1px solid #ddd', borderRadius: 8, fontSize: 14, resize: 'vertical', marginBottom: 12 }} />
      ) : (
        <div style={{ marginBottom: 12 }}>
          <select value={templateId} onChange={e => { setTemplateId(e.target.value); setVariables({}) }} style={inputStyle}>
            <option value="">Select template...</option>
            {templates.map(t => <option key={t.id} value={t.id}>[{t.category}] {t.name}</option>)}
          </select>
          {selectedTemplate && (
            <div style={{ marginTop: 8, padding: 12, background: '#f9f9f9', borderRadius: 6, fontSize: 13 }}>
              <div style={{ color: '#999', marginBottom: 8 }}>{selectedTemplate.templateText}</div>
              {templateVars.map(v => (
                <input key={v} placeholder={v} value={variables[v] || ''} onChange={e => setVariables({ ...variables, [v]: e.target.value })}
                  style={{ ...inputStyle, marginRight: 8, marginBottom: 4 }} />
              ))}
            </div>
          )}
        </div>
      )}

      <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 16, flexWrap: 'wrap' }}>
        <select value={n} onChange={e => setN(parseInt(e.target.value))} style={inputStyle}>
          {[1, 2, 3, 4].map(i => <option key={i} value={i}>{i} image{i > 1 ? 's' : ''}</option>)}
        </select>
        <select value={collectionId} onChange={e => setCollectionId(e.target.value)} style={inputStyle}>
          <option value="">No collection</option>
          {collections.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <button onClick={generate} disabled={loading || (mode === 'custom' ? !prompt : !templateId)} style={btnStyle}>
          {loading ? 'Generating...' : 'Generate'}
        </button>
      </div>

      {result && (
        <div>
          <div style={{ fontSize: 12, color: result.status === 'COMPLETED' ? '#2ecc71' : '#e74c3c', marginBottom: 8 }}>
            {result.status} — {result.resultCount} result(s)
          </div>
          {result.error && <div style={{ color: '#e74c3c', fontSize: 13, marginBottom: 8 }}>{result.error}</div>}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 12 }}>
            {result.results?.map(r => (
              <div key={r.id} style={{ background: '#fff', borderRadius: 8, border: '1px solid #eee', overflow: 'hidden' }}>
                <img src={r.saved ? r.cdnUrl : r.imageUrl} alt="" style={{ width: '100%', height: 200, objectFit: 'cover' }} />
                <div style={{ padding: 8, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  {r.saved ? (
                    <span style={{ fontSize: 11, color: '#2ecc71', fontWeight: 600 }}>✓ Saved to library</span>
                  ) : (
                    <button onClick={() => saveResult(r.id)} style={{ ...btnStyle, fontSize: 11, padding: '4px 12px' }}>Save to Library</button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

function TemplatesTab({ templates, collections, onDone }) {
  const [form, setForm] = useState({ name: '', category: 'LOGO', templateText: '', defaultCollectionId: '' })

  const create = async (e) => {
    e.preventDefault()
    await api.post('/ai/templates', form)
    setForm({ name: '', category: 'LOGO', templateText: '', defaultCollectionId: '' })
    onDone()
  }

  return (
    <div>
      <form onSubmit={create} style={{ marginBottom: 20, padding: 16, background: '#fff', borderRadius: 8, border: '1px solid #eee' }}>
        <div style={{ display: 'flex', gap: 8, marginBottom: 8, flexWrap: 'wrap' }}>
          <input placeholder="Template name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} style={inputStyle} required />
          <select value={form.category} onChange={e => setForm({ ...form, category: e.target.value })} style={inputStyle}>
            {['LOGO', 'JERSEY', 'STADIUM', 'MATCH_POSTER', 'FLAG', 'ART', 'GENERAL'].map(c => <option key={c} value={c}>{c}</option>)}
          </select>
          <select value={form.defaultCollectionId} onChange={e => setForm({ ...form, defaultCollectionId: e.target.value })} style={inputStyle}>
            <option value="">No default collection</option>
            {collections.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>
        <textarea placeholder="Prompt template... Use {variable_name} for variables" value={form.templateText}
          onChange={e => setForm({ ...form, templateText: e.target.value })} rows={3}
          style={{ width: '100%', padding: 10, border: '1px solid #ddd', borderRadius: 6, fontSize: 13, resize: 'vertical', marginBottom: 8 }} />
        <button type="submit" style={btnStyle}>Create Template</button>
      </form>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {templates.map(t => (
          <div key={t.id} style={{ background: '#fff', padding: 12, borderRadius: 8, border: '1px solid #eee', position: 'relative' }}>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 6 }}>
              <span style={{ fontWeight: 600, fontSize: 14 }}>{t.name}</span>
              <span style={{ fontSize: 10, padding: '2px 6px', borderRadius: 3, background: '#f0e0ff', color: '#7b2d8e', fontWeight: 600 }}>{t.category}</span>
            </div>
            <div style={{ fontSize: 12, color: '#666', whiteSpace: 'pre-wrap' }}>{t.templateText}</div>
            <button onClick={async () => { await api.delete(`/ai/templates/${t.id}`); onDone() }}
              style={{ position: 'absolute', top: 8, right: 8, background: 'none', border: 'none', color: '#ddd', cursor: 'pointer' }}>×</button>
          </div>
        ))}
      </div>
    </div>
  )
}

function HistoryTab({ jobs, onDone }) {
  const [expanded, setExpanded] = useState(null)
  const [jobDetail, setJobDetail] = useState(null)

  const toggle = async (jobId) => {
    if (expanded === jobId) { setExpanded(null); return }
    setExpanded(jobId)
    const r = await api.get(`/ai/jobs/${jobId}`)
    setJobDetail(r.data)
  }

  const saveResult = async (resultId) => {
    await api.post(`/ai/results/${resultId}/save`)
    if (expanded) {
      const r = await api.get(`/ai/jobs/${expanded}`)
      setJobDetail(r.data)
    }
    onDone()
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      {jobs.map(j => (
        <div key={j.id}>
          <div onClick={() => toggle(j.id)} style={{ padding: 12, background: '#fff', borderRadius: 8, border: '1px solid #eee', cursor: 'pointer', display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <div style={{ fontSize: 13, fontWeight: 500 }}>{j.prompt?.substring(0, 80)}{j.prompt?.length > 80 ? '...' : ''}</div>
              <div style={{ fontSize: 11, color: '#999' }}>{new Date(j.createdAt).toLocaleString()} · {j.resultCount} results</div>
            </div>
            <span style={{ fontSize: 11, fontWeight: 600, color: j.status === 'COMPLETED' ? '#2ecc71' : j.status === 'FAILED' ? '#e74c3c' : '#f39c12' }}>{j.status}</span>
          </div>
          {expanded === j.id && jobDetail && (
            <div style={{ padding: 12, background: '#fafafa', borderRadius: '0 0 8px 8px' }}>
              <div style={{ fontSize: 12, color: '#666', marginBottom: 8 }}>{jobDetail.prompt}</div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))', gap: 8 }}>
                {jobDetail.results?.map(r => (
                  <div key={r.id} style={{ background: '#fff', borderRadius: 6, border: '1px solid #eee', overflow: 'hidden' }}>
                    <img src={r.saved ? r.cdnUrl : r.imageUrl} alt="" style={{ width: '100%', height: 140, objectFit: 'cover' }} />
                    <div style={{ padding: 6, textAlign: 'center' }}>
                      {r.saved ? (
                        <span style={{ fontSize: 10, color: '#2ecc71' }}>✓ In library</span>
                      ) : (
                        <button onClick={() => saveResult(r.id)} style={{ fontSize: 10, padding: '3px 10px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}>Save</button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      ))}
      {jobs.length === 0 && <p style={{ color: '#999' }}>No generations yet.</p>}
    </div>
  )
}

const inputStyle = { padding: '6px 10px', border: '1px solid #ddd', borderRadius: 6, fontSize: 13 }
const btnStyle = { padding: '8px 16px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 6, cursor: 'pointer', fontWeight: 600, fontSize: 13 }
const chip = { padding: '8px 14px', border: '1px solid #eee', borderRadius: 20, cursor: 'pointer', fontSize: 13, fontWeight: 500 }
