import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/client'

export default function UniverseDetail() {
  const { id } = useParams()
  const [universe, setUniverse] = useState(null)
  const [teams, setTeams] = useState([])
  const [nations, setNations] = useState([])
  const [competitions, setCompetitions] = useState([])
  const [tab, setTab] = useState('teams')

  const load = async () => {
    const [u, t, n, c] = await Promise.all([
      api.get(`/universes/${id}`),
      api.get(`/teams?universeId=${id}`),
      api.get(`/nations?universeId=${id}`),
      api.get(`/competitions?universeId=${id}`),
    ])
    setUniverse(u.data); setTeams(t.data); setNations(n.data); setCompetitions(c.data)
  }
  useEffect(() => { load() }, [id])

  if (!universe) return <p>Loading...</p>

  return (
    <div>
      <Link to="/" style={{ color: '#e94560', textDecoration: 'none', fontSize: 14 }}>&larr; All Universes</Link>
      <h1 style={{ marginBottom: 5 }}>{universe.name}</h1>
      <p style={{ color: '#666', marginTop: 0 }}>{universe.description}</p>

      <div style={{ display: 'flex', gap: 0, marginBottom: 20, borderBottom: '2px solid #eee' }}>
        {['teams', 'nations', 'competitions'].map(t => (
          <button key={t} onClick={() => setTab(t)}
            style={{ padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer', fontWeight: 600,
              borderBottom: tab === t ? '2px solid #e94560' : '2px solid transparent',
              color: tab === t ? '#e94560' : '#999' }}>
            {t.charAt(0).toUpperCase() + t.slice(1)} ({t === 'teams' ? teams.length : t === 'nations' ? nations.length : competitions.length})
          </button>
        ))}
      </div>

      {tab === 'teams' && <TeamsTab universeId={id} teams={teams} nations={nations} reload={load} />}
      {tab === 'nations' && <NationsTab universeId={id} nations={nations} reload={load} />}
      {tab === 'competitions' && <CompetitionsTab universeId={id} competitions={competitions} reload={load} />}
    </div>
  )
}

function TeamsTab({ universeId, teams, nations, reload }) {
  const [form, setForm] = useState({ name: '', shortName: '', type: 'CLUB', primaryColor: '#003366', secondaryColor: '#ffffff', nationId: '' })

  const create = async (e) => {
    e.preventDefault()
    const params = new URLSearchParams({ universeId })
    if (form.nationId) params.append('nationId', form.nationId)
    await api.post(`/teams?${params}`, form)
    setForm({ ...form, name: '', shortName: '' })
    reload()
  }

  const remove = async (id) => {
    await api.delete(`/teams/${id}`)
    reload()
  }

  return (
    <div>
      <form onSubmit={create} style={{ display: 'flex', gap: 8, marginBottom: 20, flexWrap: 'wrap' }}>
        <input placeholder="Team name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} style={inputStyle} required />
        <input placeholder="Short" value={form.shortName} onChange={e => setForm({ ...form, shortName: e.target.value })} style={{ ...inputStyle, width: 60 }} />
        <select value={form.type} onChange={e => setForm({ ...form, type: e.target.value })} style={inputStyle}>
          <option value="CLUB">Club</option>
          <option value="NATIONAL">National</option>
        </select>
        {nations.length > 0 && (
          <select value={form.nationId} onChange={e => setForm({ ...form, nationId: e.target.value })} style={inputStyle}>
            <option value="">No nation</option>
            {nations.map(n => <option key={n.id} value={n.id}>{n.name}</option>)}
          </select>
        )}
        <input type="color" value={form.primaryColor} onChange={e => setForm({ ...form, primaryColor: e.target.value })} title="Primary color" />
        <input type="color" value={form.secondaryColor} onChange={e => setForm({ ...form, secondaryColor: e.target.value })} title="Secondary color" />
        <button type="submit" style={btnStyle}>Add Team</button>
      </form>

      <div style={{overflowX:"auto"}}><table style={tableStyle}>
        <thead>
          <tr>{['Name', 'Short', 'Type', 'Nation', 'Colors', ''].map(h => <th key={h} style={thStyle}>{h}</th>)}</tr>
        </thead>
        <tbody>
          {teams.map(t => (
            <tr key={t.id}>
              <td style={tdStyle}>{t.name}</td>
              <td style={tdStyle}>{t.shortName}</td>
              <td style={tdStyle}><span style={{ ...badgeStyle, background: t.type === 'CLUB' ? '#e0f0ff' : '#fff0e0' }}>{t.type}</span></td>
              <td style={tdStyle}>{t.nation?.name || '-'}</td>
              <td style={tdStyle}>
                <span style={{ display: 'inline-block', width: 20, height: 20, background: t.primaryColor, borderRadius: 4, marginRight: 4, border: '1px solid #ddd' }} />
                <span style={{ display: 'inline-block', width: 20, height: 20, background: t.secondaryColor, borderRadius: 4, border: '1px solid #ddd' }} />
              </td>
              <td style={tdStyle}><button onClick={() => remove(t.id)} style={deleteBtnStyle}>×</button></td>
            </tr>
          ))}
        </tbody>
      </table></div>
    </div>
  )
}

