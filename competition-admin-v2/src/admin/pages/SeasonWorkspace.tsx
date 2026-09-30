import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link, useParams, useSearchParams } from 'react-router'
import { del, post } from '@/api/client'
import { keys, useAction, useSeason } from '@/api/hooks'
import type { GroupView, KoRoundView, SeasonView, StageType, StageView } from '@/api/types'
import { Badge, Button, Confirm, Dialog, ErrorBox, Field, Input, PageTitle, Select, Spinner, cx } from '@/components/ui'
import { numOrNull, readForm, strOrNull } from '@/lib/forms'
import { stageTypeLabel, statusLabel } from '@/lib/format'
import { GroupEditor } from '../season/GroupEditor'
import { KoRoundEditor } from '../season/KoRoundEditor'
import { SeasonTeamsDialog } from '../season/SeasonTeamsDialog'

type Selection = { kind: 'group'; id: number } | { kind: 'ko'; id: number } | null

/** Everything about one season on one screen: teams, structure on the left, the selected group or round on the right. */
export function SeasonWorkspace() {
  const id = Number(useParams().id)
  const q = useSeason(id)
  const [params, setParams] = useSearchParams()
  const [selection, setSelection] = useState<Selection>(() => {
    const g = params.get('group'), k = params.get('ko')
    return g ? { kind: 'group', id: Number(g) } : k ? { kind: 'ko', id: Number(k) } : null
  })
  const [teamsOpen, setTeamsOpen] = useState(false)
  const [stageOpen, setStageOpen] = useState(false)
  const [confirm, setConfirm] = useState<{ title: string; message: string; run: () => void } | null>(null)
  const inval = [keys.season(id)]
  const recalc = useAction(() => post(`/api/admin/seasons/${id}/recalculate`), inval)
  const scaffold = useAction(() => post(`/api/admin/seasons/${id}/scaffold`), inval)
  const addStage = useAction((body: unknown) => post(`/api/admin/seasons/${id}/stages`, body), inval)
  const deleteStage = useAction((sid: number) => del(`/api/admin/stages/${sid}`), inval)
  const addGroup = useAction((a: { stageId: number; key: string; name: string }) => post(`/api/admin/stages/${a.stageId}/groups`, { key: a.key, name: a.name }), inval)
  const deleteGroup = useAction((gid: number) => del(`/api/admin/stage-groups/${gid}`), inval)
  const addKo = useAction((a: { stageId: number; key: string; name: string; legs: number; placement: boolean }) => post(`/api/admin/stages/${a.stageId}/ko-rounds`, a), inval)
  const deleteKo = useAction((kid: number) => del(`/api/admin/ko-rounds/${kid}`), inval)
  const addHonour = useAction((body: unknown) => post(`/api/admin/seasons/${id}/honours`, body), [keys.season(id), keys.seasons(q.data?.competition.id ?? 0)])
  const deleteHonour = useAction((hid: number) => del(`/api/admin/honours/${hid}`), [keys.season(id), keys.seasons(q.data?.competition.id ?? 0)])

  const v = q.data
  // keep the selection valid and reflected in the URL
  useEffect(() => {
    if (!v) return
    const groups = v.stages.flatMap((s) => s.groups)
    const kos = v.stages.flatMap((s) => s.koRounds)
    let sel = selection
    if (sel?.kind === 'group' && !groups.some((g) => g.id === sel!.id)) sel = null
    if (sel?.kind === 'ko' && !kos.some((k) => k.id === sel!.id)) sel = null
    if (!sel) sel = groups[0] ? { kind: 'group', id: groups[0].id } : kos[0] ? { kind: 'ko', id: kos[0].id } : null
    if (sel !== selection) setSelection(sel)
    setParams((p) => { p.delete('group'); p.delete('ko'); if (sel) p.set(sel.kind, String(sel.id)); return p }, { replace: true })
  }, [v, selection, setParams])

  const selected = useMemo((): { kind: 'group'; stage: StageView; group: GroupView } | { kind: 'ko'; stage: StageView; ko: KoRoundView } | null => {
    if (!v || !selection) return null
    for (const s of v.stages) {
      if (selection.kind === 'group') { const g = s.groups.find((g) => g.id === selection.id); if (g) return { kind: 'group', stage: s, group: g } }
      else { const k = s.koRounds.find((k) => k.id === selection.id); if (k) return { kind: 'ko', stage: s, ko: k } }
    }
    return null
  }, [v, selection])

  if (q.isLoading) return <Spinner />
  if (q.error) return <ErrorBox error={q.error} />
  if (!v) return null

  function submitStage(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const f = readForm(e.currentTarget)
    addStage.mutate({ key: f.key, name: f.name, type: f.type as StageType, ordinal: numOrNull(f.ordinal) }, { onSuccess: () => setStageOpen(false) })
  }
  function submitHonour(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const f = readForm(e.currentTarget)
    if (!f.teamId) return
    addHonour.mutate({ teamId: Number(f.teamId), kind: f.kind })
    e.currentTarget.reset()
  }

  const formatStageKeys = v.format?.stages.map((s) => s.key) ?? []

  return (
    <div>
      <PageTitle
        eyebrow={<span><Link to="/admin" className="hover:underline">Universes</Link> <span className="text-ink-3">/</span> <Link to={`/admin/universes/${v.universe.key}?tab=competitions`} className="hover:underline">{v.universe.name}</Link> <span className="text-ink-3">/</span> <Link to={`/admin/competitions/${v.competition.id}`} className="hover:underline">{v.competition.name}</Link></span>}
        title={<>{v.season.name}{v.season.year && <span className="tnum ml-3 text-ink-3">{v.season.year}</span>}</>}
        right={
          <>
            <Badge tone={v.season.status === 'COMPLETED' ? 'neutral' : v.season.status === 'ONGOING' ? 'green' : 'amber'}>{statusLabel[v.season.status]}</Badge>
            {v.format && <Badge>{v.format.name ?? v.format.key}</Badge>}
            <Link to={`/seasons/${v.season.id}`} className="text-sm text-biro hover:underline">View</Link>
            <Button onClick={() => recalc.mutate(undefined)} disabled={recalc.isPending}>{recalc.isPending ? 'Recalculating…' : 'Recalculate tables'}</Button>
          </>
        }
      />
      <ErrorBox error={recalc.error ?? scaffold.error ?? deleteStage.error ?? deleteGroup.error ?? deleteKo.error ?? addGroup.error ?? addKo.error} className="mb-4" />

      <div className="grid gap-6 lg:grid-cols-[300px_minmax(0,1fr)]">
        <aside className="space-y-5">
          <section className="sheet p-3">
            <div className="flex items-center justify-between">
              <h2 className="font-condensed text-base font-semibold">Season teams <span className="tnum text-sm font-normal text-ink-3">{v.seasonTeams.length}</span></h2>
              <Button size="sm" onClick={() => setTeamsOpen(true)}>Edit</Button>
            </div>
            {v.seasonTeams.length === 0 && <p className="mt-1 text-xs text-ink-3">No teams yet. Add teams before building groups.</p>}
          </section>

          <section>
            <div className="mb-2 flex items-center justify-between">
              <h2 className="font-condensed text-base font-semibold">Structure</h2>
              <div className="flex gap-1">
                {v.stages.length === 0 && v.format && <Button size="sm" onClick={() => scaffold.mutate(undefined)} disabled={scaffold.isPending}>Build from format</Button>}
                <Button size="sm" onClick={() => setStageOpen(true)}>Add stage</Button>
              </div>
            </div>
            {v.stages.length === 0 && <p className="sheet px-3 py-4 text-sm text-ink-3">No stages yet. Build them from the format, or add one by hand.</p>}
            <div className="space-y-3">
              {v.stages.map((s) => (
                <div key={s.id} className="sheet">
                  <header className="flex items-center justify-between border-b border-rule-soft px-3 py-1.5">
                    <div>
                      <div className="font-condensed text-[15px] font-semibold">{s.name}</div>
                      <div className="text-[11px] text-ink-3">{stageTypeLabel[s.type]} · {s.key}</div>
                    </div>
                    <Button size="sm" variant="ghost" aria-label="Delete stage" onClick={() => setConfirm({ title: 'Delete stage', message: `Delete stage "${s.name}" with its groups, rounds and matches?`, run: () => deleteStage.mutate(s.id) })}>✕</Button>
                  </header>
                  <ul className="py-1 text-sm">
                    {s.groups.map((g) => (
                      <li key={g.id} className="group flex items-center">
                        <button type="button" onClick={() => setSelection({ kind: 'group', id: g.id })} className={cx('flex flex-1 items-center justify-between px-3 py-1 text-left hover:bg-paper', selection?.kind === 'group' && selection.id === g.id && 'bg-biro-soft text-biro')}>
                          <span>{g.name}</span>
                          <span className="tnum text-xs text-ink-3">{g.teams.length}t · {g.rounds.reduce((n, r) => n + r.matches.length, 0)}m</span>
                        </button>
                        <button type="button" className="px-2 text-xs text-ink-3 opacity-0 hover:text-relegation group-hover:opacity-100" aria-label="Delete group" onClick={() => setConfirm({ title: 'Delete group', message: `Delete ${g.name} and its matches?`, run: () => deleteGroup.mutate(g.id) })}>✕</button>
                      </li>
                    ))}
                    {s.koRounds.map((k) => (
                      <li key={k.id} className="group flex items-center">
                        <button type="button" onClick={() => setSelection({ kind: 'ko', id: k.id })} className={cx('flex flex-1 items-center justify-between px-3 py-1 text-left hover:bg-paper', selection?.kind === 'ko' && selection.id === k.id && 'bg-biro-soft text-biro')}>
                          <span>{k.name}</span>
                          <span className="tnum text-xs text-ink-3">{k.ties.length} ties</span>
                        </button>
                        <button type="button" className="px-2 text-xs text-ink-3 opacity-0 hover:text-relegation group-hover:opacity-100" aria-label="Delete round" onClick={() => setConfirm({ title: 'Delete round', message: `Delete ${k.name} and its ties?`, run: () => deleteKo.mutate(k.id) })}>✕</button>
                      </li>
                    ))}
                    <li className="px-3 pt-1">
                      {s.type === 'ROUND_ROBIN' || s.type === 'LEAGUE_PHASE' ? (
                        <button type="button" className="text-xs text-biro hover:underline" onClick={() => { const n = s.groups.length; const key = n === 0 && s.type === 'LEAGUE_PHASE' ? 'T' : String.fromCharCode(65 + n); addGroup.mutate({ stageId: s.id, key, name: s.type === 'LEAGUE_PHASE' ? 'League phase' : `Group ${key}` }) }}>+ group</button>
                      ) : (
                        <AddKoRound onAdd={(key, name, legs, placement) => addKo.mutate({ stageId: s.id, key, name, legs, placement })} />
                      )}
                    </li>
                  </ul>
                </div>
              ))}
            </div>
          </section>

          <section className="sheet p-3">
            <h2 className="mb-2 font-condensed text-base font-semibold">Honours</h2>
            <ul className="mb-2 space-y-1 text-sm">
              {v.honours.map((h) => (
                <li key={h.id} className="flex items-center justify-between">
                  <span><Badge tone={h.kind === 'CHAMPION' ? 'marker' : 'neutral'}>{h.kind.replace('_', ' ').toLowerCase()}</Badge> <span className="ml-1">{v.teams[String(h.teamId)]?.name}</span></span>
                  <button type="button" className="text-xs text-ink-3 hover:text-relegation" onClick={() => deleteHonour.mutate(h.id)} aria-label="Remove honour">✕</button>
                </li>
              ))}
              {v.honours.length === 0 && <li className="text-xs text-ink-3">None yet.</li>}
            </ul>
            <form onSubmit={submitHonour} className="flex gap-1">
              <Select name="teamId" className="h-8 text-[13px]" defaultValue=""><option value="">team…</option>{v.seasonTeams.map((t) => <option key={t.teamId} value={t.teamId}>{v.teams[String(t.teamId)]?.name}</option>)}</Select>
              <Select name="kind" className="h-8 w-32 text-[13px]" defaultValue="CHAMPION"><option value="CHAMPION">Champion</option><option value="RUNNER_UP">Runner-up</option><option value="THIRD">Third</option><option value="FOURTH">Fourth</option><option value="PROMOTED">Promoted</option><option value="RELEGATED">Relegated</option></Select>
              <Button type="submit" size="sm" disabled={addHonour.isPending}>Add</Button>
            </form>
            <ErrorBox error={addHonour.error} className="mt-2" />
          </section>
        </aside>

        <section className="min-w-0">
          {!selected && <p className="sheet px-4 py-10 text-center text-ink-3">Select a group or a knockout round on the left.</p>}
          {selected?.kind === 'group' && <GroupEditor key={selected.group.id} season={v} stage={selected.stage} group={selected.group} />}
          {selected?.kind === 'ko' && <KoRoundEditor key={selected.ko.id} season={v} stage={selected.stage} round={selected.ko} />}
        </section>
      </div>

      <SeasonTeamsDialog key={String(teamsOpen)} open={teamsOpen} onClose={() => setTeamsOpen(false)} season={v} />

      <Dialog open={stageOpen} onClose={() => setStageOpen(false)} title="Add stage">
        <form onSubmit={submitStage} className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Key" hint={formatStageKeys.length ? `Format stages: ${formatStageKeys.join(', ')} — matching a key picks up its config` : undefined}><Input name="key" required defaultValue={formatStageKeys.find((k) => !v.stages.some((s) => s.key === k)) ?? ''} /></Field>
            <Field label="Name"><Input name="name" required /></Field>
            <Field label="Type"><Select name="type" defaultValue="ROUND_ROBIN">{(Object.keys(stageTypeLabel) as StageType[]).map((t) => <option key={t} value={t}>{stageTypeLabel[t]}</option>)}</Select></Field>
            <Field label="Order"><Input name="ordinal" type="number" min={1} defaultValue={v.stages.length + 1} /></Field>
          </div>
          <ErrorBox error={addStage.error} />
          <div className="flex justify-end gap-2"><Button onClick={() => setStageOpen(false)}>Cancel</Button><Button type="submit" variant="primary" disabled={addStage.isPending}>Add stage</Button></div>
        </form>
      </Dialog>

      <Confirm open={!!confirm} onClose={() => setConfirm(null)} title={confirm?.title ?? ''} message={confirm?.message ?? ''} onConfirm={() => { confirm?.run(); setConfirm(null) }} />
    </div>
  )
}

