import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/client'
import ImageUpload from '../components/ImageUpload'

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
  const [form, setForm] = useState({ name: '', shortName: '', type: 'CLUB', homeBg: '#003366', homeText: '#ffffff', awayBg: '#ffffff', awayText: '#003366', nationId: '' })

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
        <input placeholder="ABC" value={form.shortName} onChange={e => setForm({ ...form, shortName: e.target.value.toUpperCase().slice(0, 3) })} style={{ ...inputStyle, width: 50, textAlign: 'center' }} maxLength={3} />
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
        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
          <span style={{ fontSize: 11, color: '#999' }}>H</span>
          <input type="color" value={form.homeBg} onChange={e => setForm({ ...form, homeBg: e.target.value })} title="Home BG" />
          <input type="color" value={form.homeText} onChange={e => setForm({ ...form, homeText: e.target.value })} title="Home Text" />
          <span style={{ fontSize: 11, color: '#999' }}>A</span>
          <input type="color" value={form.awayBg} onChange={e => setForm({ ...form, awayBg: e.target.value })} title="Away BG" />
          <input type="color" value={form.awayText} onChange={e => setForm({ ...form, awayText: e.target.value })} title="Away Text" />
        </div>
        <button type="submit" style={btnStyle}>Add Team</button>
      </form>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
        {teams.map(t => (
          <div key={t.id} style={{ display: 'flex', alignItems: 'center', gap: 10, background: '#fff', padding: '10px 14px', borderRadius: 8, border: '1px solid #eee' }}>
            <ImageUpload endpoint={`/upload/team/${t.id}`} currentUrl={t.logoUrl} onUploaded={reload} size={36} shape="square" />
            <TeamBadge team={t} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontWeight: 600, fontSize: 14 }}><Link to={`/teams/${t.id}`} style={{ color: '#1a1a2e', textDecoration: 'none' }}>{t.name}</Link></div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 2 }}>
                {t.nation ? (
                  <span style={{
                    display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                    padding: '1px 6px', borderRadius: 3, fontSize: 9, fontWeight: 700,
                    background: t.nation.primaryColor || '#666', color: t.nation.textColor || '#fff',
                    letterSpacing: 0.3,
                  }}>{t.nation.code || t.nation.name}</span>
                ) : (
                  <span style={{ fontSize: 11, color: '#bbb' }}>{t.type}</span>
                )}
              </div>
            </div>
            <TeamBadge team={t} away />
            <button onClick={() => remove(t.id)} style={{ background: 'none', border: 'none', color: '#ddd', cursor: 'pointer', fontSize: 16 }}>×</button>
          </div>
        ))}
      </div>
    </div>
  )
}

function NationsTab({ universeId, nations, reload }) {
  const [form, setForm] = useState({ name: '', code: '', description: '', primaryColor: '#003399', textColor: '#ffffff', awayColor: '#ffffff', awayTextColor: '#003399' })

  const create = async (e) => {
    e.preventDefault()
    await api.post(`/nations?universeId=${universeId}`, form)
    setForm({ name: '', code: '', description: '', primaryColor: '#003399', textColor: '#ffffff', awayColor: '#ffffff', awayTextColor: '#003399' })
    reload()
  }

  return (
    <div>
      <form onSubmit={create} style={{ display: 'flex', gap: 8, marginBottom: 20, flexWrap: 'wrap' }}>
        <input placeholder="Nation name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} style={inputStyle} required />
        <input placeholder="ABC" value={form.code} onChange={e => setForm({ ...form, code: e.target.value.toUpperCase().slice(0, 3) })} style={{ ...inputStyle, width: 50, textAlign: 'center' }} maxLength={3} />
        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
          <span style={{ fontSize: 11, color: '#999' }}>H</span>
          <input type="color" value={form.primaryColor} onChange={e => setForm({ ...form, primaryColor: e.target.value })} title="Home BG" />
          <input type="color" value={form.textColor} onChange={e => setForm({ ...form, textColor: e.target.value })} title="Home Text" />
          <span style={{ fontSize: 11, color: '#999' }}>A</span>
          <input type="color" value={form.awayColor} onChange={e => setForm({ ...form, awayColor: e.target.value })} title="Away BG" />
          <input type="color" value={form.awayTextColor} onChange={e => setForm({ ...form, awayTextColor: e.target.value })} title="Away Text" />
        </div>
        <button type="submit" style={btnStyle}>Add Nation</button>
      </form>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
        {nations.map(n => (
          <div key={n.id} style={{ display: 'flex', alignItems: 'center', gap: 12, background: '#fff', padding: '10px 14px', borderRadius: 8, border: '1px solid #eee' }}>
            <ImageUpload endpoint={`/upload/nation/${n.id}`} currentUrl={n.flagUrl} onUploaded={reload} size={36} shape="square" />
            <span style={{
              display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
              width: 42, height: 28, borderRadius: 4, fontSize: 11, fontWeight: 800, letterSpacing: 0.5,
              background: n.primaryColor || '#666', color: n.textColor || '#fff',
              border: '1px solid rgba(0,0,0,0.08)', flexShrink: 0,
            }}>{n.code || '???'}</span>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontWeight: 600, fontSize: 14 }}><Link to={`/nations/${n.id}`} style={{ color: '#1a1a2e', textDecoration: 'none' }}>{n.name}</Link></div>
              {n.description && <div style={{ fontSize: 11, color: '#999' }}>{n.description}</div>}
            </div>
            <span style={{
              display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
              width: 36, height: 22, borderRadius: 4, fontSize: 9, fontWeight: 800, letterSpacing: 0.3,
              background: n.awayColor || '#f0f0f0', color: n.awayTextColor || '#333',
              border: '1px solid rgba(0,0,0,0.08)', flexShrink: 0,
            }}>{n.code || '???'}</span>
            <button onClick={async () => { await api.delete(`/nations/${n.id}`); reload() }}
              style={{ background: 'none', border: 'none', color: '#ddd', cursor: 'pointer', fontSize: 16 }}>×</button>
          </div>
        ))}
      </div>
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

function TeamBadge({ team, away }) {
  const bg = away ? (team.awayBg || '#f0f0f0') : (team.homeBg || '#333')
  const text = away ? (team.awayText || '#333') : (team.homeText || '#fff')
  const size = away ? { width: 32, height: 22, fontSize: 9 } : { width: 38, height: 26, fontSize: 10 }
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
      ...size, borderRadius: 4, fontWeight: 800, letterSpacing: 0.3,
      background: bg, color: text, border: '1px solid rgba(0,0,0,0.08)', flexShrink: 0,
      fontFamily: 'system-ui, sans-serif', textTransform: 'uppercase',
    }}>
      {team.shortName || '???'}
    </span>
  )
}

const inputStyle = { padding: '6px 10px', border: '1px solid #ddd', borderRadius: 6, fontSize: 13 }
const btnStyle = { padding: '6px 16px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 6, cursor: 'pointer', fontWeight: 600, fontSize: 13 }
const tableStyle = { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8, overflow: 'hidden' }
const thStyle = { textAlign: 'left', padding: '10px 12px', background: '#f8f8f8', fontSize: 12, color: '#666', fontWeight: 600 }
const tdStyle = { padding: '10px 12px', borderTop: '1px solid #f0f0f0', fontSize: 14 }
const badgeStyle = { display: 'inline-block', padding: '2px 8px', borderRadius: 4, fontSize: 11, fontWeight: 600, background: '#e0f0ff' }
const deleteBtnStyle = { background: 'none', border: 'none', color: '#ccc', cursor: 'pointer', fontSize: 18, fontWeight: 700 }