function NationsTab({ universeId, nations, reload }) {
  const [form, setForm] = useState({ name: '', description: '' })

  const create = async (e) => {
    e.preventDefault()
    await api.post(`/nations?universeId=${universeId}`, form)
    setForm({ name: '', description: '' })
    reload()
  }

  return (
    <div>
      <form onSubmit={create} style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
        <input placeholder="Nation name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} style={inputStyle} required />
        <input placeholder="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} style={{ ...inputStyle, flex: 2 }} />
        <button type="submit" style={btnStyle}>Add Nation</button>
      </form>
      <div style={{overflowX:"auto"}}><table style={tableStyle}>
        <thead><tr>{['Name', 'Description', ''].map(h => <th key={h} style={thStyle}>{h}</th>)}</tr></thead>
        <tbody>
          {nations.map(n => (
            <tr key={n.id}>
              <td style={tdStyle}>{n.name}</td>
              <td style={tdStyle}>{n.description || '-'}</td>
              <td style={tdStyle}><button onClick={async () => { await api.delete(`/nations/${n.id}`); reload() }} style={deleteBtnStyle}>×</button></td>
            </tr>
          ))}
        </tbody>
      </table></div>
    </div>
  )
}

function CompetitionsTab({ universeId, competitions, reload }) {
  const [form, setForm] = useState({ name: '', type: 'LEAGUE', teamLevel: 'CLUB', description: '' })

  const create = async (e) => {
    e.preventDefault()
    await api.post(`/competitions?universeId=${universeId}`, form)
    setForm({ ...form, name: '', description: '' })
    reload()
  }

  return (
    <div>
      <form onSubmit={create} style={{ display: 'flex', gap: 8, marginBottom: 20, flexWrap: 'wrap' }}>
        <input placeholder="Competition name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} style={inputStyle} required />
        <select value={form.type} onChange={e => setForm({ ...form, type: e.target.value })} style={inputStyle}>
          <option value="LEAGUE">League</option>
          <option value="KNOCKOUT">Knockout</option>
          <option value="GROUP_KNOCKOUT">Group + Knockout</option>
          <option value="SWISS">Swiss System</option>
        </select>
        <select value={form.teamLevel} onChange={e => setForm({ ...form, teamLevel: e.target.value })} style={inputStyle}>
          <option value="CLUB">Club level</option>
          <option value="NATIONAL">National level</option>
        </select>
        <button type="submit" style={btnStyle}>Add Competition</button>
      </form>

      <div style={{ display: 'grid', gap: 12 }}>
        {competitions.map(c => (
          <CompetitionCard key={c.id} competition={c} reload={reload} />
        ))}
      </div>
    </div>
  )
}

function CompetitionCard({ competition, reload }) {
  const [seasons, setSeasons] = useState([])
  const [expanded, setExpanded] = useState(false)
  const [seasonForm, setSeasonForm] = useState({ name: '', year: '' })

  const loadSeasons = async () => {
    const r = await api.get(`/seasons?competitionId=${competition.id}`)
    setSeasons(r.data)
  }

  useEffect(() => { if (expanded) loadSeasons() }, [expanded])

  const createSeason = async (e) => {
    e.preventDefault()
    await api.post(`/seasons?competitionId=${competition.id}`, { ...seasonForm, year: seasonForm.year ? parseInt(seasonForm.year) : null, status: 'PLANNED' })
    setSeasonForm({ name: '', year: '' })
    loadSeasons()
  }

  return (
    <div style={{ background: '#fff', padding: 16, borderRadius: 8, border: '1px solid #eee' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer' }} onClick={() => setExpanded(!expanded)}>
        <div>
          <strong>{competition.name}</strong>
          <span style={{ ...badgeStyle, marginLeft: 8 }}>{competition.type}</span>
          <span style={{ ...badgeStyle, background: '#f0f0f0', marginLeft: 4 }}>{competition.teamLevel}</span>
        </div>
        <span>{expanded ? '▾' : '▸'}</span>
      </div>

      {expanded && (
        <div style={{ marginTop: 16 }}>
          <form onSubmit={createSeason} style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
            <input placeholder="Season name" value={seasonForm.name} onChange={e => setSeasonForm({ ...seasonForm, name: e.target.value })} style={inputStyle} required />
            <input placeholder="Year" type="number" value={seasonForm.year} onChange={e => setSeasonForm({ ...seasonForm, year: e.target.value })} style={{ ...inputStyle, width: 80 }} />
            <button type="submit" style={{ ...btnStyle, fontSize: 13, padding: '6px 14px' }}>Add Season</button>
          </form>
          {seasons.map(s => (
            <Link key={s.id} to={`/seasons/${s.id}`} style={{ display: 'block', padding: '8px 12px', margin: '4px 0', background: '#f9f9f9', borderRadius: 6, textDecoration: 'none', color: '#333' }}>
              {s.name} {s.year && `(${s.year})`} — <span style={{ color: s.status === 'COMPLETED' ? '#2ecc71' : s.status === 'IN_PROGRESS' ? '#f39c12' : '#999' }}>{s.status}</span>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}

const inputStyle = { padding: '6px 10px', border: '1px solid #ddd', borderRadius: 6, fontSize: 13 }
const btnStyle = { padding: '6px 16px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 6, cursor: 'pointer', fontWeight: 600, fontSize: 13 }
const tableStyle = { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8, overflow: 'hidden' }
const thStyle = { textAlign: 'left', padding: '10px 12px', background: '#f8f8f8', fontSize: 12, color: '#666', fontWeight: 600 }
const tdStyle = { padding: '10px 12px', borderTop: '1px solid #f0f0f0', fontSize: 14 }
const badgeStyle = { display: 'inline-block', padding: '2px 8px', borderRadius: 4, fontSize: 11, fontWeight: 600, background: '#e0f0ff' }
const deleteBtnStyle = { background: 'none', border: 'none', color: '#ccc', cursor: 'pointer', fontSize: 18, fontWeight: 700 }