function AddKoRound({ onAdd }: { onAdd: (key: string, name: string, legs: number, placement: boolean) => void }) {
  const [open, setOpen] = useState(false)
  if (!open) return <button type="button" className="text-xs text-biro hover:underline" onClick={() => setOpen(true)}>+ knockout round</button>
  return (
    <form
      className="flex flex-wrap items-center gap-1 py-1"
      onSubmit={(e) => { e.preventDefault(); const f = readForm(e.currentTarget); onAdd(f.key, strOrNull(f.name) ?? f.key, Number(f.legs), f.placement === 'on'); setOpen(false) }}
    >
      <Input name="key" placeholder="QF" required className="h-7 w-14 px-1 text-xs" />
      <Input name="name" placeholder="Quarter-finals" className="h-7 w-32 px-1 text-xs" />
      <Select name="legs" defaultValue="1" className="h-7 w-20 px-1 text-xs"><option value="1">1 leg</option><option value="2">2 legs</option></Select>
      <label className="flex items-center gap-1 text-xs"><input type="checkbox" name="placement" /> 3rd place</label>
      <Button type="submit" size="sm" variant="primary">Add</Button>
      <Button size="sm" variant="ghost" onClick={() => setOpen(false)}>Cancel</Button>
    </form>
  )
}

export type { SeasonView }
