import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/client'
import ImageUpload from '../components/ImageUpload'

export default function TeamDetail() {
  const { id } = useParams()
  const [team, setTeam] = useState(null)
  const [matches, setMatches] = useState([])

  const load = async () => {
    const [t, m] = await Promise.all([
      api.get(`/teams/${id}`),
      api.get(`/matches?teamId=${id}&limit=10`)
    ])
    setTeam(t.data)
    setMatches(m.data)
  }
  useEffect(() => { load() }, [id])

  if (!team) return <p>Loading...</p>

  return (
    <div>
      {/* Banner */}
      <div style={{
        height: 160, borderRadius: 12, marginBottom: -40, position: 'relative', overflow: 'hidden',
        background: team.bannerUrl ? `url(${team.bannerUrl}) center/cover` : `linear-gradient(135deg, ${team.homeBg || '#333'} 0%, ${team.awayBg || '#666'} 100%)`
      }}>
        <div style={{ position: 'absolute', bottom: 8, right: 8 }}>
          <ImageUpload endpoint={`/upload/team/${id}/banner`} currentUrl={team.bannerUrl} onUploaded={load} size={32} shape="square" />
        </div>
      </div>

      {/* Header: logo + info */}
      <div style={{ display: 'flex', gap: 16, alignItems: 'flex-end', padding: '0 16px', marginBottom: 24, position: 'relative', zIndex: 1 }}>
        <div style={{ background: '#fff', borderRadius: 12, padding: 4, border: '2px solid #fff', boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
          <ImageUpload endpoint={`/upload/team/${id}`} currentUrl={team.logoUrl} onUploaded={load} size={72} shape="square" />
        </div>
        <div style={{ flex: 1, paddingBottom: 4 }}>
          <h1 style={{ margin: 0, fontSize: 24 }}>{team.name}</h1>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginTop: 4 }}>
            <Badge bg={team.homeBg} text={team.homeText} label={team.shortName} />
            <Badge bg={team.awayBg} text={team.awayText} label={team.shortName} small />
            {team.nation && <NationBadge nation={team.nation} />}
            <span style={{ fontSize: 12, color: '#999', background: '#f0f0f0', padding: '2px 8px', borderRadius: 4 }}>{team.type}</span>
          </div>
        </div>
      </div>

      {/* Colors */}
      <Section title="Colors">
        <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
          <ColorCard label="Home" bg={team.homeBg} text={team.homeText} shortName={team.shortName} />
          <ColorCard label="Away" bg={team.awayBg} text={team.awayText} shortName={team.shortName} />
        </div>
      </Section>

      {/* Description */}
      <Section title="Description">
        <EditableText value={team.description} placeholder="Add team description..." onSave={async (val) => {
          await api.put(`/teams/${id}`, { description: val })
          load()
        }} />
      </Section>

      {/* Assets grid */}
      <Section title="Assets">
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))', gap: 12 }}>
          <AssetSlot label="Logo" url={team.logoUrl} uploadEndpoint={`/upload/team/${id}`} onDone={load} />
          <AssetSlot label="Banner" url={team.bannerUrl} uploadEndpoint={`/upload/team/${id}/banner`} onDone={load} />
        </div>
        <p style={{ fontSize: 11, color: '#bbb', marginTop: 8 }}>More asset types (Jersey Kits, Team Image, Match Banners) coming via AI Studio.</p>
      </Section>

      {/* Recent Matches */}
      <Section title={`Recent Matches (${matches.length})`}>
        {matches.length === 0 ? <p style={{ color: '#999', fontSize: 13 }}>No matches found.</p> : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            {matches.map(m => (
              <div key={m.id} style={{
                display: 'flex', alignItems: 'center', gap: 8, padding: '8px 12px',
                background: '#fff', borderRadius: 8, border: '1px solid #eee', fontSize: 13
              }}>
                {m.roundName && <span style={{ color: '#999', fontSize: 11, minWidth: 40 }}>{m.roundName}</span>}
                <span style={{ flex: 1, textAlign: 'right', fontWeight: m.homeTeam?.id === id ? 700 : 400 }}>
                  {m.homeTeam?.name}
                </span>
                <span style={{
                  fontWeight: 700, minWidth: 44, textAlign: 'center', padding: '2px 8px', borderRadius: 4,
                  background: getResultColor(m, id), color: '#fff', fontSize: 12
                }}>
                  {m.homeScore} - {m.awayScore}
                </span>
                <span style={{ flex: 1, fontWeight: m.awayTeam?.id === id ? 700 : 400 }}>
                  {m.awayTeam?.name}
                </span>
                {m.leg && <span style={{ fontSize: 10, color: '#999' }}>L{m.leg}</span>}
              </div>
            ))}
          </div>
        )}
      </Section>
    </div>
  )
}

