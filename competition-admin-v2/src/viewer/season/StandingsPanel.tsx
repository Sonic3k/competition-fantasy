import { useMemo, useState } from 'react'
import type { GroupView, StageDef, TeamRef } from '@/api/types'
import { useTableAsOf } from '@/api/hooks'
import { Spinner, cx } from '@/components/ui'
import { StandingsTable } from './StandingsTable'

type Source = 'RECORDED' | 'CALCULATED'

/**
 * Standings for one group. The notebook's RECORDED table is the default when one exists;
 * CALCULATED (from the match results) is one click away, with an "as of round" scrubber.
 */
export function StandingsPanel({ group, teams, stageConfig, compact, caption, highlightTeamId }: {
  group: GroupView
  teams: Record<string, TeamRef>
  stageConfig: StageDef | null
  compact?: boolean
  caption?: string
  highlightTeamId?: number | null
}) {
  const recorded = useMemo(() => [...group.recorded].sort((a, b) => (a.checkpointRound || 999) - (b.checkpointRound || 999)), [group.recorded])
  const hasRecorded = recorded.length > 0
  const finalRecorded = recorded.find((r) => r.checkpointRound === 0) ?? recorded[recorded.length - 1]
  const [source, setSource] = useState<Source>(hasRecorded ? 'RECORDED' : 'CALCULATED')
  const [checkpoint, setCheckpoint] = useState<number>(finalRecorded?.checkpointRound ?? 0)
  const roundsPlayed = group.rounds.filter((r) => r.matches.some((m) => m.homeScore != null)).map((r) => r.number)
  const lastRound = roundsPlayed.length ? Math.max(...roundsPlayed) : 0
  const [asOf, setAsOf] = useState<number | 'all'>('all')
  const asOfQuery = useTableAsOf(group.id, source === 'CALCULATED' && asOf !== 'all' ? asOf : undefined)

  const showRecorded = source === 'RECORDED' && hasRecorded
  const recordedTable = showRecorded ? recorded.find((r) => r.checkpointRound === checkpoint) ?? finalRecorded : null

  const rows = showRecorded
    ? recordedTable?.rows ?? []
    : asOf === 'all'
      ? group.calculated?.rows ?? []
      : asOfQuery.data?.rows ?? []

  const mismatch = useMemo(() => {
    if (!finalRecorded || !group.calculated || finalRecorded.checkpointRound !== 0) return null
    const calc = new Map(group.calculated.rows.map((r) => [r.teamId, r]))
    let diffs = 0
    for (const r of finalRecorded.rows) {
      const c = calc.get(r.teamId)
      if (!c || c.points !== r.points || c.goalsFor !== r.goalsFor || c.goalsAgainst !== r.goalsAgainst) diffs++
    }
    return diffs
  }, [finalRecorded, group.calculated])

  return (
    <div>
      {(hasRecorded || !compact) && (
      <div className="mb-2 flex flex-wrap items-center gap-2 text-xs">
        <div className="inline-flex rounded-sm border border-rule bg-sheet p-0.5">
          <button type="button" onClick={() => setSource('RECORDED')} disabled={!hasRecorded} className={cx('rounded-[2px] px-2 py-1', source === 'RECORDED' && hasRecorded ? 'bg-ink text-sheet' : 'text-ink-2 disabled:opacity-40')}>
            Notebook
          </button>
          <button type="button" onClick={() => setSource('CALCULATED')} className={cx('rounded-[2px] px-2 py-1', source === 'CALCULATED' || !hasRecorded ? 'bg-ink text-sheet' : 'text-ink-2')}>
            Calculated
          </button>
        </div>
        {showRecorded && recorded.length > 1 && (
          <select value={checkpoint} onChange={(e) => setCheckpoint(Number(e.target.value))} className="h-7 rounded-sm border border-rule bg-sheet px-1.5 text-xs">
            {recorded.map((r) => (
              <option key={r.id} value={r.checkpointRound}>{r.checkpointRound === 0 ? 'Final table' : `After round ${r.checkpointRound}`}</option>
            ))}
          </select>
        )}
        {!showRecorded && !compact && lastRound > 1 && (
          <label className="inline-flex items-center gap-1.5 text-ink-2">
            as of round
            <select value={asOf} onChange={(e) => setAsOf(e.target.value === 'all' ? 'all' : Number(e.target.value))} className="h-7 rounded-sm border border-rule bg-sheet px-1.5 text-xs">
              <option value="all">latest</option>
              {Array.from({ length: lastRound }, (_, i) => i + 1).map((n) => <option key={n} value={n}>{n}</option>)}
            </select>
          </label>
        )}
        {mismatch != null && mismatch > 0 && (
          <span className="text-playoff" title="The notebook table differs from what the recorded results add up to">
            {mismatch} {mismatch > 1 ? 'rows differ' : 'row differs'} from the results
          </span>
        )}
      </div>
      )}
      {!showRecorded && asOf !== 'all' && asOfQuery.isLoading ? (
        <Spinner />
      ) : rows.length === 0 ? (
        <p className="sheet px-4 py-6 text-center text-sm text-ink-3">No table yet.</p>
      ) : (
        <StandingsTable rows={rows} teams={teams} zones={stageConfig?.zones} compact={compact} caption={caption} highlightTeamId={highlightTeamId} />
      )}
    </div>
  )
}
