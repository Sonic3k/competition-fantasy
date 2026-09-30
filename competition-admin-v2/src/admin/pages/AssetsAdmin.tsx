import { useRef, useState, type FormEvent } from 'react'
import { del, post, upload } from '@/api/client'
import { useAction, useAssetStatus, useAssets } from '@/api/hooks'
import type { AssetDto } from '@/api/types'
import { Badge, Button, Confirm, Dialog, ErrorBox, Field, Input, PageTitle, Select, Spinner } from '@/components/ui'
import { readForm } from '@/lib/forms'

/** Image library: upload, browse by collection, attach to an owner's slot. */
export function AssetsAdmin() {
  const status = useAssetStatus()
  const [collection, setCollection] = useState('')
  const [page, setPage] = useState(0)
  const assets = useAssets(collection || undefined, page)
  const fileRef = useRef<HTMLInputElement>(null)
  const [attaching, setAttaching] = useState<AssetDto | null>(null)
  const [deleting, setDeleting] = useState<AssetDto | null>(null)
  const inval = [['assets'], ['slots']]
  const up = useAction((f: FormData) => upload<AssetDto>('/api/admin/assets', f), inval)
  const attach = useAction((body: unknown) => post('/api/admin/assets/attach', body), inval)
  const remove = useAction((a: { id: number; fromStorage: boolean }) => del(`/api/admin/assets/${a.id}?fromStorage=${a.fromStorage}`), inval)

  function submitUpload(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = e.currentTarget
    const f = new FormData(form)
    if (!(f.get('file') as File)?.size) return
    up.mutate(f, { onSuccess: () => form.reset() })
  }
  function submitAttach(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const v = readForm(e.currentTarget)
    attach.mutate({ ownerType: v.ownerType, ownerId: Number(v.ownerId), slot: v.slot, assetId: attaching?.id }, { onSuccess: () => setAttaching(null) })
  }

  return (
    <div>
      <PageTitle title="Assets" right={status.data && <Badge tone={status.data.configured ? 'green' : 'red'}>{status.data.configured ? `storage on · ${status.data.prefix}` : 'storage not configured'}</Badge>}>
        Images are stored in Backblaze B2 and served through the CDN. Attach one to a team, nation, competition, stadium or universe slot.
      </PageTitle>
      {status.data && !status.data.configured && <ErrorBox error={new Error('Set B2_KEY_ID and B2_APP_KEY on the API service to enable uploads.')} className="mb-4" />}

      <form onSubmit={submitUpload} className="sheet mb-6 flex flex-wrap items-end gap-3 p-3">
        <Field label="Image"><input ref={fileRef} name="file" type="file" accept="image/*" required className="text-sm" /></Field>
        <Field label="Title"><Input name="title" className="w-48" /></Field>
        <Field label="Collection" hint="folder in storage"><Input name="collection" placeholder="logos" className="w-36" /></Field>
        <Field label="Tags" hint="comma-separated"><Input name="tags" className="w-48" /></Field>
        <Button type="submit" variant="primary" disabled={up.isPending || status.data?.configured === false}>{up.isPending ? 'Uploading…' : 'Upload'}</Button>
        <ErrorBox error={up.error} className="basis-full" />
      </form>

      <div className="mb-3 flex items-center gap-3">
        <Input placeholder="Filter by collection" value={collection} onChange={(e) => { setCollection(e.target.value); setPage(0) }} className="w-56" />
        {assets.data && <span className="tnum text-sm text-ink-3">{assets.data.total} assets</span>}
        <div className="ml-auto flex gap-1">
          <Button size="sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Previous</Button>
          <Button size="sm" disabled={!assets.data || (page + 1) * assets.data.size >= assets.data.total} onClick={() => setPage((p) => p + 1)}>Next</Button>
        </div>
      </div>
      {assets.isLoading && <Spinner />}
      <ErrorBox error={assets.error ?? remove.error} />
      <ul className="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-6">
        {assets.data?.items.map((a) => (
          <li key={a.id} className="sheet flex flex-col p-2 text-xs">
            <div className="flex h-28 items-center justify-center bg-paper"><img src={a.url} alt={a.title ?? ''} className="max-h-28 max-w-full object-contain" loading="lazy" /></div>
            <div className="mt-1 truncate font-medium" title={a.title ?? ''}>{a.title ?? a.storageKey.split('/').pop()}</div>
            <div className="tnum text-ink-3">{a.width && a.height ? `${a.width}×${a.height} · ` : ''}{a.sizeBytes ? `${(a.sizeBytes / 1024).toFixed(0)} kB` : ''}{a.collection ? ` · ${a.collection}` : ''}</div>
            <div className="mt-auto flex justify-between pt-1">
              <Button size="sm" variant="ghost" onClick={() => setAttaching(a)}>Attach</Button>
              <Button size="sm" variant="ghost" onClick={() => navigator.clipboard.writeText(a.url)}>Copy URL</Button>
              <Button size="sm" variant="ghost" onClick={() => setDeleting(a)}>✕</Button>
            </div>
          </li>
        ))}
        {assets.data?.items.length === 0 && <li className="sheet col-span-full px-3 py-8 text-center text-sm text-ink-3">No assets yet.</li>}
      </ul>

      <Dialog open={!!attaching} onClose={() => setAttaching(null)} title="Attach to a slot">
        {attaching && (
          <form onSubmit={submitAttach} className="space-y-4">
            <img src={attaching.url} alt="" className="mx-auto max-h-32 object-contain" />
            <div className="grid gap-4 sm:grid-cols-3">
              <Field label="Owner type"><Select name="ownerType" defaultValue="TEAM">{(status.data?.ownerTypes ?? ['TEAM']).map((t) => <option key={t} value={t}>{t}</option>)}</Select></Field>
              <Field label="Owner id" hint="the numeric id from the URL"><Input name="ownerId" type="number" required /></Field>
              <Field label="Slot"><Select name="slot" defaultValue="LOGO">{(status.data?.slots ?? ['LOGO']).map((s) => <option key={s} value={s}>{s}</option>)}</Select></Field>
            </div>
            <ErrorBox error={attach.error} />
            <div className="flex justify-end gap-2"><Button onClick={() => setAttaching(null)}>Cancel</Button><Button type="submit" variant="primary" disabled={attach.isPending}>Attach</Button></div>
          </form>
        )}
      </Dialog>
      <Confirm open={!!deleting} onClose={() => setDeleting(null)} title="Delete asset" message={`Remove ${deleting?.title ?? 'this image'} from the library and from storage? Slots using it lose their image.`} busy={remove.isPending} onConfirm={() => deleting && remove.mutate({ id: deleting.id, fromStorage: true }, { onSuccess: () => setDeleting(null) })} />
    </div>
  )
}