function getResultColor(match, teamId) {
  const isHome = match.homeTeam?.id === teamId
  const hs = match.homeScore, as = match.awayScore
  if (hs === as) return '#999'
  if ((isHome && hs > as) || (!isHome && as > hs)) return '#2ecc71'
  return '#e74c3c'
}

function Section({ title, children }) {
  return (
    <div style={{ marginBottom: 24 }}>
      <div style={{ fontSize: 12, fontWeight: 700, color: '#999', textTransform: 'uppercase', marginBottom: 8, letterSpacing: 0.5 }}>{title}</div>
      {children}
    </div>
  )
}

function Badge({ bg, text, label, small }) {
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
      width: small ? 32 : 38, height: small ? 20 : 26, borderRadius: 4,
      fontSize: small ? 9 : 10, fontWeight: 800, letterSpacing: 0.3,
      background: bg || '#333', color: text || '#fff', border: '1px solid rgba(0,0,0,0.08)',
    }}>{label || '???'}</span>
  )
}

function NationBadge({ nation }) {
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 4, padding: '2px 8px', borderRadius: 4,
      background: nation.primaryColor || '#666', color: nation.textColor || '#fff',
      fontSize: 10, fontWeight: 700,
    }}>
      {nation.flagUrl && <img src={nation.flagUrl} alt="" style={{ width: 14, height: 14, borderRadius: 2 }} />}
      {nation.code || nation.name}
    </span>
  )
}

function ColorCard({ label, bg, text, shortName }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
      <div style={{
        width: 48, height: 36, borderRadius: 6, display: 'flex', alignItems: 'center', justifyContent: 'center',
        background: bg || '#ccc', color: text || '#333', fontWeight: 800, fontSize: 11, border: '1px solid rgba(0,0,0,0.08)'
      }}>{shortName}</div>
      <div>
        <div style={{ fontSize: 11, fontWeight: 600 }}>{label}</div>
        <div style={{ fontSize: 10, color: '#999' }}>{bg || '—'} / {text || '—'}</div>
      </div>
    </div>
  )
}

function AssetSlot({ label, url, uploadEndpoint, onDone }) {
  return (
    <div style={{ background: '#fff', borderRadius: 8, border: '1px solid #eee', overflow: 'hidden', textAlign: 'center' }}>
      {url ? (
        <img src={url} alt={label} style={{ width: '100%', height: 100, objectFit: 'cover' }} />
      ) : (
        <div style={{ height: 100, display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f8f8f8' }}>
          <ImageUpload endpoint={uploadEndpoint} currentUrl={null} onUploaded={onDone} size={40} shape="square" />
        </div>
      )}
      <div style={{ padding: '6px 0', fontSize: 11, color: '#666', fontWeight: 600 }}>{label}</div>
    </div>
  )
}

function EditableText({ value, placeholder, onSave }) {
  const [editing, setEditing] = useState(false)
  const [text, setText] = useState(value || '')

  if (editing) {
    return (
      <div>
        <textarea value={text} onChange={e => setText(e.target.value)} rows={3}
          style={{ width: '100%', padding: 10, border: '1px solid #ddd', borderRadius: 6, fontSize: 13, resize: 'vertical' }} />
        <div style={{ display: 'flex', gap: 8, marginTop: 6 }}>
          <button onClick={() => { onSave(text); setEditing(false) }}
            style={{ padding: '4px 12px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 4, fontSize: 12, cursor: 'pointer' }}>Save</button>
          <button onClick={() => { setText(value || ''); setEditing(false) }}
            style={{ padding: '4px 12px', background: '#eee', border: 'none', borderRadius: 4, fontSize: 12, cursor: 'pointer' }}>Cancel</button>
        </div>
      </div>
    )
  }

  return (
    <p onClick={() => setEditing(true)} style={{
      padding: 12, background: '#fff', borderRadius: 8, border: '1px solid #eee', fontSize: 13, color: value ? '#333' : '#bbb',
      cursor: 'pointer', margin: 0, minHeight: 40, whiteSpace: 'pre-wrap'
    }}>
      {value || placeholder}
    </p>
  )
}
