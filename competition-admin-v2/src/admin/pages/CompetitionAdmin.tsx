import { useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router'
import { del, post, put } from '@/api/client'
import { keys, useAction, useCompetition, usePresets } from '@/api/hooks'
import type { SeasonSummary } from '@/api/types'
import { Badge, Button, Confirm, Dialog, ErrorBox, Field, Input, PageTitle, Select, Spinner, Textarea } from '@/components/ui'
import { numOrNull, parseJsonOrNull, readForm, strOrNull } from '@/lib/forms'
import { statusLabel } from '@/lib/format'

export function CompetitionAdmin() {
  const id = Number(useParams().id)
  const q = useCompetition(id)
  const presets = usePresets()
  const [editing, setEditing] = useState<SeasonSummary | 'new' | null>(null)
  const [deleting, setDeleting] = useState<SeasonSummary | null>(null)
  const [formatMode, setFormatMode] = useState<'preset' | 'json'>('preset')
  const [jsonError, setJsonError] = useState<string | null>(null)
  const inval = [keys.competition(id), keys.seasons(id)]
  const save = useAction((a: { id?: number; body: unknown }) => (a.id ? put<SeasonSummary>(`/api/admin/seasons/${a.id}`, a.body) : post<SeasonSummary>(`/api/admin/competitions/${id}/seasons`, a.body)), inval)
  const remove = useAction((sid: number) => del(`/api/admin/seasons/${sid}`), inval)

  if (q.isLoading) return <Spinner />
  if (q.error) return <ErrorBox error={q.error} />
  const { competition: c, seasons } = q.data!

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const v = readForm(e.currentTarget)
    setJsonError(null)
    let format: unknown = null
    if (formatMode === 'json') {
      try { format = parseJsonOrNull(v.format) } catch (err) { setJsonError((err as Error).message); return }
    }
    const body = {
      key: v.key, name: v.name, year: numOrNull(v.year), startDate: strOrNull(v.startDate), endDate: strOrNull(v.endDate), status: v.status,
      presetKey: formatMode === 'preset' ? strOrNull(v.presetKey) : null, format, notes: strOrNull(v.notes), scaffold: v.scaffold === 'on',
    }
    save.mutate({ id: editing !== 'new' ? editing?.id : undefined, body }, { onSuccess: () => setEditing(null) })
  }

  const open = (s: SeasonSummary | 'new') => { setFormatMode('preset'); setJsonError(null); setEditing(s) }

  return (
    <div>
      <PageTitle
        eyebrow={<span><Link to="/admin" className="hover:underline">Universes</Link>{c.universeKey && <> <span className="text-ink-3">/</span> <Link to={`/admin/universes/${c.universeKey}?tab=competitions`} className="hover:underline">{c.universeName}</Link></>}</span>}
        title={c.name}
        right={<><Link to={`/competitions/${c.id}`} className="text-sm text-biro hover:underline">View</Link><Button variant="primary" onClick={() => open('new')}>New season</Button></>}
      >
        {c.teamLevel === 'NATIONAL' ? 'National teams' : 'Clubs'}{c.tier ? ` · tier ${c.tier}` : ''} <span className="ml-2 font-mono text-xs text-ink-3">{c.key}</span>
      </PageTitle>

      <table className="ruled sheet w-full text-sm">
        <thead className="font-condensed text-ink-2"><tr><th className="px-3 py-2 text-left">Season</th><th className="px-3 py-2 text-left">Format</th><th className="px-3 py-2 text-right">Teams</th><th className="px-3 py-2 text-left">Champion</th><th className="px-3 py-2 text-left">Status</th><th /></tr></thead>
        <tbody>
          {seasons.map((s) => (
            <tr key={s.id}>
              <td className="px-3 py-1.5"><Link to={`/admin/seasons/${s.id}`} className="font-medium hover:underline">{s.name}</Link>{s.year && <span className="tnum ml-2 text-ink-3">{s.year}</span>}</td>
              <td className="px-3 py-1.5 font-mono text-xs text-ink-2">{s.presetKey ?? 'custom'}</td>
              <td className="tnum px-3 py-1.5 text-right">{s.teamCount}</td>
              <td className="px-3 py-1.5">{s.championName ?? <span className="text-ink-3">—</span>}</td>
              <td className="px-3 py-1.5"><Badge tone={s.status === 'COMPLETED' ? 'neutral' : s.status === 'ONGOING' ? 'green' : 'amber'}>{statusLabel[s.status]}</Badge></td>
              <td className="px-3 py-1.5 text-right whitespace-nowrap">
                <Link to={`/admin/seasons/${s.id}`}><Button size="sm" variant="ghost">Open</Button></Link>
                <Button size="sm" variant="ghost" onClick={() => open(s)}>Edit</Button>
                <Button size="sm" variant="ghost" onClick={() => setDeleting(s)}>Delete</Button>
              </td>
            </tr>
          ))}
          {seasons.length === 0 && <tr><td colSpan={6} className="px-3 py-8 text-center text-ink-3">No seasons yet. Create one from a preset, or import a JSON file.</td></tr>}
        </tbody>
      </table>

      <Dialog open={editing !== null} onClose={() => setEditing(null)} title={editing === 'new' ? 'New season' : 'Edit season'} width="lg">
        {editing !== null && (
          <form onSubmit={submit} className="space-y-4" key={editing === 'new' ? 'new' : editing.id}>
            <div className="grid gap-4 sm:grid-cols-3">
              <Field label="Name" className="sm:col-span-2"><Input name="name" required defaultValue={editing === 'new' ? '' : editing.name} placeholder="Season 7 or 2022" onChange={(e) => { if (editing === 'new') (e.currentTarget.form!.elements.namedItem('key') as HTMLInputElement).value = e.currentTarget.value.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '') }} /></Field>
              <Field label="Key"><Input name="key" required pattern="[a-z0-9][a-z0-9-]*" defaultValue={editing === 'new' ? '' : editing.key} readOnly={editing !== 'new'} /></Field>
              <Field label="Year"><Input name="year" type="number" defaultValue={editing === 'new' ? '' : editing.year ?? ''} /></Field>
              <Field label="Start"><Input name="startDate" type="date" defaultValue={editing === 'new' ? '' : editing.startDate ?? ''} /></Field>
              <Field label="End"><Input name="endDate" type="date" defaultValue={editing === 'new' ? '' : editing.endDate ?? ''} /></Field>
              <Field label="Status"><Select name="status" defaultValue={editing === 'new' ? 'COMPLETED' : editing.status}><option value="PLANNED">Planned</option><option value="ONGOING">In progress</option><option value="COMPLETED">Completed</option></Select></Field>
            </div>
            <fieldset className="rounded-sm border border-rule p-3">
              <legend className="px-1 text-sm text-ink-2">Format</legend>
              <div className="mb-3 flex gap-4 text-sm">
                <label className="flex items-center gap-1.5"><input type="radio" name="_mode" checked={formatMode === 'preset'} onChange={() => setFormatMode('preset')} /> From a preset</label>
                <label className="flex items-center gap-1.5"><input type="radio" name="_mode" checked={formatMode === 'json'} onChange={() => setFormatMode('json')} /> Custom definition (JSON)</label>
              </div>
              {formatMode === 'preset' ? (
                <Field label="Preset" hint={editing !== 'new' ? 'Changing the preset rewrites the stored format; existing stages keep their own config.' : undefined}>
                  <Select name="presetKey" defaultValue={editing === 'new' ? '' : editing.presetKey ?? ''} required={editing === 'new'}>
                    <option value="">{editing === 'new' ? 'Choose…' : 'Keep current'}</option>
                    {groupPresets(presets.data ?? []).map(([family, list]) => (
                      <optgroup key={family} label={family}>{list.map((p) => <option key={p.key} value={p.key}>{p.name}</option>)}</optgroup>
                    ))}
                  </Select>
                </Field>
              ) : (
                <Field label="Format definition" hint="Same shape as a preset file; see Format presets for examples">
                  <Textarea name="format" rows={10} className="font-mono text-xs" placeholder='{"key":"MY_FORMAT","stages":[...]}' />
                </Field>
              )}
              {jsonError && <p className="mt-2 text-sm text-relegation">Invalid JSON: {jsonError}</p>}
              {editing === 'new' && <label className="mt-3 flex items-center gap-2 text-sm"><input type="checkbox" name="scaffold" defaultChecked /> Create stages, groups and knockout rounds from the format</label>}
            </fieldset>
            <Field label="Notes"><Textarea name="notes" rows={2} defaultValue={editing === 'new' ? '' : ''} /></Field>
            <ErrorBox error={save.error} />
            <div className="flex justify-end gap-2"><Button onClick={() => setEditing(null)}>Cancel</Button><Button type="submit" variant="primary" disabled={save.isPending}>Save</Button></div>
          </form>
        )}
      </Dialog>
      <Confirm open={!!deleting} onClose={() => setDeleting(null)} title="Delete season" message={`Delete ${deleting?.name} with all its matches and tables?`} busy={remove.isPending} onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })} />
    </div>
  )
}

function groupPresets<T extends { family: string | null }>(list: T[]): Array<[string, T[]]> {
  const map = new Map<string, T[]>()
  for (const p of list) map.set(p.family ?? 'OTHER', [...(map.get(p.family ?? 'OTHER') ?? []), p])
  return [...map.entries()].sort((a, b) => a[0].localeCompare(b[0]))
}
