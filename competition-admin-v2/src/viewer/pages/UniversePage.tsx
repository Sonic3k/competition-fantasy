import { useMemo, useState } from 'react'
import { Link, useParams } from 'react-router'
import { useCompetitions, useNations, useTeams, useUniverse } from '@/api/hooks'
import type { TeamDto } from '@/api/types'
import { Badge, Empty, ErrorBox, PageTitle, Spinner, Tabs } from '@/components/ui'
import { pickColor } from '@/lib/format'

type Tab = 'competitions' | 'nations' | 'teams'

export function UniversePage() {
  const { key = '' } = useParams()
  const universe = useUniverse(key)
  const competitions = useCompetitions(key)
  const nations = useNations(key, universe.data?.usesNations ?? true)
  const teams = useTeams(key)
  const [tab, setTab] = useState<Tab>('competitions')

  const teamsByNation = useMemo(() => {
    const map = new Map<string, TeamDto[]>()
    for (const t of teams.data ?? []) {
      const k = t.nationName ?? '—'
      map.set(k, [...(map.get(k) ?? []), t])
    }
    return [...map.entries()].sort((a, b) => a[0].localeCompare(b[0]))
  }, [teams.data])

  if (universe.isLoading) return <Spinner />
  if (universe.error) return <ErrorBox error={universe.error} />
  const u = universe.data!

  const tabs: Array<{ value: Tab; label: string; count?: number }> = [
    { value: 'competitions', label: 'Competitions', count: competitions.data?.length },
    ...(u.usesNations ? [{ value: 'nations' as Tab, label: 'Nations', count: nations.data?.length }] : []),
    { value: 'teams', label: 'Teams', count: teams.data?.length },
  ]

  return (
    <div>
      <PageTitle eyebrow={<Link to="/" className="hover:underline">Universes</Link>} title={u.name} right={<Badge tone={u.type === 'REAL' ? 'blue' : 'marker'}>{u.type === 'REAL' ? 'Real world' : 'Fictional'}</Badge>}>
        {u.description}
      </PageTitle>
      <Tabs value={tab} onChange={setTab} items={tabs} className="mb-6" />

      {tab === 'competitions' && (
        <>
          {competitions.isLoading && <Spinner />}
          {competitions.data?.length === 0 && <Empty title="No competitions in this universe yet" />}
          <ul className="sheet divide-y divide-rule-soft">
            {competitions.data?.map((c) => (
              <li key={c.id}>
                <Link to={`/competitions/${c.id}`} className="flex items-center justify-between gap-4 px-5 py-3 hover:bg-paper/60">
                  <div className="flex items-center gap-3">
                    {c.logoUrl && <img src={c.logoUrl} alt="" className="h-8 w-8 object-contain" />}
                    <div>
                      <div className="font-condensed text-lg font-semibold leading-tight">{c.name}</div>
                      <div className="text-xs text-ink-3">{c.teamLevel === 'NATIONAL' ? 'National teams' : 'Clubs'}{c.tier ? ` · tier ${c.tier}` : ''}</div>
                    </div>
                  </div>
                  <span className="tnum text-sm text-ink-2">{c.seasonCount} seasons</span>
                </Link>
              </li>
            ))}
          </ul>
        </>
      )}

      {tab === 'nations' && (
        <>
          {nations.isLoading && <Spinner />}
          {nations.data?.length === 0 && <Empty title="No nations yet" />}
          <ul className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
            {nations.data?.map((n) => (
              <li key={n.id} className="sheet">
                <Link to={`/nations/${n.id}`} className="flex items-center gap-3 px-4 py-3 hover:bg-paper/60">
                  {n.flagUrl ? <img src={n.flagUrl} alt="" className="h-6 w-9 rounded-[2px] object-cover" /> : <span className="h-6 w-9 rounded-[2px]" style={{ background: pickColor(n.colors) ?? 'var(--color-rule)' }} />}
                  <span className="font-medium">{n.name}</span>
                  <span className="ml-auto text-xs text-ink-3">{n.code}</span>
                </Link>
              </li>
            ))}
          </ul>
        </>
      )}

      {tab === 'teams' && (
        <>
          {teams.isLoading && <Spinner />}
          {teams.data?.length === 0 && <Empty title="No teams yet" />}
          <div className="grid gap-6 md:grid-cols-2">
            {teamsByNation.map(([nation, list]) => (
              <section key={nation} className="sheet">
                <h3 className="border-b border-rule px-4 py-2 font-condensed text-base font-semibold text-ink-2">{nation}</h3>
                <ul className="divide-y divide-rule-soft">
                  {list.map((t) => (
                    <li key={t.id}>
                      <Link to={`/teams/${t.id}`} className="flex items-center gap-3 px-4 py-2 text-sm hover:bg-paper/60">
                        {t.logoUrl ? <img src={t.logoUrl} alt="" className="h-5 w-5 object-contain" /> : <span className="h-3.5 w-1.5 rounded-[1px] bg-rule" />}
                        <span>{t.name}</span>
                        {t.type === 'NATIONAL' && <Badge>National</Badge>}
                        <span className="ml-auto text-xs text-ink-3">{t.code}</span>
                      </Link>
                    </li>
                  ))}
                </ul>
              </section>
            ))}
          </div>
        </>
      )}
    </div>
  )
}
