import { useState, type FormEvent, type ReactNode } from 'react'
import { Link, useParams, useSearchParams } from 'react-router'
import { del, post, put } from '@/api/client'
import { keys, useAction, useCompetitions, useNations, useStadiums, useTeams, useUniverse } from '@/api/hooks'
import type { CompetitionDto, NationDto, StadiumDto, TeamDto } from '@/api/types'
import { Badge, Button, Confirm, Dialog, ErrorBox, Field, Input, PageTitle, Select, Spinner, Tabs, Textarea } from '@/components/ui'
import { listOrEmpty, numOrNull, readForm, strOrNull } from '@/lib/forms'
import { slugify } from '@/lib/format'
import { ColorsInput, colorsFromForm } from '../components/ColorsInput'
import { SlotImage } from '../components/SlotImage'

type Tab = 'nations' | 'stadiums' | 'teams' | 'competitions'

export function UniverseAdmin() {
  const { key = '' } = useParams()
  const u = useUniverse(key)
  const [params, setParams] = useSearchParams()
  const tab = (params.get('tab') as Tab) || 'teams'
  const setTab = (t: Tab) => setParams((p) => { p.set('tab', t); return p })
  if (u.isLoading) return <Spinner />
  if (u.error) return <ErrorBox error={u.error} />
  const universe = u.data!
  return (
    <div>
      <PageTitle eyebrow={<Link to="/admin" className="hover:underline">Universes</Link>} title={universe.name} right={<Link to={`/u/${universe.key}`} className="text-sm text-biro hover:underline">View</Link>} />
      <Tabs
        value={tab}
        onChange={setTab}
        className="mb-6"
        items={[
          { value: 'teams', label: 'Teams' },
          ...(universe.usesNations ? [{ value: 'nations' as Tab, label: 'Nations' }] : []),
          { value: 'stadiums', label: 'Stadiums' },
          { value: 'competitions', label: 'Competitions' },
        ]}
      />
      {tab === 'nations' && <NationsTab universeKey={key} />}
      {tab === 'stadiums' && <StadiumsTab universeKey={key} />}
      {tab === 'teams' && <TeamsTab universeKey={key} usesNations={universe.usesNations} />}
      {tab === 'competitions' && <CompetitionsTab universeKey={key} />}
    </div>
  )
}

function Toolbar({ children, onNew, newLabel }: { children?: ReactNode; onNew: () => void; newLabel: string }) {
  return (
    <div className="mb-3 flex flex-wrap items-center justify-between gap-3">
      <div className="flex items-center gap-2">{children}</div>
      <Button variant="primary" onClick={onNew}>{newLabel}</Button>
    </div>
  )
}

function RowActions({ onEdit, onDelete }: { onEdit: () => void; onDelete: () => void }) {
  return (
    <td className="px-3 py-1.5 text-right whitespace-nowrap">
      <Button size="sm" variant="ghost" onClick={onEdit}>Edit</Button>
      <Button size="sm" variant="ghost" onClick={onDelete}>Delete</Button>
    </td>
  )
}

// ---------------- nations ----------------

