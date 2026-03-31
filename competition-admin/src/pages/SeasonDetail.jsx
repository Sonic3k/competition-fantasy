import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/client'

export default function SeasonDetail() {
  const { id } = useParams()
  const [season, setSeason] = useState(null)
  const [rounds, setRounds] = useState([])
  const [standings, setStandings] = useState([])
  const [allTeams, setAllTeams] = useState([])
  const [tab, setTab] = useState('matches')

  const load = async () => {
    const [s, r, st] = await Promise.all([
      api.get(`/seasons/${id}`),
      api.get(`/rounds?seasonId=${id}`),
      api.get(`/standings?seasonId=${id}`),
    ])
    setSeason(s.data); setRounds(r.data); setStandings(st.data)
    if (s.data.competition?.universe?.id) {
      const t = await api.get(`/teams?universeId=${s.data.competition.universe.id}`)
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
  const univId = season.competition?.universe?.id

  return (
    <div>
      {univId && <Link to={`/universes/${univId}`} style={{ color: '#e94560', textDecoration: 'none', fontSize: 14 }}>&larr; Back to Universe</Link>}
      <h1 style={{ marginBottom: 5 }}>{compName} — {season.name}</h1>
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
      {tab === 'standings' && <StandingsTab standings={standings} />}
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

function StandingsTab({ standings }) {
  return (
    <table style={tableStyle}>
      <thead>
        <tr>{['#', 'Team', 'P', 'W', 'D', 'L', 'GF', 'GA', 'GD', 'Pts'].map(h => <th key={h} style={thStyle}>{h}</th>)}</tr>
      </thead>
      <tbody>
        {standings.map((s, i) => (
          <tr key={s.id}>
            <td style={tdStyle}>{i + 1}</td>
            <td style={{ ...tdStyle, fontWeight: 600 }}>{s.team?.name}</td>
            <td style={tdStyle}>{s.played}</td>
            <td style={tdStyle}>{s.won}</td>
            <td style={tdStyle}>{s.drawn}</td>
            <td style={tdStyle}>{s.lost}</td>
            <td style={tdStyle}>{s.goalsFor}</td>
            <td style={tdStyle}>{s.goalsAgainst}</td>
            <td style={tdStyle}>{s.goalsFor - s.goalsAgainst}</td>
            <td style={{ ...tdStyle, fontWeight: 700 }}>{s.points}</td>
          </tr>
        ))}
        {standings.length === 0 && <tr><td colSpan={10} style={{ ...tdStyle, color: '#999', textAlign: 'center' }}>No standings yet. Add teams and play matches!</td></tr>}
      </tbody>
    </table>
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
