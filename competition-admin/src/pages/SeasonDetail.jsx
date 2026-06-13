import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/client'

export default function SeasonDetail() {
  const { id } = useParams()
  const [season, setSeason] = useState(null)
  const [stages, setStages] = useState([])
  const [rounds, setRounds] = useState([])
  const [allTeams, setAllTeams] = useState([])
  const [tab, setTab] = useState('matches')

  const load = async () => {
    const [s, st, r] = await Promise.all([
      api.get(`/seasons/${id}`),
      api.get(`/stages?seasonId=${id}`),
      api.get(`/rounds?seasonId=${id}`),
    ])
    setSeason(s.data); setStages(st.data); setRounds(r.data)
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
  const hasKnockout = stages.some(s => s.type === 'KNOCKOUT')

  return (
    <div>
      {univId && <Link to={`/universes/${univId}`} style={{ color: '#e94560', textDecoration: 'none', fontSize: 14 }}>&larr; Back to Universe</Link>}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1 style={{ marginBottom: 5 }}>{compName} — {season.name}</h1>
        <button onClick={async () => {
          if (!confirm('Delete this season and ALL its matches, standings? Cannot undo.')) return
          await api.delete(`/seasons/${id}`)
          window.location.href = `/universes/${univId}`
        }} style={dangerBtn}>Delete Season</button>
      </div>
      <p style={{ color: '#666', marginTop: 0 }}>
        Status: <strong>{season.status}</strong> | Teams: {season.teams?.length || 0} | Stages: {stages.length}
        {season.formatPresetKey && <> | Format: <strong>{season.formatPresetKey}</strong></>}
      </p>

      {stages.length === 0 && <GeneratePanel seasonId={id} seasonTeams={season.teams || []} onDone={load} />}
      {hasKnockout && (
        <div style={{ marginBottom: 16 }}>
          <AdvanceButton seasonId={id} status={season.status} onDone={load} />
        </div>
      )}

      <div style={{ display: 'flex', gap: 0, marginBottom: 20, borderBottom: '2px solid #eee' }}>
        {['matches', 'standings', 'teams'].map(t => (
          <button key={t} onClick={() => setTab(t)}
            style={{ padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer', fontWeight: 600,
              borderBottom: tab === t ? '2px solid #e94560' : '2px solid transparent', color: tab === t ? '#e94560' : '#999' }}>
            {t.charAt(0).toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {tab === 'matches' && <MatchesTab seasonId={id} stages={stages} rounds={rounds} seasonTeams={season.teams || []} reload={load} />}
      {tab === 'standings' && <StandingsTab seasonId={id} stages={stages} rounds={rounds} />}
      {tab === 'teams' && <TeamsTab seasonTeams={season.teams || []} allTeams={allTeams} onAdd={addTeamsToSeason} />}
    </div>
  )
}

function MatchesTab({ seasonId, stages, rounds, seasonTeams, reload }) {
  const [selectedStage, setSelectedStage] = useState(null)
  const [viewMode, setViewMode] = useState('round')
  const [matchesByKey, setMatchesByKey] = useState({})
  const [expandedKey, setExpandedKey] = useState(null)

  const activeStage = stages.find(s => s.id === selectedStage) || stages[0]
  const hasGroups = activeStage?.groups?.length > 1
  const isKnockout = activeStage?.type === 'KNOCKOUT'
  const stageRounds = rounds.filter(r => r.stageId === activeStage?.id)
  const groups = activeStage?.groups || []

  // Group rounds by roundNumber for "By Round" view
  const matchdays = []
  const seen = new Set()
  for (const r of stageRounds) {
    if (!seen.has(r.roundNumber)) {
      seen.add(r.roundNumber)
      matchdays.push({
        roundNumber: r.roundNumber,
        name: r.name,
        roundIds: stageRounds.filter(x => x.roundNumber === r.roundNumber).map(x => x.id)
      })
    }
  }

  const loadMatches = async (key, params) => {
    const r = await api.get(`/matches?${params}`)
    setMatchesByKey(prev => ({ ...prev, [key]: r.data }))
  }

  const loadMatchday = async (key, roundIds) => {
    const all = await Promise.all(roundIds.map(rid => api.get(`/matches?roundId=${rid}`)))
    const matches = all.flatMap(r => r.data)
    setMatchesByKey(prev => ({ ...prev, [key]: matches }))
  }

  const toggle = (key, loader) => {
    if (expandedKey === key) { setExpandedKey(null); return }
    setExpandedKey(key)
    if (!matchesByKey[key]) loader()
  }

  // Group matches by stageGroupName for display
  const groupedMatches = (matches) => {
    const map = {}
    for (const m of matches) {
      const g = m.stageGroupName || 'Matches'
      if (!map[g]) map[g] = []
      map[g].push(m)
    }
    return Object.entries(map)
  }

  return (
    <div>
      {stages.length > 1 && (
        <div style={{ display: 'flex', gap: 8, marginBottom: 16, flexWrap: 'wrap' }}>
          {stages.map(s => (
            <button key={s.id} onClick={() => { setSelectedStage(s.id); setExpandedKey(null); setMatchesByKey({}); setViewMode('round') }}
              style={{ ...chipBtn, background: (activeStage?.id === s.id) ? '#1a1a2e' : '#fff', color: (activeStage?.id === s.id) ? '#fff' : '#333' }}>
              {s.name} {s.type === 'KNOCKOUT' && s.legs > 1 ? `(${s.legs} legs)` : ''}
            </button>
          ))}
        </div>
      )}

      {hasGroups && (
        <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
          <button onClick={() => { setViewMode('round'); setExpandedKey(null) }} style={{ ...chipBtn, background: viewMode === 'round' ? '#e94560' : '#fff', color: viewMode === 'round' ? '#fff' : '#333' }}>By Round</button>
          <button onClick={() => { setViewMode('group'); setExpandedKey(null) }} style={{ ...chipBtn, background: viewMode === 'group' ? '#e94560' : '#fff', color: viewMode === 'group' ? '#fff' : '#333' }}>By Group</button>
        </div>
      )}

      {(viewMode === 'round' || !hasGroups) ? (
        matchdays.map(md => (
          <div key={md.roundNumber} style={{ marginBottom: 8 }}>
            <div onClick={() => toggle(`md-${md.roundNumber}`, () => loadMatchday(`md-${md.roundNumber}`, md.roundIds))}
              style={{ ...expandHeader, background: expandedKey === `md-${md.roundNumber}` ? '#f0f0f0' : '#fff' }}>
              <span><strong>{md.name || `Round ${md.roundNumber}`}</strong> {hasGroups && <span style={{ color: '#999', fontSize: 12 }}>({md.roundIds.length} groups)</span>}</span>
              <span>{expandedKey === `md-${md.roundNumber}` ? '▾' : '▸'}</span>
            </div>
            {expandedKey === `md-${md.roundNumber}` && (
              <div style={{ background: '#fafafa', borderRadius: '0 0 6px 6px', padding: '4px 0' }}>
                {groupedMatches(matchesByKey[`md-${md.roundNumber}`] || []).map(([gName, matches]) => (
                  <div key={gName}>
                    {hasGroups && <div style={{ padding: '8px 14px 4px', fontSize: 12, fontWeight: 700, color: '#666', borderTop: '1px solid #eee' }}>{gName}</div>}
                    <MatchList matches={matches} isKnockout={isKnockout} onSaved={() => loadMatchday(`md-${md.roundNumber}`, md.roundIds)} />
                  </div>
                ))}
              </div>
            )}
          </div>
        ))
      ) : (
        groups.map(g => (
          <div key={g.id} style={{ marginBottom: 8 }}>
            <div onClick={() => toggle(`g-${g.id}`, () => loadMatches(`g-${g.id}`, `stageGroupId=${g.id}`))}
              style={{ ...expandHeader, background: expandedKey === `g-${g.id}` ? '#f0f0f0' : '#fff' }}>
              <span><strong>{g.name}</strong> <span style={{ color: '#999', fontSize: 12 }}>({g.teams?.length} teams)</span></span>
              <span>{expandedKey === `g-${g.id}` ? '▾' : '▸'}</span>
            </div>
            {expandedKey === `g-${g.id}` && <MatchList matches={matchesByKey[`g-${g.id}`] || []} showRound isKnockout={isKnockout} onSaved={() => loadMatches(`g-${g.id}`, `stageGroupId=${g.id}`)} />}
          </div>
        ))
      )}

      {matchdays.length === 0 && stageRounds.length === 0 && <p style={{ color: '#999' }}>No rounds in this stage.</p>}
    </div>
  )
}

function MatchList({ matches, showGroup, showRound, isKnockout, onSaved }) {
  if (!matches.length) return <p style={{ padding: '12px 14px', color: '#999', fontSize: 13 }}>No matches</p>
  return (
    <div style={{ padding: '8px 14px', background: '#fafafa', borderRadius: '0 0 6px 6px' }}>
      {matches.map(m => (
        <MatchRow key={m.id} m={m} showGroup={showGroup} showRound={showRound} isKnockout={isKnockout} onSaved={onSaved} />
      ))}
    </div>
  )
}

function MatchRow({ m, showGroup, showRound, isKnockout, onSaved }) {
  const [edit, setEdit] = useState(false)
  const [hs, setHs] = useState(m.homeScore ?? '')
  const [as, setAs] = useState(m.awayScore ?? '')
  const [hp, setHp] = useState(m.homePenalties ?? '')
  const [ap, setAp] = useState(m.awayPenalties ?? '')
  const [busy, setBusy] = useState(false)

  const canEdit = m.homeTeam && m.awayTeam // teams must be known to enter a score
  const homeName = m.homeTeam?.name || 'TBD'
  const awayName = m.awayTeam?.name || 'TBD'

  const save = async () => {
    if (hs === '' || as === '') { alert('Enter both scores'); return }
    setBusy(true)
    try {
      let url = `/matches/${m.id}/score?homeScore=${hs}&awayScore=${as}`
      if (isKnockout && hp !== '' && ap !== '') url += `&homePenalties=${hp}&awayPenalties=${ap}`
      await api.put(url)
      setEdit(false)
      onSaved && onSaved()
    } catch (e) { alert('Error: ' + (e.response?.data?.message || e.message)) }
    setBusy(false)
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '6px 0', borderBottom: '1px solid #eee', fontSize: 14 }}>
      {showRound && <span style={{ color: '#999', fontSize: 11, minWidth: 30 }}>R{m.roundNumber}</span>}
      <span style={{ flex: 1, textAlign: 'right', fontWeight: 500, color: m.homeTeam ? '#333' : '#bbb' }}>
        {homeName}
        {m.homeTeam?.nation && <span style={{ color: '#999', fontSize: 11 }}> ({m.homeTeam.nation.name})</span>}
      </span>

      {edit ? (
        <span style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
          <input value={hs} onChange={e => setHs(e.target.value)} style={scoreInput} inputMode="numeric" />
          <span>-</span>
          <input value={as} onChange={e => setAs(e.target.value)} style={scoreInput} inputMode="numeric" />
          {isKnockout && (
            <>
              <span style={{ fontSize: 10, color: '#999' }}>pen</span>
              <input value={hp} onChange={e => setHp(e.target.value)} style={penInput} placeholder="-" inputMode="numeric" />
              <input value={ap} onChange={e => setAp(e.target.value)} style={penInput} placeholder="-" inputMode="numeric" />
            </>
          )}
          <button onClick={save} disabled={busy} style={miniBtn}>✓</button>
          <button onClick={() => setEdit(false)} style={miniBtnGhost}>✕</button>
        </span>
      ) : (
        <span onClick={() => canEdit && setEdit(true)}
          title={canEdit ? 'Click to edit score' : 'Teams not decided yet'}
          style={{ fontWeight: 700, minWidth: 54, textAlign: 'center', cursor: canEdit ? 'pointer' : 'default', color: m.status === 'COMPLETED' ? '#333' : '#ccc' }}>
          {m.homeScore ?? '?'} - {m.awayScore ?? '?'}
          {isKnockout && m.homePenalties != null && m.awayPenalties != null &&
            <span style={{ fontSize: 10, color: '#e94560' }}> ({m.homePenalties}-{m.awayPenalties}p)</span>}
        </span>
      )}

      <span style={{ flex: 1, fontWeight: 500, color: m.awayTeam ? '#333' : '#bbb' }}>
        {awayName}
        {m.awayTeam?.nation && <span style={{ color: '#999', fontSize: 11 }}> ({m.awayTeam.nation.name})</span>}
      </span>
      {m.leg && <span style={{ fontSize: 11, color: '#999' }}>Leg {m.leg}</span>}
      {m.winnerTeamId && <span style={{ fontSize: 10, color: '#27ae60' }} title="Winner resolved">✓{m.decidedBy && m.decidedBy !== 'REGULAR' ? ' ' + m.decidedBy.toLowerCase().replace('_', ' ') : ''}</span>}
      {showGroup && m.stageGroupName && <span style={groupBadge}>{m.stageGroupName}</span>}
    </div>
  )
}

function GeneratePanel({ seasonId, seasonTeams, onDone }) {
  const [formats, setFormats] = useState([])
  const [preset, setPreset] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => { api.get('/formats').then(r => setFormats(r.data)).catch(() => {}) }, [])

  const sel = formats.find(f => f.presetKey === preset)
  const generate = async () => {
    if (!preset) return
    setBusy(true)
    try {
      const teamIds = (seasonTeams || []).map(t => t.id)
      await api.post(`/seasons/${seasonId}/generate?presetKey=${preset}`, teamIds)
      onDone()
    } catch (e) { alert('Error: ' + (e.response?.data?.error || e.message)) }
    setBusy(false)
  }

  return (
    <div style={{ background: '#fff8f8', border: '1px solid #f0d0d0', borderRadius: 8, padding: 14, marginBottom: 16 }}>
      <strong>Set up tournament structure</strong>
      <p style={{ color: '#888', fontSize: 13, margin: '4px 0 10px' }}>
        No stages yet. Pick a format to generate groups, fixtures and the knockout bracket.
        {' '}{seasonTeams?.length || 0} team(s) will be drawn into the groups (in order).
      </p>
      <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
        <select value={preset} onChange={e => setPreset(e.target.value)} style={inputStyle}>
          <option value="">Choose format…</option>
          {formats.map(f => <option key={f.presetKey} value={f.presetKey}>{f.displayName || f.presetKey}</option>)}
        </select>
        <button onClick={generate} disabled={!preset || busy} style={btnStyle}>{busy ? 'Generating…' : 'Generate'}</button>
      </div>
      {sel && (
        <p style={{ color: '#999', fontSize: 12, marginBottom: 0, marginTop: 8 }}>
          {sel.phases?.map(p => p.type === 'GROUP'
            ? `${p.numGroups}×${p.teamsPerGroup || '?'} groups`
            : p.name).join(' → ')}
          {sel.thirdPlacePlayoff ? ' + 3rd place' : ''}
        </p>
      )}
    </div>
  )
}

function AdvanceButton({ seasonId, status, onDone }) {
  const [busy, setBusy] = useState(false)
  const disabled = status === 'COMPLETED' || busy

  const advance = async () => {
    setBusy(true)
    try {
      const r = await api.post(`/seasons/${seasonId}/advance`)
      alert(r.data?.message || 'Advanced.')
      onDone()
    } catch (e) { alert('Error: ' + (e.response?.data?.message || e.message)) }
    setBusy(false)
  }

  return (
    <button onClick={advance} disabled={disabled}
      title={status === 'COMPLETED' ? 'Season is completed — advance disabled' : 'Recompute standings, fill knockout slots, propagate winners'}
      style={{ ...btnStyle, background: disabled ? '#bbb' : '#1a1a2e' }}>
      {busy ? 'Advancing…' : '⟳ Advance knockout'}
    </button>
  )
}

function StandingsTab({ seasonId, stages, rounds }) {
  const [recorded, setRecorded] = useState([])
  const [calculated, setCalculated] = useState([])
  const [afterRound, setAfterRound] = useState('')
  const [selectedStageGroup, setSelectedStageGroup] = useState('')
  const [loading, setLoading] = useState(false)

  const allGroups = stages.flatMap(s => (s.groups || []).map(g => ({ ...g, stageName: s.name })))

  const loadStandings = async () => {
    let params = `seasonId=${seasonId}`
    if (selectedStageGroup) params += `&stageGroupId=${selectedStageGroup}`
    if (afterRound) params += `&afterRound=${afterRound}`

    const [rec, calc] = await Promise.all([
      api.get(`/standings?${params}&type=RECORDED`).catch(() => ({ data: [] })),
      api.get(`/standings?${params}&type=CALCULATED`).catch(() => ({ data: [] })),
    ])
    setRecorded(rec.data); setCalculated(calc.data)
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

  useEffect(() => { loadStandings() }, [seasonId, afterRound, selectedStageGroup])

  const calcMap = {}
  calculated.forEach(c => { calcMap[c.team?.id] = c })

  return (
    <div>
      <div style={{ display: 'flex', gap: 10, alignItems: 'center', marginBottom: 16, flexWrap: 'wrap' }}>
        {allGroups.length > 1 && (
          <select value={selectedStageGroup} onChange={e => setSelectedStageGroup(e.target.value)} style={inputStyle}>
            <option value="">All groups</option>
            {allGroups.map(g => <option key={g.id} value={g.id}>{g.stageName} — {g.name}</option>)}
          </select>
        )}
        <select value={afterRound} onChange={e => setAfterRound(e.target.value)} style={inputStyle}>
          <option value="">Final</option>
          {rounds.map(r => <option key={r.id} value={r.roundNumber}>After R{r.roundNumber} {r.name || ''}</option>)}
        </select>
        <button onClick={calculate} disabled={loading} style={btnStyle}>
          {loading ? '...' : 'Calculate'}
        </button>
      </div>

      <div style={{ overflowX: 'auto' }}>
        <table style={tableStyle}>
          <thead>
            <tr>
              <th style={thStyle}>#</th><th style={thStyle}>Team</th>
              <th style={thStyle}>P</th><th style={thStyle}>W</th><th style={thStyle}>D</th><th style={thStyle}>L</th>
              <th style={thStyle}>GF</th><th style={thStyle}>GA</th><th style={thStyle}>GD</th><th style={thStyle}>Pts</th>
              {calculated.length > 0 && <><th style={cTh}>cPts</th><th style={cTh}>OK</th></>}
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
                  {c && <><td style={cTd}>{c.points}</td><td style={cTd}>{match ? '✓' : '✗'}</td></>}
                </tr>
              )
            })}
            {recorded.length === 0 && calculated.length > 0 && calculated.map((c, i) => (
              <tr key={c.id}>
                <td style={tdStyle}>{c.rank ?? i + 1}</td>
                <td style={{ ...tdStyle, fontWeight: 600 }}>{c.team?.name}</td>
                <td style={tdStyle}>{c.played}</td><td style={tdStyle}>{c.won}</td><td style={tdStyle}>{c.drawn}</td><td style={tdStyle}>{c.lost}</td>
                <td style={tdStyle}>{c.goalsFor}</td><td style={tdStyle}>{c.goalsAgainst}</td><td style={tdStyle}>{c.goalDifference}</td>
                <td style={{ ...tdStyle, fontWeight: 700 }}>{c.points}</td>
              </tr>
            ))}
            {recorded.length === 0 && calculated.length === 0 && <tr><td colSpan={12} style={{ ...tdStyle, color: '#999', textAlign: 'center' }}>No standings</td></tr>}
          </tbody>
        </table>
      </div>
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
            <span style={{ display: 'inline-block', width: 10, height: 10, borderRadius: '50%', background: t.primaryColor || '#ccc', marginRight: 6 }} />
            {t.name}
            {t.nation && <span style={{ color: '#999', fontSize: 11 }}> ({t.nation.name})</span>}
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
const dangerBtn = { background: '#e74c3c', color: '#fff', border: 'none', borderRadius: 6, padding: '6px 14px', cursor: 'pointer', fontSize: 12 }
const chipBtn = { padding: '8px 14px', border: '1px solid #eee', borderRadius: 20, cursor: 'pointer', fontSize: 13, fontWeight: 500 }
const expandHeader = { padding: '10px 14px', borderRadius: 6, cursor: 'pointer', display: 'flex', justifyContent: 'space-between', border: '1px solid #eee', marginBottom: 2 }
const groupBadge = { display: 'inline-block', padding: '2px 8px', borderRadius: 4, fontSize: 10, fontWeight: 600, background: '#e8e8e8', color: '#666', marginLeft: 6 }
const scoreInput = { width: 34, padding: '3px 4px', border: '1px solid #ddd', borderRadius: 4, fontSize: 13, textAlign: 'center' }
const penInput = { width: 26, padding: '3px 2px', border: '1px solid #f0c0c0', borderRadius: 4, fontSize: 11, textAlign: 'center' }
const miniBtn = { padding: '2px 8px', background: '#27ae60', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer', fontSize: 12 }
const miniBtnGhost = { padding: '2px 8px', background: '#eee', color: '#666', border: 'none', borderRadius: 4, cursor: 'pointer', fontSize: 12 }
const tableStyle = { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8 }
const thStyle = { textAlign: 'left', padding: '8px 10px', background: '#f8f8f8', fontSize: 12, color: '#666', fontWeight: 600 }
const cTh = { ...thStyle, background: '#e8f4e8' }
const tdStyle = { padding: '8px 10px', borderTop: '1px solid #f0f0f0', fontSize: 13 }
const cTd = { ...tdStyle, background: '#f0f8f0' }
