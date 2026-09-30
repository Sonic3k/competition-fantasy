import { useMemo } from 'react'
import { Link, useParams } from 'react-router'
import { useCompetition } from '@/api/hooks'
import { Badge, Empty, ErrorBox, PageTitle, Spinner } from '@/components/ui'
import { statusLabel } from '@/lib/format'

export function CompetitionPage() {
  const id = Number(useParams().id)
  const q = useCompetition(id)

  const roll = useMemo(() => {
    const counts = new Map<number, { name: string; titles: number; seasons: string[] }>()
    for (const s of q.data?.seasons ?? []) {
      if (s.championTeamId == null) continue
      const e = counts.get(s.championTeamId) ?? { name: s.championName ?? `#${s.championTeamId}`, titles: 0, seasons: [] }
      e.titles += 1
      e.seasons.push(s.year ? String(s.year) : s.name)
      counts.set(s.championTeamId, e)
    }
    return [...counts.entries()].sort((a, b) => b[1].titles - a[1].titles || a[1].name.localeCompare(b[1].name))
  }, [q.data])

  if (q.isLoading) return <Spinner />
  if (q.error) return <ErrorBox error={q.error} />
  const { competition: c, seasons } = q.data!

  return (
    <div>
      <PageTitle eyebrow={c.universeKey ? <Link to={`/u/${c.universeKey}`} className="hover:underline">{c.universeName}</Link> : undefined} title={c.name}>
        {c.teamLevel === 'NATIONAL' ? 'National teams' : 'Clubs'}{c.tier ? ` · tier ${c.tier}` : ''}{c.description ? ` — ${c.description}` : ''}
      </PageTitle>
      <div className="grid gap-8 lg:grid-cols-[1fr_320px]">
        <section>
          {seasons.length === 0 && <Empty title="No seasons yet" />}
          {seasons.length > 0 && (
            <table className="ruled sheet w-full text-sm">
              <thead className="font-condensed text-ink-2">
                <tr>
                  <th className="px-4 py-2 text-left">Season</th>
                  <th className="px-2 py-2 text-left">Champion</th>
                  <th className="px-2 py-2 text-right">Teams</th>
                  <th className="px-4 py-2 text-right">Status</th>
                </tr>
              </thead>
              <tbody>
                {seasons.map((s) => (
                  <tr key={s.id} className="hover:bg-paper/60">
                    <td className="px-4 py-2.5">
                      <Link to={`/seasons/${s.id}`} className="font-medium hover:underline">{s.name}</Link>
                      {s.year && <span className="tnum ml-2 text-ink-3">{s.year}</span>}
                    </td>
                    <td className="px-2 py-2.5">
                      {s.championTeamId ? <Link to={`/teams/${s.championTeamId}`} className="hover:underline">{s.championName}</Link> : <span className="text-ink-3">—</span>}
                    </td>
                    <td className="tnum px-2 py-2.5 text-right text-ink-2">{s.teamCount}</td>
                    <td className="px-4 py-2.5 text-right">
                      <Badge tone={s.status === 'COMPLETED' ? 'neutral' : s.status === 'ONGOING' ? 'green' : 'amber'}>{statusLabel[s.status]}</Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
        <aside>
          <h2 className="mb-2 font-condensed text-lg font-semibold">Roll of honour</h2>
          {roll.length === 0 ? (
            <p className="text-sm text-ink-3">No champions recorded yet.</p>
          ) : (
            <ol className="sheet divide-y divide-rule-soft text-sm">
              {roll.map(([teamId, e]) => (
                <li key={teamId} className="flex items-baseline justify-between gap-3 px-4 py-2">
                  <Link to={`/teams/${teamId}`} className="font-medium hover:underline">{e.name}</Link>
                  <span className="tnum text-right text-ink-2">
                    <span className="font-semibold text-ink">{e.titles}</span>
                    <span className="ml-2 text-xs text-ink-3">{e.seasons.join(', ')}</span>
                  </span>
                </li>
              ))}
            </ol>
          )}
        </aside>
      </div>
    </div>
  )
}
