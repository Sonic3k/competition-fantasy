import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/client'
import ImageUpload from '../components/ImageUpload'

export default function NationDetail() {
  const { id } = useParams()
  const [nation, setNation] = useState(null)
  const [teams, setTeams] = useState([])

  const load = async () => {
    const [n, t] = await Promise.all([
      api.get(`/nations/${id}`),
      api.get(`/nations/${id}/teams`)
    ])
    setNation(n.data)
    setTeams(t.data)
  }
  useEffect(() => { load() }, [id])

  if (!nation) return <p>Loading...</p>

  const clubs = teams.filter(t => t.type === 'CLUB')
  const nationals = teams.filter(t => t.type === 'NATIONAL')

  return (
    <div>
      {/* Back link */}
      {nation.universeId && <Link to={`/universes/${nation.universeId}`} style={{ color: '#e94560', textDecoration: 'none', fontSize: 13 }}>&larr; Back to Universe</Link>}

      {/* Banner */}
      <div style={{
        height: 140, borderRadius: 12, marginTop: 8, marginBottom: -36, position: 'relative', overflow: 'hidden',
        background: nation.bannerUrl ? `url(${nation.bannerUrl}) center/cover` : `linear-gradient(135deg, ${nation.primaryColor || '#333'} 0%, ${nation.awayColor || '#666'} 100%)`
      }}>
        <div style={{ position: 'absolute', bottom: 8, right: 8 }}>
          <ImageUpload endpoint={`/upload/nation/${id}/banner`} currentUrl={nation.bannerUrl} onUploaded={load} size={28} shape="square" />
        </div>
      </div>

      {/* Header */}
      <div style={{ display: 'flex', gap: 16, alignItems: 'flex-end', padding: '0 16px', marginBottom: 20, position: 'relative', zIndex: 1 }}>
        <div style={{ background: '#fff', borderRadius: 12, padding: 4, border: '2px solid #fff', boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
          <ImageUpload endpoint={`/upload/nation/${id}/logo`} currentUrl={nation.logoUrl} onUploaded={load} size={64} shape="square" />
        </div>
        <div style={{ flex: 1, paddingBottom: 4 }}>
          <h1 style={{ margin: 0, fontSize: 22 }}>{nation.name}</h1>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginTop: 4 }}>
            <Badge bg={nation.primaryColor} text={nation.textColor} label={nation.code} />
            <Badge bg={nation.awayColor} text={nation.awayTextColor} label={nation.code} small />
          </div>
        </div>
      </div>

      {/* Emblem */}
      <Section title="Emblem">
        <div style={{ display: 'flex', gap: 16, alignItems: 'center' }}>
          <div style={{ background: '#fff', borderRadius: 8, padding: 8, border: '1px solid #eee' }}>
            <ImageUpload endpoint={`/upload/nation/${id}`} currentUrl={nation.flagUrl} onUploaded={load} size={80} shape="square" />
          </div>
          <div style={{ fontSize: 12, color: '#999' }}>
            {nation.flagUrl ? 'Click to view, ✎ to change' : 'Upload emblem'}
          </div>
        </div>
      </Section>

      {/* Colors */}
      <Section title="Colors">
        <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
          <ColorCard label="Home" bg={nation.primaryColor} text={nation.textColor} code={nation.code} />
          <ColorCard label="Away" bg={nation.awayColor} text={nation.awayTextColor} code={nation.code} />
        </div>
      </Section>

      {/* Description */}
      <Section title="Description">
        <EditableText value={nation.description} placeholder="Add nation description..." onSave={async (val) => {
          await api.put(`/nations/${id}`, { description: val })
          load()
        }} />
      </Section>

      {/* National Teams */}
      {nationals.length > 0 && (
        <Section title={`National Teams (${nationals.length})`}>
          <TeamList teams={nationals} />
        </Section>
      )}

      {/* Clubs */}
      <Section title={`Clubs (${clubs.length})`}>
        {clubs.length === 0 ? <p style={{ color: '#999', fontSize: 13 }}>No clubs registered under this nation.</p> : (
          <TeamList teams={clubs} />
        )}
      </Section>
    </div>
  )
}

function TeamList({ teams }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
      {teams.map(t => (
        <Link key={t.id} to={`/teams/${t.id}`} style={{ textDecoration: 'none', color: '#333' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, background: '#fff', padding: '10px 14px', borderRadius: 8, border: '1px solid #eee' }}>
            {t.logoUrl ? (
              <img src={t.logoUrl} alt="" style={{ width: 32, height: 32, borderRadius: 4, objectFit: 'cover' }} />
            ) : (
              <Badge bg={t.homeBg} text={t.homeText} label={t.shortName} />
            )}
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 600, fontSize: 14 }}>{t.name}</div>
              <div style={{ fontSize: 11, color: '#999' }}>{t.type}</div>
            </div>
            <Badge bg={t.homeBg} text={t.homeText} label={t.shortName} small />
            <Badge bg={t.awayBg} text={t.awayText} label={t.shortName} small />
          </div>
        </Link>
      ))}
    </div>
  )
}

function Badge({ bg, text, label, small }) {
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
      width: small ? 32 : 40, height: small ? 20 : 26, borderRadius: 4,
      fontSize: small ? 9 : 10, fontWeight: 800, letterSpacing: 0.3,
      background: bg || '#333', color: text || '#fff', border: '1px solid rgba(0,0,0,0.08)',
    }}>{label || '???'}</span>
  )
}

function ColorCard({ label, bg, text, code }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
      <div style={{
        width: 48, height: 36, borderRadius: 6, display: 'flex', alignItems: 'center', justifyContent: 'center',
        background: bg || '#ccc', color: text || '#333', fontWeight: 800, fontSize: 11, border: '1px solid rgba(0,0,0,0.08)'
      }}>{code}</div>
      <div>
        <div style={{ fontSize: 11, fontWeight: 600 }}>{label}</div>
        <div style={{ fontSize: 10, color: '#999' }}>{bg || '—'} / {text || '—'}</div>
      </div>
    </div>
  )
}

function Section({ title, children }) {
  return (
    <div style={{ marginBottom: 24 }}>
      <div style={{ fontSize: 12, fontWeight: 700, color: '#999', textTransform: 'uppercase', marginBottom: 8, letterSpacing: 0.5 }}>{title}</div>
      {children}
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
