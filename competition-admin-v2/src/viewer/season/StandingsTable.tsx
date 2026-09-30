import type { RankedRow, StandingRowView, TeamRef, Zone, ZoneKind } from '@/api/types'
import { TeamChip } from '@/components/TeamChip'
import { cx } from '@/components/ui'
import { zoneClass, zoneLabel } from '@/lib/format'

export type AnyRow = StandingRowView | (RankedRow & { zone?: ZoneKind | null })

export function zonesByPosition(zones: Zone[] | undefined): Map<number, { kind: ZoneKind; label: string }> {
  const map = new Map<number, { kind: ZoneKind; label: string }>()
  for (const z of zones ?? []) for (const p of z.positions ?? []) if (!map.has(p)) map.set(p, { kind: z.kind, label: z.label ?? zoneLabel[z.kind] })
  return map
}

/** The hero of the viewer: a ruled, tabular standings table with team colours and zone stripes. */
export function StandingsTable({ rows, teams, zones, compact, highlightTeamId, caption }: {
  rows: AnyRow[]
  teams: Record<string, TeamRef>
  zones?: Zone[]
  compact?: boolean
  highlightTeamId?: number | null
  caption?: string
}) {
  const zoneMap = zonesByPosition(zones)
  const legend = [...new Map([...zoneMap.values()].map((z) => [z.kind + z.label, z])).values()].filter((z) => z.kind !== 'OTHER' || z.label)
  const cell = compact ? 'px-1.5 py-1' : 'px-2 py-1.5'
  return (
    <div className="sheet overflow-x-auto">
      <table className={cx('ruled tnum w-full', compact ? 'text-[13px]' : 'text-sm')}>
        {caption && <caption className="px-3 pt-2 text-left font-condensed text-base font-semibold">{caption}</caption>}
        <thead className="font-condensed text-ink-2">
          <tr>
            <th className={cx(cell, 'w-8 text-right')}>#</th>
            <th className={cx(cell, 'text-left')}>Team</th>
            <th className={cx(cell, 'text-right')}>P</th>
            <th className={cx(cell, 'text-right')}>W</th>
            <th className={cx(cell, 'text-right')}>D</th>
            <th className={cx(cell, 'text-right')}>L</th>
            {!compact && <th className={cx(cell, 'text-right')}>GF</th>}
            {!compact && <th className={cx(cell, 'text-right')}>GA</th>}
            <th className={cx(cell, 'text-right')}>{compact ? '+/-' : 'GD'}</th>
            <th className={cx(cell, 'text-right font-semibold text-ink')}>Pts</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((r) => {
            const zone = ('zone' in r && r.zone) || zoneMap.get(r.position)?.kind || null
            const team = teams[String(r.teamId)]
            const hl = highlightTeamId != null && highlightTeamId === r.teamId
            return (
              <tr key={r.teamId} className={cx('relative', hl && 'bg-marker-soft', zone === 'CHAMPION' && 'font-semibold')}>
                <td className={cx(cell, 'relative text-right text-ink-2')}>
                  {zone && zone !== 'OTHER' && <span className={cx('absolute inset-y-0 left-0 w-1', zoneClass[zone])} aria-hidden />}
                  {r.position}
                </td>
                <td className={cx(cell, 'max-w-[260px] text-left')}>
                  <TeamChip team={team} id={r.teamId} size={compact ? 'sm' : 'md'} />
                  {r.tieNote && <span className="ml-1 text-ink-3" title={r.tieNote}>*</span>}
                </td>
                <td className={cx(cell, 'text-right')}>{r.played}</td>
                <td className={cx(cell, 'text-right')}>{r.won}</td>
                <td className={cx(cell, 'text-right')}>{r.drawn}</td>
                <td className={cx(cell, 'text-right')}>{r.lost}</td>
                {!compact && <td className={cx(cell, 'text-right')}>{r.goalsFor}</td>}
                {!compact && <td className={cx(cell, 'text-right')}>{r.goalsAgainst}</td>}
                <td className={cx(cell, 'text-right')}>{r.goalDiff > 0 ? `+${r.goalDiff}` : r.goalDiff}</td>
                <td className={cx(cell, 'text-right font-semibold text-ink')}>
                  {r.points}
                  {r.adjustment !== 0 && <span className="ml-0.5 text-xs font-normal text-relegation" title="Points adjustment">{r.adjustment > 0 ? `+${r.adjustment}` : r.adjustment}</span>}
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
      {legend.length > 0 && (
        <div className="flex flex-wrap gap-x-4 gap-y-1 border-t border-rule-soft px-3 py-2 text-xs text-ink-2">
          {legend.map((z) => (
            <span key={z.kind + z.label} className="inline-flex items-center gap-1.5">
              <span className={cx('h-2.5 w-1', zoneClass[z.kind])} />
              {z.label}
            </span>
          ))}
        </div>
      )}
    </div>
  )
}
