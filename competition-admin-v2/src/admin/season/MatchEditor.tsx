import { useState, type FormEvent } from 'react'
import type { MatchView, TeamRef } from '@/api/types'
import { Button, ErrorBox, Input, Select, cx } from '@/components/ui'
import { numOrNull, readForm, strOrNull } from '@/lib/forms'

export interface MatchPayload {
  roundId?: number | null
  tieId?: number | null
  leg: number
  homeTeamId: number | null
  awayTeamId: number | null
  date: string | null
  status: string | null
  homeScore: number | null
  awayScore: number | null
  homeEt: number | null
  awayEt: number | null
  homePens: number | null
  awayPens: number | null
  walkover: string | null
  source: string
  confidence: string
  sourceRef: string | null
  notes: string | null
}

/** One match as an editable row: teams, score, and (expanded) extra time, penalties, walkover, date, notes. */
export function MatchEditor({ match, teams, teamIds, defaultHome, defaultAway, leg, onSave, onDelete, busy, error, lockTeams }: {
  match?: MatchView
  teams: Record<string, TeamRef>
  teamIds: number[]
  defaultHome?: number | null
  defaultAway?: number | null
  leg?: number
  onSave: (payload: MatchPayload) => void
  onDelete?: () => void
  busy?: boolean
  error?: unknown
  lockTeams?: boolean
}) {
  const [more, setMore] = useState(!!(match && (match.homeEt != null || match.homePens != null || match.walkover || match.notes)))

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const v = readForm(e.currentTarget)
    const hs = numOrNull(v.homeScore), as = numOrNull(v.awayScore)
    onSave({
      leg: leg ?? match?.leg ?? 1,
      homeTeamId: numOrNull(v.homeTeamId), awayTeamId: numOrNull(v.awayTeamId),
      date: strOrNull(v.date),
      status: v.status || (hs != null && as != null ? 'PLAYED' : 'SCHEDULED'),
      homeScore: hs, awayScore: as,
      homeEt: numOrNull(v.homeEt), awayEt: numOrNull(v.awayEt),
      homePens: numOrNull(v.homePens), awayPens: numOrNull(v.awayPens),
      walkover: strOrNull(v.walkover),
      source: match?.source ?? 'MANUAL', confidence: v.confidence || match?.confidence || 'VERIFIED',
      sourceRef: strOrNull(v.sourceRef), notes: strOrNull(v.notes),
    })
  }

  const options = teamIds.map((id) => <option key={id} value={id}>{teams[String(id)]?.name ?? `#${id}`}</option>)
  const isNew = !match
  return (
    <form onSubmit={submit} className={cx('px-3 py-2', isNew && 'bg-paper/60')} key={match?.id ?? 'new'}>
      <div className="grid grid-cols-[1fr_3.25rem_auto_3.25rem_1fr_auto] items-center gap-1.5">
        <Select name="homeTeamId" defaultValue={match?.homeTeamId ?? defaultHome ?? ''} disabled={lockTeams} className="h-8 text-[13px]"><option value="">home…</option>{options}</Select>
        <Input name="homeScore" type="number" min={0} defaultValue={match?.homeScore ?? ''} className="tnum h-8 px-1.5 text-center" aria-label="Home score" />
        <span className="text-ink-3">–</span>
        <Input name="awayScore" type="number" min={0} defaultValue={match?.awayScore ?? ''} className="tnum h-8 px-1.5 text-center" aria-label="Away score" />
        <Select name="awayTeamId" defaultValue={match?.awayTeamId ?? defaultAway ?? ''} disabled={lockTeams} className="h-8 text-[13px]"><option value="">away…</option>{options}</Select>
        <div className="flex items-center gap-1">
          <button type="button" onClick={() => setMore((m) => !m)} className="h-8 px-1.5 text-xs text-ink-3 hover:text-ink" title="Extra time, penalties, date, notes">{more ? 'less' : 'more'}</button>
          <Button type="submit" size="sm" variant={isNew ? 'primary' : 'secondary'} disabled={busy}>{isNew ? 'Add' : 'Save'}</Button>
          {onDelete && <Button size="sm" variant="ghost" onClick={onDelete} disabled={busy} aria-label="Delete match">✕</Button>}
        </div>
      </div>
      {lockTeams && <><input type="hidden" name="homeTeamId" value={match?.homeTeamId ?? defaultHome ?? ''} /><input type="hidden" name="awayTeamId" value={match?.awayTeamId ?? defaultAway ?? ''} /></>}
      {more && (
        <div className="mt-2 grid grid-cols-2 gap-2 text-xs sm:grid-cols-4">
          <label>aet <span className="flex items-center gap-1"><Input name="homeEt" type="number" min={0} defaultValue={match?.homeEt ?? ''} className="tnum h-7 px-1 text-center" /> – <Input name="awayEt" type="number" min={0} defaultValue={match?.awayEt ?? ''} className="tnum h-7 px-1 text-center" /></span></label>
          <label>pens <span className="flex items-center gap-1"><Input name="homePens" type="number" min={0} defaultValue={match?.homePens ?? ''} className="tnum h-7 px-1 text-center" /> – <Input name="awayPens" type="number" min={0} defaultValue={match?.awayPens ?? ''} className="tnum h-7 px-1 text-center" /></span></label>
          <label>date <Input name="date" type="date" defaultValue={match?.date ?? ''} className="h-7 px-1" /></label>
          <label>status
            <Select name="status" defaultValue={match?.status ?? ''} className="h-7 px-1">
              <option value="">auto</option><option value="PLAYED">Played</option><option value="SCHEDULED">Scheduled</option><option value="UNKNOWN">Unknown (page missing)</option><option value="CANCELLED">Cancelled</option>
            </Select>
          </label>
          <label>walkover <Select name="walkover" defaultValue={match?.walkover ?? ''} className="h-7 px-1"><option value="">none</option><option value="HOME">home awarded</option><option value="AWAY">away awarded</option></Select></label>
          <label>confidence <Select name="confidence" defaultValue={match?.confidence ?? 'VERIFIED'} className="h-7 px-1"><option value="VERIFIED">Verified</option><option value="DRAFT">Draft</option></Select></label>
          <label>source ref <Input name="sourceRef" defaultValue={match?.sourceRef ?? ''} placeholder="notebook page" className="h-7 px-1" /></label>
          <label>notes <Input name="notes" defaultValue={match?.notes ?? ''} className="h-7 px-1" /></label>
        </div>
      )}
      <ErrorBox error={error} className="mt-2" />
    </form>
  )
}