function NationsTab({ universeKey }: { universeKey: string }) {
  const q = useNations(universeKey)
  const [editing, setEditing] = useState<NationDto | 'new' | null>(null)
  const [deleting, setDeleting] = useState<NationDto | null>(null)
  const inval = [keys.nations(universeKey), keys.universe(universeKey), keys.teams(universeKey)]
  const save = useAction((a: { id?: number; body: unknown }) => (a.id ? put(`/api/admin/nations/${a.id}`, a.body) : post(`/api/admin/universes/${universeKey}/nations`, a.body)), inval)
  const remove = useAction((id: number) => del(`/api/admin/nations/${id}`), inval)

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const v = readForm(e.currentTarget)
    const body = { key: v.key, name: v.name, code: v.code, description: strOrNull(v.description), colors: colorsFromForm(v) }
    save.mutate({ id: editing !== 'new' ? editing?.id : undefined, body }, { onSuccess: () => setEditing(null) })
  }

  return (
    <div>
      <Toolbar onNew={() => setEditing('new')} newLabel="New nation"><span className="tnum text-sm text-ink-2">{q.data?.length ?? 0} nations</span></Toolbar>
      {q.isLoading && <Spinner />}
      <table className="ruled sheet w-full text-sm">
        <thead className="font-condensed text-ink-2"><tr><th className="px-3 py-2 text-left">Nation</th><th className="px-3 py-2 text-left">Code</th><th className="px-3 py-2 text-left">Key</th><th className="px-3 py-2 text-left">Colours</th><th /></tr></thead>
        <tbody>
          {q.data?.map((n) => (
            <tr key={n.id}>
              <td className="px-3 py-1.5"><span className="inline-flex items-center gap-2">{n.flagUrl && <img src={n.flagUrl} alt="" className="h-4 w-6 object-cover" />}<Link to={`/nations/${n.id}`} className="hover:underline">{n.name}</Link></span></td>
              <td className="px-3 py-1.5">{n.code}</td>
              <td className="px-3 py-1.5 font-mono text-xs text-ink-2">{n.key}</td>
              <td className="px-3 py-1.5"><span className="inline-flex gap-1">{[n.colors?.primary, n.colors?.secondary].filter(Boolean).map((c, i) => <span key={i} className="h-4 w-4 rounded-[2px] border border-rule" style={{ background: c as string }} />)}</span></td>
              <RowActions onEdit={() => setEditing(n)} onDelete={() => setDeleting(n)} />
            </tr>
          ))}
          {q.data?.length === 0 && <tr><td colSpan={5} className="px-3 py-6 text-center text-ink-3">No nations yet.</td></tr>}
        </tbody>
      </table>
      <Dialog open={editing !== null} onClose={() => setEditing(null)} title={editing === 'new' ? 'New nation' : 'Edit nation'} width="lg">
        {editing !== null && (
          <form onSubmit={submit} className="space-y-4" key={editing === 'new' ? 'new' : editing.id}>
            <div className="grid gap-4 sm:grid-cols-3">
              <Field label="Name"><Input name="name" required defaultValue={editing === 'new' ? '' : editing.name} onChange={(e) => { if (editing === 'new') (e.currentTarget.form!.elements.namedItem('key') as HTMLInputElement).value = slugify(e.currentTarget.value) }} /></Field>
              <Field label="Code" hint="2–3 letters"><Input name="code" required maxLength={3} defaultValue={editing === 'new' ? '' : editing.code} className="uppercase" /></Field>
              <Field label="Key"><Input name="key" required pattern="[a-z0-9][a-z0-9-]*" defaultValue={editing === 'new' ? '' : editing.key} readOnly={editing !== 'new'} /></Field>
            </div>
            <ColorsInput kind="nation" value={editing === 'new' ? null : editing.colors} />
            <Field label="Description"><Textarea name="description" rows={2} defaultValue={editing === 'new' ? '' : editing.description ?? ''} /></Field>
            {editing !== 'new' && (
              <div className="grid gap-4 sm:grid-cols-2">
                <SlotImage ownerType="NATION" ownerId={editing.id} slot="FLAG" label="Flag" collection="flags" shape="wide" onChanged={() => save.reset()} />
                <SlotImage ownerType="NATION" ownerId={editing.id} slot="EMBLEM" label="Emblem" collection="emblems" />
              </div>
            )}
            <ErrorBox error={save.error} />
            <div className="flex justify-end gap-2"><Button onClick={() => setEditing(null)}>Cancel</Button><Button type="submit" variant="primary" disabled={save.isPending}>Save</Button></div>
          </form>
        )}
      </Dialog>
      <Confirm open={!!deleting} onClose={() => setDeleting(null)} title="Delete nation" message={`Delete ${deleting?.name}? Teams keep existing but lose their nation.`} busy={remove.isPending} onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })} />
    </div>
  )
}

// ---------------- stadiums ----------------

