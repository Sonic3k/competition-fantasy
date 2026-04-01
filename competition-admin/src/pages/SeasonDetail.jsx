import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/client'

export default function SeasonDetail() {
  const { id } = useParams()
  const [season, setSeason] = useState(null)
  const [rounds, setRounds] = useState([])
  const [allTeams, setAllTeams] = useState([])
  const [tab, setTab] = useState('matches')

  const load = async () => {
    const [s, r] = await Promise.all([
      api.get(`/seasons/${id}`),
      api.get(`/rounds?seasonId=${id}`),
    ])
    setSeason(s.data); setRounds(r.data)
    if (s.data.universeId) {
      const t = await api.get(`/teams?universeId=${s.data.universeId}`)
      setAllTeams(t.data)
    }
  }
  useEffect(() => { load() }, [id])

  const addTeamsToSeason = async (teamIds) => {
    await api.post(`/seasons/${id}/teams`, teamIds)
    load()
  }

  if (!season) return <p>Loading...</p>

  const compName = season.competition?.name || ''
  const univId = season.universeId

  return (
    <div>
      {univId && <Link to={`/universes/${univId}`} style={{ color: '#e94560', textDecoration: 'none', fontSize: 14 }}>&larr; Back to Universe</Link>}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1 style={{ marginBottom: 5 }}>{compName} — {season.name}</h1>
        <button onClick={async () => {
          if (!confirm('Delete this season and ALL its matches, standings? Cannot undo.')) return
          await api.delete(`/seasons/${id}`)
          window.location.href = `/universes/${univId}`
        }} style={{ background: '#e74c3c', color: '#fff', border: 'none', borderRadius: 6, padding: '6px 14px', cursor: 'pointer', fontSize: 12 }}>Delete Season</button>
      </div>
      <p style={{ color: '#666', marginTop: 0 }}>Status: <strong>{season.status}</strong> | Teams: {season.teams?.length || 0}</p>

      <div style={{ display: 'flex', gap: 0, marginBottom: 20, borderBottom: '2px solid #eee' }}>
        {['matches', 'standings', 'teams'].map(t => (
          <button key={t} onClick={() => setTab(t)}
            style={{ padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer', fontWeight: 600,
              borderBottom: tab === t ? '2px solid #e94560' : '2px solid transparent', color: tab === t ? '#e94560' : '#999' }}>
            {t.charAt(0).toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {tab === 'matches' && <MatchesTab seasonId={id} rounds={rounds} seasonTeams={season.teams || []} reload={load} />}
      {tab === 'standings' && <StandingsTab seasonId={id} rounds={rounds} />}
      {tab === 'teams' && <TeamsTab seasonTeams={season.teams || []} allTeams={allTeams} onAdd={addTeamsToSeason} />}
    </div>
  )
}

function MatchesTab({ seasonId, rounds, seasonTeams, reload }) {
  const [roundForm, setRoundForm] = useState({ roundNumber: '', name: '' })
  const [matchesByRound, setMatchesByRound] = useState({})
  const [expandedRound, setExpandedRound] = useState(null)

  const loadMatches = async (roundId) => {
    const r = await api.get(`/matches?roundId=${roundId}`)
    setMatchesByRound(prev => ({ ...prev, [roundId]: r.data }))
  }

  const createRound = async (e) => {
    e.preventDefault()
    await api.post(`/rounds?seasonId=${seasonId}`, { ...roundForm, roundNumber: parseInt(roundForm.roundNumber) })
    setRoundForm({ roundNumber: '', name: '' })
    reload()
  }

  const toggleRound = (roundId) => {
    if (expandedRound === roundId) { setExpandedRound(null); return }
    setExpandedRound(roundId)
    if (!matchesByRound[roundId]) loadMatches(roundId)
  }

  return (
    <div>
      <form onSubmit={createRound} style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
        <input placeholder="Round #" type="number" value={roundForm.roundNumber} onChange={e => setRoundForm({ ...roundForm, roundNumber: e.target.value })} style={{ ...inputStyle, width: 80 }} required />
        <input placeholder="Round name (opt)" value={roundForm.name} onChange={e => setRoundForm({ ...roundForm, name: e.target.value })} style={inputStyle} />
        <button type="submit" style={btnStyle}>Add Round</button>
      </form>

      {rounds.map(r => (
        <div key={r.id} style={{ marginBottom: 8 }}>
          <div onClick={() => toggleRound(r.id)} style={{ padding: '10px 14px', background: '#fff', borderRadius: 6, cursor: 'pointer', display: 'flex', justifyContent: 'space-between', border: '1px solid #eee' }}>
            <span><strong>Round {r.roundNumber}</strong> {r.name && `— ${r.name}`}</span>
            <span>{expandedRound === r.id ? '▾' : '▸'}</span>
          </div>
          {expandedRound === r.id && (
            <RoundMatches roundId={r.id} matches={matchesByRound[r.id] || []} seasonTeams={seasonTeams}
              reload={() => { loadMatches(r.id); reload() }} />
          )}
        </div>
      ))}
    </div>
  )
}

function RoundMatches({ roundId, matches, seasonTeams, reload }) {
  const [form, setForm] = useState({ homeTeamId: '', awayTeamId: '' })

  const createMatch = async (e) => {
    e.preventDefault()
    if (form.homeTeamId === form.awayTeamId) return alert('Home and away must be different')
    await api.post(`/matches?roundId=${roundId}`, {
      homeTeam: { id: form.homeTeamId }, awayTeam: { id: form.awayTeamId }, status: 'SCHEDULED'
    })
    setForm({ homeTeamId: '', awayTeamId: '' })
    reload()
  }

  const updateScore = async (matchId, homeScore, awayScore) => {
    await api.put(`/matches/${matchId}/score?homeScore=${homeScore}&awayScore=${awayScore}`)
    reload()
  }

  return (
    <div style={{ padding: '12px 14px', background: '#fafafa', borderRadius: '0 0 6px 6px' }}>
      {matches.map(m => (
        <MatchRow key={m.id} match={m} onUpdateScore={updateScore} />
      ))}

      <form onSubmit={createMatch} style={{ display: 'flex', gap: 8, marginTop: 10 }}>
        <select value={form.homeTeamId} onChange={e => setForm({ ...form, homeTeamId: e.target.value })} style={inputStyle} required>
          <option value="">Home team</option>
          {seasonTeams.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
        </select>
        <span style={{ alignSelf: 'center', fontWeight: 700 }}>vs</span>
        <select value={form.awayTeamId} onChange={e => setForm({ ...form, awayTeamId: e.target.value })} style={inputStyle} required>
          <option value="">Away team</option>
          {seasonTeams.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
        </select>
        <button type="submit" style={{ ...btnStyle, fontSize: 12 }}>Add Match</button>
      </form>
    </div>
  )
}

function MatchRow({ match, onUpdateScore }) {
  const [editing, setEditing] = useState(false)
  const [home, setHome] = useState(match.homeScore ?? '')
  const [away, setAway] = useState(match.awayScore ?? '')

  const save = () => {
    onUpdateScore(match.id, parseInt(home), parseInt(away))
    setEditing(false)
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '8px 0', borderBottom: '1px solid #eee' }}>
      <span style={{ flex: 1, textAlign: 'right', fontWeight: 500 }}>{match.homeTeam?.name || '?'}</span>
      {editing ? (
        <>
          <input type="number" value={home} onChange={e => setHome(e.target.value)} style={{ ...inputStyle, width: 40, textAlign: 'center' }} min="0" />
          <span>-</span>
          <input type="number" value={away} onChange={e => setAway(e.target.value)} style={{ ...inputStyle, width: 40, textAlign: 'center' }} min="0" />
          <button onClick={save} style={{ ...btnStyle, fontSize: 11, padding: '4px 10px' }}>Save</button>
        </>
      ) : (
        <>
          <span style={{ fontWeight: 700, minWidth: 50, textAlign: 'center', cursor: 'pointer', color: match.status === 'COMPLETED' ? '#333' : '#ccc' }}
            onClick={() => setEditing(true)}>
            {match.homeScore ?? '?'} - {match.awayScore ?? '?'}
          </span>
        </>
      )}
      <span style={{ flex: 1, fontWeight: 500 }}>{match.awayTeam?.name || '?'}</span>
    </div>
  )
}

function StandingsTab({ seasonId, rounds }) {
  const [recorded, setRecorded] = useState([])
  const [calculated, setCalculated] = useState([])
  const [afterRound, setAfterRound] = useState('')
  const [loading, setLoading] = useState(false)

  const loadRecorded = async () => {
    const params = afterRound ? `&type=RECORDED&afterRound=${afterRound}` : '&type=RECORDED'
    const r = await api.get(`/standings?seasonId=${seasonId}${params}`)
    setRecorded(r.data)
  }

  const loadCalculated = async () => {
    const params = afterRound ? `&type=CALCULATED&afterRound=${afterRound}` : '&type=CALCULATED'
    const r = await api.get(`/standings?seasonId=${seasonId}${params}`)
    setCalculated(r.data)
  }

  const calculate = async () => {
    setLoading(true)
    try {
      const params = afterRound ? `?seasonId=${seasonId}&afterRound=${afterRound}` : `?seasonId=${seasonId}`
      const r = await api.post(`/standings/calculate${params}`)
      setCalculated(r.data)
    } catch (err) { alert('Error: ' + err.message) }
    setLoading(false)
  }

  useEffect(() => { loadRecorded(); loadCalculated() }, [seasonId, afterRound])

  const calcMap = {}
  calculated.forEach(c => { calcMap[c.team?.id] = c })

  return (
    <div>
      <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 16 }}>
        <select value={afterRound} onChange={e => setAfterRound(e.target.value)} style={inputStyle}>
          <option value="">Final (all rounds)</option>
          {rounds.map(r => <option key={r.id} value={r.roundNumber}>After Round {r.roundNumber} {r.name ? `(${r.name})` : ''}</option>)}
        </select>
        <button onClick={calculate} disabled={loading} style={btnStyle}>
          {loading ? 'Calculating...' : 'Calculate from Matches'}
        </button>
      </div>

      <div style={{overflowX:"auto"}}><table style={tableStyle}>
        <thead>
          <tr>
            <th style={thStyle}>#</th><th style={thStyle}>Team</th>
            <th style={thStyle}>P</th><th style={thStyle}>W</th><th style={thStyle}>D</th><th style={thStyle}>L</th>
            <th style={thStyle}>GF</th><th style={thStyle}>GA</th><th style={thStyle}>GD</th><th style={thStyle}>Pts</th>
            {calculated.length > 0 && <><th style={{ ...thStyle, background: '#e8f4e8' }}>cP</th><th style={{ ...thStyle, background: '#e8f4e8' }}>cW</th><th style={{ ...thStyle, background: '#e8f4e8' }}>cD</th><th style={{ ...thStyle, background: '#e8f4e8' }}>cL</th><th style={{ ...thStyle, background: '#e8f4e8' }}>cGF</th><th style={{ ...thStyle, background: '#e8f4e8' }}>cGA</th><th style={{ ...thStyle, background: '#e8f4e8' }}>cPts</th><th style={{ ...thStyle, background: '#e8f4e8' }}>OK</th></>}
          </tr>
        </thead>
        <tbody>
          {recorded.map((s, i) => {
            const c = calcMap[s.team?.id]
            const match = c && c.points === s.points && c.goalsFor === s.goalsFor && c.goalsAgainst === s.goalsAgainst
            return (
              <tr key={s.id} style={!c ? {} : match ? {} : { background: '#fff3f3' }}>
                <td style={tdStyle}>{i + 1}</td>
                <td style={{ ...tdStyle, fontWeight: 600 }}>{s.team?.name}</td>
                <td style={tdStyle}>{s.played}</td><td style={tdStyle}>{s.won}</td><td style={tdStyle}>{s.drawn}</td><td style={tdStyle}>{s.lost}</td>
                <td style={tdStyle}>{s.goalsFor}</td><td style={tdStyle}>{s.goalsAgainst}</td><td style={tdStyle}>{s.goalDifference}</td>
                <td style={{ ...tdStyle, fontWeight: 700 }}>{s.points}</td>
                {c && <><td style={cTd}>{c.played}</td><td style={cTd}>{c.won}</td><td style={cTd}>{c.drawn}</td><td style={cTd}>{c.lost}</td><td style={cTd}>{c.goalsFor}</td><td style={cTd}>{c.goalsAgainst}</td><td style={{ ...cTd, fontWeight: 700 }}>{c.points}</td><td style={cTd}>{match ? '✓' : '✗'}</td></>}
              </tr>
            )
          })}
          {recorded.length === 0 && calculated.length > 0 && calculated.map((c, i) => (
            <tr key={c.id}>
              <td style={tdStyle}>{i + 1}</td>
              <td style={{ ...tdStyle, fontWeight: 600 }}>{c.team?.name}</td>
              <td style={tdStyle}>{c.played}</td><td style={tdStyle}>{c.won}</td><td style={tdStyle}>{c.drawn}</td><td style={tdStyle}>{c.lost}</td>
              <td style={tdStyle}>{c.goalsFor}</td><td style={tdStyle}>{c.goalsAgainst}</td><td style={tdStyle}>{c.goalDifference}</td>
              <td style={{ ...tdStyle, fontWeight: 700 }}>{c.points}</td>
            </tr>
          ))}
          {recorded.length === 0 && calculated.length === 0 && <tr><td colSpan={10} style={{ ...tdStyle, color: '#999', textAlign: 'center' }}>No standings yet.</td></tr>}
        </tbody>
      </table></div>
      {calculated.length > 0 && <p style={{ fontSize: 12, color: '#999', marginTop: 8 }}>Green columns (c*) = calculated from match data. Red rows = mismatch.</p>}
    </div>
  )
}

function TeamsTab({ seasonTeams, allTeams, onAdd }) {
  const available = allTeams.filter(t => !seasonTeams.find(st => st.id === t.id))
  const [selected, setSelected] = useState([])

  const toggle = (id) => setSelected(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id])

  return (
    <div>
      <h3>Teams in this season ({seasonTeams.length})</h3>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 20 }}>
        {seasonTeams.map(t => (
          <span key={t.id} style={{ padding: '6px 12px', background: '#fff', borderRadius: 6, border: '1px solid #eee', fontSize: 13 }}>
            <span style={{ display: 'inline-block', width: 10, height: 10, borderRadius: '50%', background: t.primaryColor, marginRight: 6 }} />
            {t.name}
          </span>
        ))}
      </div>

      {available.length > 0 && (
        <>
          <h3>Add teams</h3>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 12 }}>
            {available.map(t => (
              <label key={t.id} style={{ padding: '6px 12px', background: selected.includes(t.id) ? '#e94560' : '#fff', color: selected.includes(t.id) ? '#fff' : '#333',
                borderRadius: 6, border: '1px solid #eee', cursor: 'pointer', fontSize: 13 }}>
                <input type="checkbox" checked={selected.includes(t.id)} onChange={() => toggle(t.id)} style={{ display: 'none' }} />
                {t.name}
              </label>
            ))}
          </div>
          <button onClick={() => { onAdd(selected); setSelected([]) }} disabled={!selected.length} style={btnStyle}>
            Add {selected.length} team(s)
          </button>
        </>
      )}
    </div>
  )
}

const inputStyle = { padding: '6px 10px', border: '1px solid #ddd', borderRadius: 6, fontSize: 13 }
const btnStyle = { padding: '6px 16px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 6, cursor: 'pointer', fontWeight: 600, fontSize: 13 }
const tableStyle = { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8, overflow: 'hidden' }
const thStyle = { textAlign: 'left', padding: '10px 12px', background: '#f8f8f8', fontSize: 12, color: '#666', fontWeight: 600 }
const tdStyle = { padding: '8px 12px', borderTop: '1px solid #f0f0f0', fontSize: 14 }
const cTd = { padding: '8px 12px', borderTop: '1px solid #f0f0f0', fontSize: 14, background: '#f0f8f0' }
