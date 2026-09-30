import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { del, post, put } from '@/api/client'
import { keys, useAction, useUniverses } from '@/api/hooks'
import type { UniverseDto } from '@/api/types'
import { Badge, Button, Confirm, Dialog, ErrorBox, Field, Input, PageTitle, Select, Spinner, Textarea } from '@/components/ui'
import { readForm, strOrNull } from '@/lib/forms'
import { slugify } from '@/lib/format'
import { SlotImage } from '../components/SlotImage'

export function AdminHome() {
  const q = useUniverses()
  const [editing, setEditing] = useState<UniverseDto | 'new' | null>(null)
  const [deleting, setDeleting] = useState<UniverseDto | null>(null)

  const save = useAction((args: { id?: number; body: unknown }) => (args.id ? put<UniverseDto>(`/api/admin/universes/${args.id}`, args.body) : post<UniverseDto>('/api/admin/universes', args.body)), [keys.universes])
  const remove = useAction((id: number) => del(`/api/admin/universes/${id}`), [keys.universes])

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const v = readForm(e.currentTarget)
    const body = { key: v.key, name: v.name, description: strOrNull(v.description), type: v.type, usesNations: v.usesNations === 'on' }
    save.mutate({ id: editing && editing !== 'new' ? editing.id : undefined, body }, { onSuccess: () => setEditing(null) })
  }

  return (
    <div>
      <PageTitle title="Universes" right={<Button variant="primary" onClick={() => setEditing('new')}>New universe</Button>} />
      {q.isLoading && <Spinner />}
      <ErrorBox error={q.error} />
      <table className="ruled sheet w-full text-sm">
        <thead className="font-condensed text-ink-2">
          <tr><th className="px-4 py-2 text-left">Name</th><th className="px-2 py-2 text-left">Key</th><th className="px-2 py-2 text-left">Type</th><th className="px-2 py-2 text-right">Competitions</th><th className="px-2 py-2 text-right">Teams</th><th className="px-4 py-2" /></tr>
        </thead>
        <tbody>
          {q.data?.map((u) => (
            <tr key={u.id}>
              <td className="px-4 py-2"><Link to={`/admin/universes/${u.key}`} className="font-medium hover:underline">{u.name}</Link></td>
              <td className="px-2 py-2 font-mono text-xs text-ink-2">{u.key}</td>
              <td className="px-2 py-2"><Badge tone={u.type === 'REAL' ? 'blue' : 'marker'}>{u.type}</Badge>{u.usesNations && <Badge className="ml-1">nations</Badge>}</td>
              <td className="tnum px-2 py-2 text-right">{u.competitionCount}</td>
              <td className="tnum px-2 py-2 text-right">{u.teamCount}</td>
              <td className="px-4 py-2 text-right">
                <Button size="sm" variant="ghost" onClick={() => setEditing(u)}>Edit</Button>
                <Button size="sm" variant="ghost" onClick={() => setDeleting(u)}>Delete</Button>
              </td>
            </tr>
          ))}
          {q.data?.length === 0 && <tr><td colSpan={6} className="px-4 py-8 text-center text-ink-3">No universes yet. Create one, or import a JSON file from the Script manager.</td></tr>}
        </tbody>
      </table>

      <Dialog open={editing !== null} onClose={() => setEditing(null)} title={editing === 'new' ? 'New universe' : 'Edit universe'}>
        {editing !== null && (
          <form onSubmit={submit} className="space-y-4" key={editing === 'new' ? 'new' : editing.id}>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Name"><Input name="name" required defaultValue={editing === 'new' ? '' : editing.name} onChange={(e) => { const f = e.currentTarget.form; if (editing === 'new' && f) (f.elements.namedItem('key') as HTMLInputElement).value = slugify(e.currentTarget.value) }} /></Field>
              <Field label="Key" hint="Stable slug used in URLs and import files"><Input name="key" required defaultValue={editing === 'new' ? '' : editing.key} readOnly={editing !== 'new'} pattern="[a-z0-9][a-z0-9-]*" /></Field>
              <Field label="Type">
                <Select name="type" defaultValue={editing === 'new' ? 'FICTIONAL' : editing.type}><option value="FICTIONAL">Fictional</option><option value="REAL">Real world</option></Select>
              </Field>
              <label className="mt-6 flex items-center gap-2 text-sm"><input type="checkbox" name="usesNations" defaultChecked={editing === 'new' ? true : editing.usesNations} /> Has nations</label>
            </div>
            <Field label="Description"><Textarea name="description" rows={3} defaultValue={editing === 'new' ? '' : editing.description ?? ''} /></Field>
            {editing !== 'new' && (
              <div className="grid gap-4 sm:grid-cols-2">
                <SlotImage ownerType="UNIVERSE" ownerId={editing.id} slot="AVATAR" label="Avatar" collection="universes" />
                <SlotImage ownerType="UNIVERSE" ownerId={editing.id} slot="BANNER" label="Banner" collection="universes" shape="wide" />
              </div>
            )}
            <ErrorBox error={save.error} />
            <div className="flex justify-end gap-2"><Button onClick={() => setEditing(null)}>Cancel</Button><Button type="submit" variant="primary" disabled={save.isPending}>Save</Button></div>
          </form>
        )}
      </Dialog>
      <Confirm open={!!deleting} onClose={() => setDeleting(null)} title="Delete universe" message={`Delete "${deleting?.name}" with all its teams, competitions and seasons? This cannot be undone.`} busy={remove.isPending} onConfirm={() => deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })} />
    </div>
  )
}