function StadiumsTab({ universeKey }: { universeKey: string }) {
  const q = useStadiums(universeKey)
  const [editing, setEditing] = useState<StadiumDto | 'new' | null>(null)
  const [deleting, setDeleting] = useState<StadiumDto | null>(null)
  const inval = [keys.stadiums(universeKey)]
  const save = useAction((a: { id?: number; body: unknown }) => (a.id ? put(`/api/admin/stadiums/${a.id}`, a.body) : post(`/api/admin/universes/${universeKey}/stadiums`, a.body)), inval)
  const remove = useAction((id: number) => del(`/api/admin/stadiums/${id}`), inval)

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const v = readForm(e.currentTarget)
    const body = { key: v.key, name: v.name, city: strOrNull(v.city), capacity: numOrNull(v.capacity), inspiredBy: strOrNull(v.inspiredBy), description: strOrNull(v.description) }
    save.mutate({ id: editing !== 'new' ? editing?.id : undefined, body }, { onSuccess: () => setEditing(null) })
  }

  return (
    <div>
      <Toolbar onNew={() => setEditing('new')} newLabel="New stadium"><span className="tnum text-sm text-ink-2">{q.data?.length ?? 0} stadiums</span></Toolbar>
      {q.isLoading && <Spinner />}
      <table className="ruled sheet w-full text-sm">
        <thead className="font-condensed text-ink-2"><tr><th className="px-3 py-2 text-left">Stadium</th><th className="px-3 py-2 text-left">City</th><th className="px-3 py-2 text-right">Capacity</th><th className="px-3 py-2 text-left">Inspired by</th><th /></tr></thead>
        <tbody>
          {q.data?.map((s) => (
            <tr key={s.id}>
              <td className="px-3 py-1.5">{s.name}</td>
              <td className="px-3 py-1.5 text-ink-2">{s.city}</td>
              <td className="tnum px-3 py-1.5 text-right">{s.capacity?.toLocaleString()}</td>
              <td className="px-3 py-1.5 text-ink-2">{s.inspiredBy}</td>
              <RowActions onEdit={() => setEditing(s)} onDelete={() => setDeleting(s)} />
            </tr>
          ))}
          {q.data?.length === 0 && <tr><td colSpan={5} className="px-3 py-6 text-center text-ink-3">No stadiums yet.</td></tr>}
        </tbody>
      </table>
      <Dialog open={editing !== null} onClose={() => setEditing(null)} title={editing === 'new' ? 'New stadium' : 'Edit stadium'}>
        {editing !== null && (
          <form onSubmit={submit} className="space-y-4" key={editing === 'new' ? 'new' : editing.id}>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Name"><Input name="name" required defaultValue={editing === 'new' ? '' : editing.name} onChange={(e) => { if (editing === 'new') (e.currentTarget.form!.elements.namedItem('key') as HTMLInputElement).value = slugify(e.currentTarget.value) }} /></Field>
              <Field label="Key"><Input name="key" required pattern="[a-z0-9][a-z0-9-]*" defaultValue={editing === 'new' ? '' : editing.key} readOnly={editing !== 'new'} /></Field>
              <Field label="City"><Input name="city" defaultValue={editing === 'new' ? '' : editing.city ?? ''} /></Field>
              <Field label="Capacity"><Input name="capacity" type="number" min={0} defaultValue={editing === 'new' ? '' : editing.capacity ?? ''} /></Field>
              <Field label="Inspired by" hint="Real-world stadium, if any"><Input name="inspiredBy" defaultValue={editing === 'new' ? '' : editing.inspiredBy ?? ''} /></Field>
            </div>
            <Field label="Description"><Textarea name="description" rows={2} defaultValue={editing === 'new' ? '' : editing.description ?? ''} /></Field>
            {editing !== 'new' && <SlotImage ownerType="STADIUM" ownerId={editing.id} slot="IMAGE" label="Photo" collection="stadiums" shape="wide" />}
            <ErrorBox error={save.error} />
            <div className="flex justify-end gap-2"><Button onClick={() => setEditing(null)}>Cancel</Button><Button type="submit" variant="primary" disabled={save.isPending}>Save</Button></div>
          </form>
        )}
      </Dialog>
      <Confirm open={!!deleting} onClose={() => setDeleting(null)} title="Delete stadium" message={`Delete ${deleting?.name}?`} busy={remove.isPending} onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })} />
    </div>
  )
}

