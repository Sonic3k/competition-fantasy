import { useMemo, useState } from 'react'
import { put } from '@/api/client'
import { keys, useAction, useTeams } from '@/api/hooks'
import type { SeasonView } from '@/api/types'
import { Button, Dialog, ErrorBox, Input } from '@/components/ui'

/** Pick which of the universe's teams take part in this season. */
export function SeasonTeamsDialog({ open, onClose, season }: { open: boolean; onClose: () => void; season: SeasonView }) {
  const teams = useTeams(season.universe.key, season.competition.teamLevel)
  const [filter, setFilter] = useState('')
  const [selected, setSelected] = useState<Set<number>>(() => new Set(season.seasonTeams.map((t) => t.teamId)))
  const save = useAction((ids: number[]) => put(`/api/admin/seasons/${season.season.id}/teams`, ids.map((teamId, i) => ({ teamId, seed: i + 1 }))), [keys.season(season.season.id), keys.seasons(season.competition.id)])

  const list = useMemo(() => (teams.data ?? []).filter((t) => !filter || t.name.toLowerCase().includes(filter.toLowerCase()) || (t.nationName ?? '').toLowerCase().includes(filter.toLowerCase())), [teams.data, filter])
  const toggle = (id: number) => setSelected((s) => { const n = new Set(s); if (n.has(id)) n.delete(id); else n.add(id); return n })

  return (
    <Dialog open={open} onClose={onClose} title="Season teams" width="lg">
      <div className="mb-3 flex items-center gap-3">
        <Input placeholder="Filter" value={filter} onChange={(e) => setFilter(e.target.value)} className="w-64" />
        <span className="tnum text-sm text-ink-2">{selected.size} selected</span>
        <Button size="sm" variant="ghost" onClick={() => setSelected(new Set(list.map((t) => t.id)))}>Select shown</Button>
        <Button size="sm" variant="ghost" onClick={() => setSelected(new Set())}>Clear</Button>
      </div>
      <ul className="grid max-h-[50vh] grid-cols-2 gap-1 overflow-y-auto text-sm sm:grid-cols-3">
        {list.map((t) => (
          <li key={t.id}>
            <label className="flex items-center gap-2 rounded-sm px-2 py-1 hover:bg-paper">
              <input type="checkbox" checked={selected.has(t.id)} onChange={() => toggle(t.id)} />
              <span className="truncate">{t.name}</span>
              {t.nationCode && <span className="ml-auto text-xs text-ink-3">{t.nationCode}</span>}
            </label>
          </li>
        ))}
      </ul>
      <ErrorBox error={save.error} className="mt-3" />
      <div className="mt-4 flex justify-end gap-2">
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="primary" disabled={save.isPending} onClick={() => save.mutate([...selected], { onSuccess: onClose })}>Save {selected.size} teams</Button>
      </div>
    </Dialog>
  )
}
