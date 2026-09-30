import { useEffect, useMemo, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router'
import { useSeason } from '@/api/hooks'
import { TeamChip } from '@/components/TeamChip'
import { Badge, ErrorBox, PageTitle, Spinner, Tabs } from '@/components/ui'
import { auth } from '@/api/client'
import { statusLabel } from '@/lib/format'
import { StageRenderer } from '../season/StageRenderer'

export function SeasonPage() {
  const id = Number(useParams().id)
  const q = useSeason(id)
  const [params, setParams] = useSearchParams()
  const stages = q.data?.stages ?? []
  const [stageKey, setStageKey] = useState<string>(params.get('stage') ?? '')

  useEffect(() => {
    if (!stages.length) return
    if (!stages.some((s) => s.key === stageKey)) {
      // default: the last stage that has any result, else the first
      const withResults = stages.filter((s) => s.groups.some((g) => g.rounds.some((r) => r.matches.some((m) => m.homeScore != null))) || s.koRounds.some((k) => k.ties.some((t) => t.matches.some((m) => m.homeScore != null))))
      setStageKey((withResults[withResults.length - 1] ?? stages[0]).key)
    }
  }, [stages, stageKey])

  const champion = useMemo(() => q.data?.honours.find((h) => h.kind === 'CHAMPION'), [q.data])
  const podium = useMemo(() => (q.data?.honours ?? []).filter((h) => h.kind !== 'CHAMPION'), [q.data])

  if (q.isLoading) return <Spinner />
  if (q.error) return <ErrorBox error={q.error} />
  const v = q.data!
  const stage = stages.find((s) => s.key === stageKey) ?? stages[0]

  const setStage = (k: string) => {
    setStageKey(k)
    setParams((p) => { p.set('stage', k); return p }, { replace: true })
  }

  return (
    <div>
      <PageTitle
        eyebrow={
          <span>
            <Link to={`/u/${v.universe.key}`} className="hover:underline">{v.universe.name}</Link>
            <span className="mx-1.5 text-ink-3">/</span>
            <Link to={`/competitions/${v.competition.id}`} className="hover:underline">{v.competition.name}</Link>
          </span>
        }
        title={<>{v.season.name}{v.season.year && <span className="tnum ml-3 text-ink-3">{v.season.year}</span>}</>}
        right={
          <>
            <Badge tone={v.season.status === 'COMPLETED' ? 'neutral' : v.season.status === 'ONGOING' ? 'green' : 'amber'}>{statusLabel[v.season.status]}</Badge>
            {v.format?.name && <Badge>{v.format.name}</Badge>}
            {auth.loggedIn() && <Link to={`/admin/seasons/${v.season.id}`} className="text-sm text-biro hover:underline">Edit</Link>}
          </>
        }
      >
        {champion && (
          <div className="flex flex-wrap items-center gap-x-4 gap-y-1">
            <span className="inline-flex items-center gap-2 rounded-sm bg-marker-soft px-2 py-1">
              <span className="text-xs text-ink-2">Champion</span>
              <TeamChip team={v.teams[String(champion.teamId)]} id={champion.teamId} bold />
            </span>
            {podium.map((h) => (
              <span key={h.id} className="inline-flex items-center gap-1.5 text-xs text-ink-2">
                {h.kind.replace('_', ' ').toLowerCase()} <TeamChip team={v.teams[String(h.teamId)]} id={h.teamId} size="sm" />
              </span>
            ))}
          </div>
        )}
        {v.season.notes && <p className="mt-2 max-w-2xl text-ink-3">{v.season.notes}</p>}
      </PageTitle>

      {stages.length === 0 && <p className="sheet px-4 py-8 text-center text-ink-3">This season has no stages yet.</p>}
      {stages.length > 1 && <Tabs value={stage?.key ?? ''} onChange={setStage} items={stages.map((s) => ({ value: s.key, label: s.name }))} className="mb-6" />}
      {stage && <StageRenderer key={stage.id} stage={stage} teams={v.teams} />}
    </div>
  )
}