// ---------------- teams ----------------

function TeamsTab({ universeKey, usesNations }: { universeKey: string; usesNations: boolean }) {
  const q = useTeams(universeKey)
  const nations = useNations(universeKey, usesNations)
  const stadiums = useStadiums(universeKey)
  const [filter, setFilter] = useState('')
  const [editing, setEditing] = useState<TeamDto | 'new' | null>(null)
  const [deleting, setDeleting] = useState<TeamDto | null>(null)
  const inval = [keys.teams(universeKey), keys.universe(universeKey)]
  const save = useAction((a: { id?: number; body: unknown }) => (a.id ? put<TeamDto>(`/api/admin/teams/${a.id}`, a.body) : post<TeamDto>(`/api/admin/universes/${universeKey}/teams`, a.body)), inval)
  const remove = useAction((id: number) => del(`/api/admin/teams/${id}`), inval)

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const v = readForm(e.currentTarget)
    const body = {
      key: v.key, name: v.name, shortName: strOrNull(v.shortName), code: strOrNull(v.code), type: v.type,
      nationId: numOrNull(v.nationId), homeStadiumId: numOrNull(v.homeStadiumId), description: strOrNull(v.description),
      foundedYear: numOrNull(v.foundedYear), dissolvedYear: numOrNull(v.dissolvedYear), aliases: listOrEmpty(v.aliases),
    }
    save.mutate({ id: editing !== 'new' ? editing?.id : undefined, body }, { onSuccess: () => setEditing(null) })
  }

  const list = (q.data ?? []).filter((t) => !filter || t.name.toLowerCase().includes(filter.toLowerCase()) || (t.nationName ?? '').toLowerCase().includes(filter.toLowerCase()))

  return (
    <div>
      <Toolbar onNew={() => setEditing('new')} newLabel="New team">
        <Input placeholder="Filter by name or nation" value={filter} onChange={(e) => setFilter(e.target.value)} className="w-64" />
        <span className="tnum text-sm text-ink-2">{list.length} / {q.data?.length ?? 0}</span>
      </Toolbar>
      {q.isLoading && <Spinner />}
      <table className="ruled sheet w-full text-sm">
        <thead className="font-condensed text-ink-2"><tr><th className="px-3 py-2 text-left">Team</th><th className="px-3 py-2 text-left">Type</th>{usesNations && <th className="px-3 py-2 text-left">Nation</th>}<th className="px-3 py-2 text-left">Code</th><th className="px-3 py-2 text-left">Aliases</th><th /></tr></thead>
        <tbody>
          {list.map((t) => (
            <tr key={t.id}>
              <td className="px-3 py-1.5"><Link to={`/admin/teams/${t.id}`} className="inline-flex items-center gap-2 font-medium hover:underline">{t.logoUrl && <img src={t.logoUrl} alt="" className="h-4 w-4 object-contain" />}{t.name}</Link></td>
              <td className="px-3 py-1.5"><Badge tone={t.type === 'NATIONAL' ? 'blue' : 'neutral'}>{t.type === 'NATIONAL' ? 'National' : 'Club'}</Badge></td>
              {usesNations && <td className="px-3 py-1.5 text-ink-2">{t.nationName ?? <span className="text-relegation">none</span>}</td>}
              <td className="px-3 py-1.5 text-ink-2">{t.code}</td>
              <td className="px-3 py-1.5 text-xs text-ink-3">{t.aliases.join(', ')}</td>
              <RowActions onEdit={() => setEditing(t)} onDelete={() => setDeleting(t)} />
            </tr>
          ))}
          {list.length === 0 && <tr><td colSpan={6} className="px-3 py-6 text-center text-ink-3">No teams match.</td></tr>}
        </tbody>
      </table>
      <Dialog open={editing !== null} onClose={() => setEditing(null)} title={editing === 'new' ? 'New team' : 'Edit team'} width="lg">
        {editing !== null && (
          <form onSubmit={submit} className="space-y-4" key={editing === 'new' ? 'new' : editing.id}>
            <div className="grid gap-4 sm:grid-cols-3">
              <Field label="Name" className="sm:col-span-2"><Input name="name" required defaultValue={editing === 'new' ? '' : editing.name} onChange={(e) => { if (editing === 'new') (e.currentTarget.form!.elements.namedItem('key') as HTMLInputElement).value = slugify(e.currentTarget.value) }} /></Field>
              <Field label="Key"><Input name="key" required pattern="[a-z0-9][a-z0-9-]*" defaultValue={editing === 'new' ? '' : editing.key} readOnly={editing !== 'new'} /></Field>
              <Field label="Short name"><Input name="shortName" defaultValue={editing === 'new' ? '' : editing.shortName ?? ''} /></Field>
              <Field label="Code" hint="3–4 letters"><Input name="code" maxLength={4} defaultValue={editing === 'new' ? '' : editing.code ?? ''} className="uppercase" /></Field>
              <Field label="Type"><Select name="type" defaultValue={editing === 'new' ? 'CLUB' : editing.type}><option value="CLUB">Club</option><option value="NATIONAL">National team</option></Select></Field>
              {usesNations && (
                <Field label="Nation">
                  <Select name="nationId" defaultValue={editing === 'new' ? '' : editing.nationId ?? ''}>
                    <option value="">—</option>
                    {nations.data?.map((n) => <option key={n.id} value={n.id}>{n.name}</option>)}
                  </Select>
                </Field>
              )}
              <Field label="Home stadium">
                <Select name="homeStadiumId" defaultValue={editing === 'new' ? '' : editing.homeStadiumId ?? ''}>
                  <option value="">—</option>
                  {stadiums.data?.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                </Select>
              </Field>
              <Field label="Founded"><Input name="foundedYear" type="number" defaultValue={editing === 'new' ? '' : editing.foundedYear ?? ''} /></Field>
              <Field label="Dissolved"><Input name="dissolvedYear" type="number" defaultValue={editing === 'new' ? '' : editing.dissolvedYear ?? ''} /></Field>
            </div>
            <Field label="Aliases" hint="Other spellings from the notebooks, comma-separated; imports resolve them"><Input name="aliases" defaultValue={editing === 'new' ? '' : editing.aliases.join(', ')} /></Field>
            <Field label="Description"><Textarea name="description" rows={2} defaultValue={editing === 'new' ? '' : editing.description ?? ''} /></Field>
            <ErrorBox error={save.error} />
            <div className="flex items-center justify-between gap-2">
              {editing !== 'new' ? <Link to={`/admin/teams/${editing.id}`} className="text-sm text-biro hover:underline">Profiles, kits and logo</Link> : <span />}
              <div className="flex gap-2"><Button onClick={() => setEditing(null)}>Cancel</Button><Button type="submit" variant="primary" disabled={save.isPending}>Save</Button></div>
            </div>
          </form>
        )}
      </Dialog>
      <Confirm open={!!deleting} onClose={() => setDeleting(null)} title="Delete team" message={`Delete ${deleting?.name}? Its matches and table rows are removed too.`} busy={remove.isPending} onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })} />
    </div>
  )
}

