import { useState } from 'react'
import type { MatchView, RoundView, TeamRef } from '@/api/types'
import { TeamChip } from '@/components/TeamChip'
import { cx } from '@/components/ui'
import { fmtDate, scoreDetail, scoreText } from '@/lib/format'

export function MatchRow({ m, teams, showLeg }: { m: MatchView; teams: Record<string, TeamRef>; showLeg?: boolean }) {
  const home = teams[String(m.homeTeamId)]
  const away = teams[String(m.awayTeamId)]
  const detail = scoreDetail(m)
  const played = m.homeScore != null && m.awayScore != null
  const homeWin = played && m.winnerTeamId != null && m.winnerTeamId === m.homeTeamId
  const awayWin = played && m.winnerTeamId != null && m.winnerTeamId === m.awayTeamId
  return (
    <li className="grid grid-cols-[1fr_auto_1fr] items-center gap-2 px-3 py-1.5 text-sm">
      <div className="flex justify-end text-right">
        <TeamChip team={home} id={m.homeTeamId} bold={homeWin} className="flex-row-reverse" />
      </div>
      <div className="text-center">
        <span className={cx('tnum inline-block min-w-[3.25rem] rounded-sm px-1.5 py-0.5 font-semibold', played ? 'bg-ink text-sheet' : 'border border-rule text-ink-3', m.status === 'UNKNOWN' && 'border-dashed')} title={m.status === 'UNKNOWN' ? 'Result not recorded' : detail ?? undefined}>
          {scoreText(m)}
        </span>
        {detail && <div className="mt-0.5 text-[11px] leading-tight text-ink-3">{detail}</div>}
        {(showLeg || m.date) && (
          <div className="mt-0.5 text-[11px] leading-tight text-ink-3">
            {showLeg && `leg ${m.leg}`}{showLeg && m.date && ' · '}{fmtDate(m.date)}
          </div>
        )}
      </div>
      <div className="flex justify-start">
        <TeamChip team={away} id={m.awayTeamId} bold={awayWin} />
      </div>
    </li>
  )
}

/** Rounds of a group, one matchday at a time; defaults to the latest round with results. */
export function RoundsPanel({ rounds, teams, compact }: { rounds: RoundView[]; teams: Record<string, TeamRef>; compact?: boolean }) {
  const withResults = rounds.filter((r) => r.matches.some((m) => m.homeScore != null))
  const initial = withResults.length ? withResults[withResults.length - 1].id : rounds[0]?.id
  const [current, setCurrent] = useState<number | 'all'>(compact ? 'all' : initial ?? 'all')
  if (rounds.length === 0) return <p className="sheet px-4 py-6 text-center text-sm text-ink-3">No fixtures yet.</p>
  const list = current === 'all' ? rounds : rounds.filter((r) => r.id === current)
  return (
    <div>
      {rounds.length > 1 && (
        <div className="mb-2 flex flex-wrap gap-1">
          {rounds.map((r) => (
            <button key={r.id} type="button" onClick={() => setCurrent(r.id)} className={cx('tnum h-7 min-w-7 rounded-sm border px-1.5 text-xs', current === r.id ? 'border-ink bg-ink text-sheet' : 'border-rule bg-sheet text-ink-2 hover:border-ink-3')} title={r.name}>
              {r.number}
            </button>
          ))}
          <button type="button" onClick={() => setCurrent('all')} className={cx('h-7 rounded-sm border px-2 text-xs', current === 'all' ? 'border-ink bg-ink text-sheet' : 'border-rule bg-sheet text-ink-2 hover:border-ink-3')}>
            All
          </button>
        </div>
      )}
      <div className="space-y-3">
        {list.map((r) => (
          <section key={r.id} className="sheet">
            <header className="flex items-baseline justify-between border-b border-rule-soft px-3 py-1.5">
              <h4 className="font-condensed text-[15px] font-semibold">{r.name}</h4>
              <span className="text-xs text-ink-3">{fmtDate(r.startDate)}{r.endDate && r.endDate !== r.startDate ? ` – ${fmtDate(r.endDate)}` : ''}</span>
            </header>
            <ul className="divide-y divide-rule-soft">
              {r.matches.map((m) => <MatchRow key={m.id} m={m} teams={teams} />)}
              {r.matches.length === 0 && <li className="px-3 py-2 text-sm text-ink-3">No matches recorded.</li>}
            </ul>
          </section>
        ))}
      </div>
    </div>
  )
}
