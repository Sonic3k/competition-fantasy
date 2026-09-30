import { useRef, useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { post, upload } from '@/api/client'
import type { AssetDto, SlotDto } from '@/api/types'
import { keys, useSlots } from '@/api/hooks'
import { Button, ErrorBox, cx } from '@/components/ui'

/** Current image of an owner's slot with an upload-and-attach control. */
export function SlotImage({ ownerType, ownerId, slot, label, collection, shape = 'square', onChanged }: {
  ownerType: string
  ownerId: number
  slot: string
  label: string
  collection?: string
  shape?: 'square' | 'wide'
  onChanged?: () => void
}) {
  const qc = useQueryClient()
  const slots = useSlots(ownerType, ownerId)
  const current = slots.data?.find((s) => s.slot === slot && s.current)
  const fileRef = useRef<HTMLInputElement>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<unknown>(null)

  async function onFile(file: File) {
    setBusy(true)
    setError(null)
    try {
      const form = new FormData()
      form.append('file', file)
      form.append('collection', collection ?? ownerType.toLowerCase())
      form.append('title', `${ownerType.toLowerCase()} ${ownerId} ${slot.toLowerCase()}`)
      const asset = await upload<AssetDto>('/api/admin/assets', form)
      await post<SlotDto>('/api/admin/assets/attach', { ownerType, ownerId, slot, assetId: asset.id })
      qc.invalidateQueries({ queryKey: keys.slots(ownerType, ownerId) })
      qc.invalidateQueries({ queryKey: ['assets'] })
      onChanged?.()
    } catch (e) {
      setError(e)
    } finally {
      setBusy(false)
      if (fileRef.current) fileRef.current.value = ''
    }
  }

  return (
    <div className="text-sm">
      <div className="mb-1 text-ink-2">{label}</div>
      <div className="flex items-center gap-3">
        <div className={cx('flex items-center justify-center overflow-hidden rounded-sm border border-rule bg-paper', shape === 'wide' ? 'h-16 w-28' : 'h-16 w-16')}>
          {current?.url ? <img src={current.url} alt="" className="h-full w-full object-contain" /> : <span className="text-xs text-ink-3">none</span>}
        </div>
        <div>
          <input ref={fileRef} type="file" accept="image/*" className="hidden" onChange={(e) => e.target.files?.[0] && onFile(e.target.files[0])} />
          <Button size="sm" onClick={() => fileRef.current?.click()} disabled={busy}>{busy ? 'Uploading…' : current ? 'Replace' : 'Upload'}</Button>
        </div>
      </div>
      <ErrorBox error={error} className="mt-2" />
    </div>
  )
}
