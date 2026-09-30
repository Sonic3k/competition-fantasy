import type { KoRoundView, MatchView, TeamRef, TieView } from '@/api/types'
import { TeamChip } from '@/components/TeamChip'
import { cx } from '@/components/ui'
import { fmtDate } from '@/lib/format'

function legScore(m: MatchView, side: 'home' | 'away', teamId: number | null): string {
  if (teamId == null) return ''
  const isHome = m.homeTeamId === teamId
  const s90 = isHome ? m.homeScore : m.awayScore
  if (s90 == null) return m.status === 'UNKNOWN' ? '?' : '–'
  const et = isHome ? m.homeEt : m.awayEt
  const val = et ?? s90
  void side
  return String(val)
}

function aggregate(t: TieView, teamId: number | null): number | null {
  if (teamId == null) return null
  let total = 0
  let any = false
  for (const m of t.matches) {
    if (m.homeScore == null || m.awayScore == null) continue
    any = true
    const isHome = m.homeTeamId === teamId
    const et = isHome ? m.homeEt : m.awayEt
    total += et ?? (isHome ? m.homeScore : m.awayScore) ?? 0
  }
  return any ? total : null
}

function resolutionText(t: TieView) {
  switch (t.resolution) {
    case 'AWAY_GOALS': return 'on away goals'
    case 'PENALTIES': {
      const m = t.matches[t.matches.length - 1]
      return m && m.homePens != null ? `on penalties ${m.homePens}–${m.awayPens}` : 'on penalties'
    }
    case 'EXTRA_TIME': return 'after extra time'
    case 'REPLAY': return 'after a replay'
    case 'WALKOVER': return 'walkover'
    case 'LOTS': return 'by drawing of lots'
    default: return null
  }
}

function sourceText(src: Record<string, unknown> | null): string {
  if (!src) return 'TBD'
  const type = String(src.type ?? '')
  if (type === 'GROUP_POSITION') return `${src.position}${ordinal(Number(src.position))} Group ${src.group}`
  if (type === 'BEST_THIRD') return `Best 3rd`
  if (type === 'WINNER') return `Winner ${src.round ?? ''} ${src.tie ?? ''}`.trim()
  if (type === 'LOSER') return `Loser ${src.round ?? ''} ${src.tie ?? ''}`.trim()
  return String(src.label ?? 'TBD')
}
function ordinal(n: number) { return n === 1 ? 'st' : n === 2 ? 'nd' : n === 3 ? 'rd' : 'th' }

export function TieCard({ tie, teams, legs }: { tie: TieView; teams: Record<string, TeamRef>; legs: number }) {
  const rows: Array<{ id: number | null; src: Record<string, unknown> | null }> = [
    { id: tie.homeTeamId, src: tie.homeSource },
    { id: tie.awayTeamId, src: tie.awaySource },
  ]
  const multiLeg = legs > 1 || tie.matches.length > 1
  const res = resolutionText(tie)
  return (
    <div className="sheet text-sm">
      {rows.map((r, i) => {
        const win = tie.winnerTeamId != null && tie.winnerTeamId === r.id
        const agg = multiLeg ? aggregate(tie, r.id) : null
        return (
          <div key={i} className={cx('grid items-center gap-2 px-3 py-1.5', i === 1 && 'border-t border-rule-soft')} style={{ gridTemplateColumns: `1fr repeat(${tie.matches.length}, 1.5rem)${multiLeg ? ' 2rem' : ''}` }}>
            {r.id != null ? <TeamChip team={teams[String(r.id)]} id={r.id} bold={win} muted={tie.winnerTeamId != null && !win} /> : <span className="text-ink-3">{sourceText(r.src)}</span>}
            {tie.matches.map((m) => (
              <span key={m.id} className={cx('tnum text-right', win ? 'font-semibold' : 'text-ink-2')}>{legScore(m, i === 0 ? 'home' : 'away', r.id)}</span>
            ))}
            {multiLeg && <span className={cx('tnum text-right font-semibold', !win && 'text-ink-2')}>{agg ?? ''}</span>}
          </div>
        )
      })}
      {(res || tie.matches.some((m) => m.date)) && (
        <div className="border-t border-rule-soft px-3 py-1 text-[11px] text-ink-3">
          {tie.matches.map((m) => fmtDate(m.date)).filter(Boolean).join(' · ')}
          {res && <span className="ml-2 text-ink-2">{res}</span>}
        </div>
      )}
    </div>
  )
}

/** Knockout rounds as columns, left to right; placement matches shown beneath. */
export function Bracket({ rounds, teams }: { rounds: KoRoundView[]; teams: Record<string, TeamRef> }) {
  const main = rounds.filter((r) => !r.placement)
  const placement = rounds.filter((r) => r.placement)
  if (rounds.length === 0) return <p className="sheet px-4 py-6 text-center text-sm text-ink-3">No knockout rounds yet.</p>
  return (
    <div>
      <div className="overflow-x-auto pb-2">
        <div className="flex items-stretch gap-4" style={{ minWidth: `${main.length * 260}px` }}>
          {main.map((r) => (
            <section key={r.id} className="flex w-[260px] shrink-0 flex-col">
              <h3 className="mb-2 font-condensed text-base font-semibold">
                {r.name} <span className="ml-1 text-xs font-normal text-ink-3">{r.legs === 2 ? 'two legs' : ''}</span>
              </h3>
              <div className="flex flex-1 flex-col justify-around gap-3">
                {r.ties.map((t) => <TieCard key={t.id} tie={t} teams={teams} legs={r.legs} />)}
                {r.ties.length === 0 && <p className="text-sm text-ink-3">Not drawn yet.</p>}
              </div>
            </section>
          ))}
        </div>
      </div>
      {placement.map((r) => (
        <section key={r.id} className="mt-4 max-w-[320px]">
          <h3 className="mb-2 font-condensed text-base font-semibold">{r.name}</h3>
          <div className="space-y-3">{r.ties.map((t) => <TieCard key={t.id} tie={t} teams={teams} legs={r.legs} />)}</div>
        </section>
      ))}
    </div>
  )
}
