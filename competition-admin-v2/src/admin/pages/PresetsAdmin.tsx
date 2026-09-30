import { useMemo, useState, type FormEvent } from 'react'
import { del, post, put } from '@/api/client'
import { keys, useAction, usePresets } from '@/api/hooks'
import type { PresetDto } from '@/api/types'
import { Badge, Button, Confirm, Dialog, ErrorBox, Field, Input, PageTitle, Spinner, Textarea, cx } from '@/components/ui'
import { readForm, strOrNull } from '@/lib/forms'
import { stageTypeLabel } from '@/lib/format'

/** Catalogue of format presets. Built-in ones are read-only; clone to make a variant. */
export function PresetsAdmin() {
  const q = usePresets()
  const [current, setCurrent] = useState<string | null>(null)
  const [cloneOf, setCloneOf] = useState<PresetDto | null>(null)
  const [editing, setEditing] = useState<PresetDto | 'new' | null>(null)
  const [deleting, setDeleting] = useState<PresetDto | null>(null)
  const [jsonError, setJsonError] = useState<string | null>(null)
  const clone = useAction((a: { key: string; newKey: string; newName: string | null }) => post<PresetDto>(`/api/admin/presets/${a.key}/clone`, { newKey: a.newKey, newName: a.newName }), [keys.presets])
  const save = useAction((a: { key?: string; body: unknown }) => (a.key ? put<PresetDto>(`/api/admin/presets/${a.key}`, a.body) : post<PresetDto>('/api/admin/presets', a.body)), [keys.presets])
  const remove = useAction((key: string) => del(`/api/admin/presets/${key}`), [keys.presets])

  const families = useMemo(() => {
    const map = new Map<string, PresetDto[]>()
    for (const p of q.data ?? []) map.set(p.family ?? 'OTHER', [...(map.get(p.family ?? 'OTHER') ?? []), p])
    return [...map.entries()].sort((a, b) => a[0].localeCompare(b[0]))
  }, [q.data])
  const selected = q.data?.find((p) => p.key === current) ?? q.data?.[0]

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const v = readForm(e.currentTarget)
    setJsonError(null)
    let definition: unknown
    try { definition = JSON.parse(v.definition) } catch (err) { setJsonError((err as Error).message); return }
    save.mutate({ key: editing !== 'new' ? editing?.key : undefined, body: { key: v.key, name: strOrNull(v.name), family: strOrNull(v.family), definition } }, { onSuccess: (p) => { setEditing(null); setCurrent(p.key) } })
  }

  if (q.isLoading) return <Spinner />
  return (
    <div>
      <PageTitle title="Format presets" right={<Button variant="primary" onClick={() => setEditing('new')}>New preset</Button>}>
        A season copies its preset when created, so editing a preset never changes existing seasons.
      </PageTitle>
      <ErrorBox error={q.error ?? remove.error ?? clone.error} className="mb-4" />
      <div className="grid gap-6 lg:grid-cols-[320px_minmax(0,1fr)]">
        <aside className="sheet max-h-[75vh] overflow-y-auto">
          {families.map(([family, list]) => (
            <div key={family}>
              <div className="border-b border-rule-soft bg-paper px-3 py-1 text-xs text-ink-3">{family.replace(/_/g, ' ').toLowerCase()}</div>
              {list.map((p) => (
                <button key={p.key} type="button" onClick={() => setCurrent(p.key)} className={cx('flex w-full items-center justify-between px-3 py-1.5 text-left text-sm hover:bg-paper', selected?.key === p.key && 'bg-biro-soft text-biro')}>
                  <span className="truncate">{p.name}</span>
                  {!p.builtin && <Badge tone="amber">custom</Badge>}
                </button>
              ))}
            </div>
          ))}
        </aside>
        {selected && (
          <section>
            <div className="mb-3 flex flex-wrap items-start justify-between gap-3">
              <div>
                <h2 className="font-condensed text-2xl font-semibold">{selected.name}</h2>
                <div className="font-mono text-xs text-ink-3">{selected.key}{selected.definition.teams ? ` · ${selected.definition.teams} teams` : ''} · {selected.definition.points?.win ?? 3}-{selected.definition.points?.draw ?? 1}-{selected.definition.points?.loss ?? 0} points</div>
              </div>
              <div className="flex gap-2">
                <Button onClick={() => setCloneOf(selected)}>Clone</Button>
                {!selected.builtin && <Button onClick={() => setEditing(selected)}>Edit</Button>}
                {!selected.builtin && <Button variant="danger" onClick={() => setDeleting(selected)}>Delete</Button>}
              </div>
            </div>
            <ol className="mb-4 space-y-2">
              {selected.definition.stages.map((s) => (
                <li key={s.key} className="sheet px-4 py-2.5 text-sm">
                  <div className="flex flex-wrap items-baseline gap-x-3">
                    <span className="font-condensed text-base font-semibold">{s.name ?? s.key}</span>
                    <Badge tone="blue">{stageTypeLabel[s.type]}</Badge>
                    <span className="text-ink-2">
                      {s.type === 'ROUND_ROBIN' && `${s.groups ?? 1} group${(s.groups ?? 1) > 1 ? 's' : ''}${s.teamsPerGroup ? ` of ${s.teamsPerGroup}` : ''}, ${s.meetings === 2 ? 'home and away' : 'single round'}`}
                      {s.type === 'LEAGUE_PHASE' && `${s.matchesPerTeam} matches per team, ${s.pots} pots`}
                      {(s.type === 'KNOCKOUT' || s.type === 'SINGLE_MATCH') && `${(s.rounds ?? []).map((r) => r.name ?? r.key).join(' → ')}, ${s.legs === 2 ? 'two legs' : 'one match'}`}
                    </span>
                  </div>
                  <div className="mt-1 text-xs text-ink-3">
                    {s.tiebreakers && <span>Tie-breaks: {s.tiebreakers.join(', ')}. </span>}
                    {s.decider && <span>Decided by: {s.decider.join(', ')}. </span>}
                    {s.entry && <span>Entry: {JSON.stringify(s.entry)}. </span>}
                    {s.zones && <span>Zones: {s.zones.map((z) => `${z.label ?? z.kind} (${z.positions.join(',')})`).join('; ')}.</span>}
                  </div>
                </li>
              ))}
            </ol>
            <details>
              <summary className="cursor-pointer text-sm text-ink-2">Definition JSON</summary>
              <pre className="sheet mt-2 max-h-[50vh] overflow-auto p-3 text-xs">{JSON.stringify(selected.definition, null, 2)}</pre>
            </details>
          </section>
        )}
      </div>

      <Dialog open={!!cloneOf} onClose={() => setCloneOf(null)} title={`Clone ${cloneOf?.name ?? ''}`}>
        {cloneOf && (
          <form className="space-y-4" onSubmit={(e) => { e.preventDefault(); const f = readForm(e.currentTarget); clone.mutate({ key: cloneOf.key, newKey: f.newKey.toUpperCase(), newName: strOrNull(f.newName) }, { onSuccess: (p) => { setCloneOf(null); setCurrent(p.key) } }) }}>
            <Field label="New key" hint="Upper-case, underscores"><Input name="newKey" required pattern="[A-Za-z0-9_]+" defaultValue={`${cloneOf.key}_COPY`} className="font-mono" /></Field>
            <Field label="New name"><Input name="newName" defaultValue={`${cloneOf.name} (copy)`} /></Field>
            <ErrorBox error={clone.error} />
            <div className="flex justify-end gap-2"><Button onClick={() => setCloneOf(null)}>Cancel</Button><Button type="submit" variant="primary" disabled={clone.isPending}>Clone</Button></div>
          </form>
        )}
      </Dialog>

      <Dialog open={editing !== null} onClose={() => setEditing(null)} title={editing === 'new' ? 'New preset' : `Edit ${editing?.name ?? ''}`} width="xl">
        {editing !== null && (
          <form onSubmit={submit} className="space-y-4" key={editing === 'new' ? 'new' : editing.key}>
            <div className="grid gap-4 sm:grid-cols-3">
              <Field label="Key"><Input name="key" required pattern="[A-Za-z0-9_]+" defaultValue={editing === 'new' ? '' : editing.key} readOnly={editing !== 'new'} className="font-mono" /></Field>
              <Field label="Name"><Input name="name" defaultValue={editing === 'new' ? '' : editing.name} /></Field>
              <Field label="Family"><Input name="family" defaultValue={editing === 'new' ? '' : editing.family ?? ''} placeholder="GROUPS_TOP_TWO" /></Field>
            </div>
            <Field label="Definition JSON" hint="Validated on save: stage keys unique, entry.from points at an earlier stage, tie-breakers and deciders from the known lists">
              <Textarea name="definition" rows={22} className="font-mono text-xs" defaultValue={editing === 'new' ? JSON.stringify({ key: 'MY_FORMAT', name: '', family: 'SINGLE_LEAGUE', points: { win: 3, draw: 1, loss: 0 }, stages: [{ key: 'REG', name: 'League', type: 'ROUND_ROBIN', groups: 1, meetings: 2, tiebreakers: ['GD', 'GF', 'H2H'], zones: [{ positions: [1], kind: 'CHAMPION', label: 'Champion' }] }] }, null, 2) : JSON.stringify(editing.definition, null, 2)} />
            </Field>
            {jsonError && <p className="text-sm text-relegation">Invalid JSON: {jsonError}</p>}
            <ErrorBox error={save.error} />
            <div className="flex justify-end gap-2"><Button onClick={() => setEditing(null)}>Cancel</Button><Button type="submit" variant="primary" disabled={save.isPending}>Save</Button></div>
          </form>
        )}
      </Dialog>
      <Confirm open={!!deleting} onClose={() => setDeleting(null)} title="Delete preset" message={`Delete ${deleting?.name}? Seasons created from it keep their own copy.`} busy={remove.isPending} onConfirm={() => deleting && remove.mutate(deleting.key, { onSuccess: () => { setDeleting(null); setCurrent(null) } })} />
    </div>
  )
}
