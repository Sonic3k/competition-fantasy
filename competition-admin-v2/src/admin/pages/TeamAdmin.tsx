import { useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router'
import { del, put } from '@/api/client'
import { keys, useAction, useTeam } from '@/api/hooks'
import type { KitDto, ProfileDto } from '@/api/types'
import { Badge, Button, Dialog, ErrorBox, Field, Input, PageTitle, Select, Spinner, Textarea } from '@/components/ui'
import { numOrNull, readForm, strOrNull } from '@/lib/forms'
import { pickColor } from '@/lib/format'
import { ColorsInput, colorsFromForm } from '../components/ColorsInput'
import { SlotImage } from '../components/SlotImage'

/** Year-keyed identity of a team: profiles (name, sponsor, colours) and kits, plus the logo slot. */
export function TeamAdmin() {
  const id = Number(useParams().id)
  const q = useTeam(id)
  const [profile, setProfile] = useState<ProfileDto | 'new' | null>(null)
  const [kit, setKit] = useState<KitDto | 'new' | null>(null)
  const inval = [keys.team(id)]
  const saveProfile = useAction((body: unknown) => put(`/api/admin/teams/${id}/profiles`, body), inval)
  const deleteProfile = useAction((pid: number) => del(`/api/admin/team-profiles/${pid}`), inval)
  const saveKit = useAction((body: unknown) => put(`/api/admin/teams/${id}/kits`, body), inval)
  const deleteKit = useAction((kid: number) => del(`/api/admin/kits/${kid}`), inval)

  if (q.isLoading) return <Spinner />
  if (q.error) return <ErrorBox error={q.error} />
  const { team, profiles, kits } = q.data!

  function submitProfile(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const v = readForm(e.currentTarget)
    saveProfile.mutate({ year: numOrNull(v.year), displayName: strOrNull(v.displayName), sponsor: strOrNull(v.sponsor), colors: colorsFromForm(v), notes: strOrNull(v.notes) }, { onSuccess: () => setProfile(null) })
  }
  function submitKit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const v = readForm(e.currentTarget)
    saveKit.mutate({ year: numOrNull(v.year), kind: v.kind, shirtColor: strOrNull(v.shirtColor), shortsColor: strOrNull(v.shortsColor), socksColor: strOrNull(v.socksColor), sponsor: strOrNull(v.sponsor) }, { onSuccess: () => setKit(null) })
  }

  return (
    <div>
      <PageTitle
        eyebrow={<Link to="/admin" className="hover:underline">Universes</Link>}
        title={team.name}
        right={<Link to={`/teams/${team.id}`} className="text-sm text-biro hover:underline">View</Link>}
      >
        <Badge tone={team.type === 'NATIONAL' ? 'blue' : 'neutral'}>{team.type === 'NATIONAL' ? 'National team' : 'Club'}</Badge>
        {team.nationName && <span className="ml-2">{team.nationName}</span>}
        <span className="ml-2 font-mono text-xs text-ink-3">{team.key}</span>
      </PageTitle>

      <div className="grid gap-8 lg:grid-cols-[1fr_280px]">
        <div className="space-y-8">
          <section>
            <div className="mb-2 flex items-center justify-between">
              <h2 className="font-condensed text-lg font-semibold">Profiles by year</h2>
              <Button size="sm" variant="primary" onClick={() => setProfile('new')}>Add year</Button>
            </div>
            <p className="mb-2 text-sm text-ink-3">The season view picks the profile whose year matches the season: display name, sponsor and shirt colours.</p>
            <table className="ruled sheet w-full text-sm">
              <thead className="font-condensed text-ink-2"><tr><th className="px-3 py-2 text-left">Year</th><th className="px-3 py-2 text-left">Display name</th><th className="px-3 py-2 text-left">Sponsor</th><th className="px-3 py-2 text-left">Colours</th><th /></tr></thead>
              <tbody>
                {profiles.map((p) => (
                  <tr key={p.id}>
                    <td className="tnum px-3 py-1.5">{p.year}</td>
                    <td className="px-3 py-1.5">{p.displayName ?? <span className="text-ink-3">{team.name}</span>}</td>
                    <td className="px-3 py-1.5 text-ink-2">{p.sponsor}</td>
                    <td className="px-3 py-1.5"><span className="inline-flex gap-1">{[p.colors?.home?.bg, p.colors?.home?.text, p.colors?.away?.bg, p.colors?.away?.text].map((c, i) => <span key={i} className="h-4 w-4 rounded-[2px] border border-rule" style={{ background: c ?? 'transparent' }} />)}</span></td>
                    <td className="px-3 py-1.5 text-right whitespace-nowrap"><Button size="sm" variant="ghost" onClick={() => setProfile(p)}>Edit</Button><Button size="sm" variant="ghost" onClick={() => deleteProfile.mutate(p.id)}>Delete</Button></td>
                  </tr>
                ))}
                {profiles.length === 0 && <tr><td colSpan={5} className="px-3 py-6 text-center text-ink-3">No yearly profile yet; the team shows with its base name and nation colours.</td></tr>}
              </tbody>
            </table>
          </section>

          <section>
            <div className="mb-2 flex items-center justify-between">
              <h2 className="font-condensed text-lg font-semibold">Kits</h2>
              <Button size="sm" variant="primary" onClick={() => setKit('new')}>Add kit</Button>
            </div>
            <ul className="grid grid-cols-2 gap-3 sm:grid-cols-4">
              {kits.map((k) => (
                <li key={k.id} className="sheet p-3 text-sm">
                  <div className="mx-auto flex h-20 w-14 flex-col overflow-hidden rounded-sm border border-rule">
                    <span className="flex-[3]" style={{ background: k.shirtColor ?? '#ddd' }} /><span className="flex-[2]" style={{ background: k.shortsColor ?? '#eee' }} /><span className="flex-1" style={{ background: k.socksColor ?? '#ccc' }} />
                  </div>
                  <div className="tnum mt-2 text-center">{k.year} · {k.kind.toLowerCase()}</div>
                  <div className="mt-1 flex justify-center gap-1"><Button size="sm" variant="ghost" onClick={() => setKit(k)}>Edit</Button><Button size="sm" variant="ghost" onClick={() => deleteKit.mutate(k.id)}>Delete</Button></div>
                </li>
              ))}
              {kits.length === 0 && <li className="sheet col-span-full px-3 py-6 text-center text-sm text-ink-3">No kits yet.</li>}
            </ul>
          </section>
        </div>

        <aside className="space-y-6">
          <SlotImage ownerType="TEAM" ownerId={team.id} slot="LOGO" label="Logo" collection="logos" onChanged={() => q.refetch()} />
          <div className="text-sm">
            <div className="mb-1 text-ink-2">Preview colour</div>
            <div className="h-10 rounded-sm border border-rule" style={{ background: pickColor(profiles[0]?.colors) ?? 'var(--color-rule)' }} />
          </div>
        </aside>
      </div>

      <Dialog open={profile !== null} onClose={() => setProfile(null)} title={profile === 'new' ? 'Add profile year' : profile ? `Profile ${profile.year}` : ''} width="lg">
        {profile !== null && (
          <form onSubmit={submitProfile} className="space-y-4" key={profile === 'new' ? 'new' : profile.id}>
            <div className="grid gap-4 sm:grid-cols-3">
              <Field label="Year"><Input name="year" type="number" required defaultValue={profile === 'new' ? '' : profile.year} readOnly={profile !== 'new'} /></Field>
              <Field label="Display name" hint="Leave empty to use the team name"><Input name="displayName" defaultValue={profile === 'new' ? '' : profile.displayName ?? ''} /></Field>
              <Field label="Sponsor"><Input name="sponsor" defaultValue={profile === 'new' ? '' : profile.sponsor ?? ''} /></Field>
            </div>
            <ColorsInput kind="team" value={profile === 'new' ? null : profile.colors} />
            <Field label="Notes"><Textarea name="notes" rows={2} defaultValue={profile === 'new' ? '' : profile.notes ?? ''} /></Field>
            <ErrorBox error={saveProfile.error} />
            <div className="flex justify-end gap-2"><Button onClick={() => setProfile(null)}>Cancel</Button><Button type="submit" variant="primary" disabled={saveProfile.isPending}>Save</Button></div>
          </form>
        )}
      </Dialog>

      <Dialog open={kit !== null} onClose={() => setKit(null)} title={kit === 'new' ? 'Add kit' : 'Edit kit'}>
        {kit !== null && (
          <form onSubmit={submitKit} className="space-y-4" key={kit === 'new' ? 'new' : kit.id}>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Year"><Input name="year" type="number" required defaultValue={kit === 'new' ? '' : kit.year} readOnly={kit !== 'new'} /></Field>
              <Field label="Kind"><Select name="kind" defaultValue={kit === 'new' ? 'HOME' : kit.kind} disabled={kit !== 'new'}><option value="HOME">Home</option><option value="AWAY">Away</option><option value="THIRD">Third</option></Select></Field>
              {kit !== 'new' && <input type="hidden" name="kind" value={kit.kind} />}
              <Field label="Shirt"><Input name="shirtColor" placeholder="#rrggbb" defaultValue={kit === 'new' ? '' : kit.shirtColor ?? ''} /></Field>
              <Field label="Shorts"><Input name="shortsColor" placeholder="#rrggbb" defaultValue={kit === 'new' ? '' : kit.shortsColor ?? ''} /></Field>
              <Field label="Socks"><Input name="socksColor" placeholder="#rrggbb" defaultValue={kit === 'new' ? '' : kit.socksColor ?? ''} /></Field>
              <Field label="Sponsor"><Input name="sponsor" defaultValue={kit === 'new' ? '' : kit.sponsor ?? ''} /></Field>
            </div>
            <ErrorBox error={saveKit.error} />
            <div className="flex justify-end gap-2"><Button onClick={() => setKit(null)}>Cancel</Button><Button type="submit" variant="primary" disabled={saveKit.isPending}>Save</Button></div>
          </form>
        )}
      </Dialog>
    </div>
  )
}