// ---------------- competitions ----------------

function CompetitionsTab({ universeKey }: { universeKey: string }) {
  const q = useCompetitions(universeKey)
  const [editing, setEditing] = useState<CompetitionDto | 'new' | null>(null)
  const [deleting, setDeleting] = useState<CompetitionDto | null>(null)
  const inval = [keys.competitions(universeKey), keys.universe(universeKey)]
  const save = useAction((a: { id?: number; body: unknown }) => (a.id ? put(`/api/admin/competitions/${a.id}`, a.body) : post(`/api/admin/universes/${universeKey}/competitions`, a.body)), inval)
  const remove = useAction((id: number) => del(`/api/admin/competitions/${id}`), inval)

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const v = readForm(e.currentTarget)
    const body = { key: v.key, name: v.name, sport: 'FOOTBALL', teamLevel: v.teamLevel, tier: numOrNull(v.tier), description: strOrNull(v.description) }
    save.mutate({ id: editing !== 'new' ? editing?.id : undefined, body }, { onSuccess: () => setEditing(null) })
  }

  return (
    <div>
      <Toolbar onNew={() => setEditing('new')} newLabel="New competition"><span className="tnum text-sm text-ink-2">{q.data?.length ?? 0} competitions</span></Toolbar>
      {q.isLoading && <Spinner />}
      <table className="ruled sheet w-full text-sm">
        <thead className="font-condensed text-ink-2"><tr><th className="px-3 py-2 text-left">Competition</th><th className="px-3 py-2 text-left">Level</th><th className="px-3 py-2 text-right">Tier</th><th className="px-3 py-2 text-right">Seasons</th><th /></tr></thead>
        <tbody>
          {q.data?.map((c) => (
            <tr key={c.id}>
              <td className="px-3 py-1.5"><Link to={`/admin/competitions/${c.id}`} className="font-medium hover:underline">{c.name}</Link> <span className="ml-2 font-mono text-xs text-ink-3">{c.key}</span></td>
              <td className="px-3 py-1.5">{c.teamLevel === 'NATIONAL' ? 'National teams' : 'Clubs'}</td>
              <td className="tnum px-3 py-1.5 text-right">{c.tier ?? ''}</td>
              <td className="tnum px-3 py-1.5 text-right">{c.seasonCount}</td>
              <RowActions onEdit={() => setEditing(c)} onDelete={() => setDeleting(c)} />
            </tr>
          ))}
          {q.data?.length === 0 && <tr><td colSpan={5} className="px-3 py-6 text-center text-ink-3">No competitions yet.</td></tr>}
        </tbody>
      </table>
      <Dialog open={editing !== null} onClose={() => setEditing(null)} title={editing === 'new' ? 'New competition' : 'Edit competition'}>
        {editing !== null && (
          <form onSubmit={submit} className="space-y-4" key={editing === 'new' ? 'new' : editing.id}>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Name"><Input name="name" required defaultValue={editing === 'new' ? '' : editing.name} onChange={(e) => { if (editing === 'new') (e.currentTarget.form!.elements.namedItem('key') as HTMLInputElement).value = slugify(e.currentTarget.value) }} /></Field>
              <Field label="Key"><Input name="key" required pattern="[a-z0-9][a-z0-9-]*" defaultValue={editing === 'new' ? '' : editing.key} readOnly={editing !== 'new'} /></Field>
              <Field label="Team level"><Select name="teamLevel" defaultValue={editing === 'new' ? 'CLUB' : editing.teamLevel}><option value="CLUB">Clubs</option><option value="NATIONAL">National teams</option></Select></Field>
              <Field label="Tier" hint="1 = top division"><Input name="tier" type="number" min={1} defaultValue={editing === 'new' ? '' : editing.tier ?? ''} /></Field>
            </div>
            <Field label="Description"><Textarea name="description" rows={2} defaultValue={editing === 'new' ? '' : editing.description ?? ''} /></Field>
            {editing !== 'new' && <SlotImage ownerType="COMPETITION" ownerId={editing.id} slot="LOGO" label="Logo" collection="competitions" />}
            <ErrorBox error={save.error} />
            <div className="flex justify-end gap-2"><Button onClick={() => setEditing(null)}>Cancel</Button><Button type="submit" variant="primary" disabled={save.isPending}>Save</Button></div>
          </form>
        )}
      </Dialog>
      <Confirm open={!!deleting} onClose={() => setDeleting(null)} title="Delete competition" message={`Delete ${deleting?.name} and all its seasons?`} busy={remove.isPending} onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })} />
    </div>
  )
}
