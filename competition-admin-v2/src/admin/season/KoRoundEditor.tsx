import { useState } from 'react'
import { del, post, put } from '@/api/client'
import { keys, useAction } from '@/api/hooks'
import type { KoRoundView, SeasonView, StageView, TieView } from '@/api/types'
import { Button, Confirm, ErrorBox, Select, cx } from '@/components/ui'
import { MatchEditor, type MatchPayload } from './MatchEditor'

export function KoRoundEditor({ season, stage, round }: { season: SeasonView; stage: StageView; round: KoRoundView }) {
  const seasonId = season.season.id
  const inval = [keys.season(seasonId)]
  const pool = season.seasonTeams.map((t) => t.teamId)
  const addTie = useAction((body: unknown) => post(`/api/admin/ko-rounds/${round.id}/ties`, body), inval)
  const updateTie = useAction((a: { id: number; body: unknown }) => put(`/api/admin/ties/${a.id}`, a.body), inval)
  const deleteTie = useAction((id: number) => del(`/api/admin/ties/${id}`), inval)
  const addMatch = useAction((p: MatchPayload) => post(`/api/admin/seasons/${seasonId}/matches`, p), inval)
  const saveMatch = useAction((a: { id: number; p: MatchPayload }) => put(`/api/admin/matches/${a.id}`, a.p), inval)
  const deleteMatch = useAction((id: number) => del(`/api/admin/matches/${id}`), inval)
  const [confirm, setConfirm] = useState<number | null>(null)

  return (
    <div>
      <div className="mb-3 flex items-baseline justify-between">
        <h2 className="font-condensed text-xl font-semibold">{stage.name} <span className="text-ink-3">/</span> {round.name}</h2>
        <span className="text-xs text-ink-3">{round.legs === 2 ? 'two legs' : 'one match'}{round.placement ? ' · placement' : ''}</span>
      </div>
      <div className="space-y-4">
        {round.ties.map((tie) => (
          <TieBlock key={tie.id} tie={tie} season={season} pool={pool} legs={round.legs}
            onTeams={(homeTeamId, awayTeamId) => updateTie.mutate({ id: tie.id, body: { position: tie.position, homeTeamId, awayTeamId } })}
            onDelete={() => setConfirm(tie.id)}
            onAddMatch={(p) => addMatch.mutate({ ...p, tieId: tie.id })}
            onSaveMatch={(id, p) => saveMatch.mutate({ id, p: { ...p, tieId: tie.id } })}
            onDeleteMatch={(id) => deleteMatch.mutate(id)}
            busy={addMatch.isPending || saveMatch.isPending || updateTie.isPending}
            error={updateTie.variables?.id === tie.id ? updateTie.error : addMatch.variables?.tieId === tie.id ? addMatch.error : null} />
        ))}
        <Button onClick={() => addTie.mutate({ position: round.ties.length + 1 })} disabled={addTie.isPending}>Add tie {round.ties.length + 1}</Button>
        <ErrorBox error={addTie.error ?? deleteTie.error ?? deleteMatch.error} />
      </div>
      <Confirm open={confirm != null} onClose={() => setConfirm(null)} title="Delete tie" message="Delete this tie and its matches?" busy={deleteTie.isPending} onConfirm={() => confirm != null && deleteTie.mutate(confirm, { onSuccess: () => setConfirm(null) })} />
    </div>
  )
}

function TieBlock({ tie, season, pool, legs, onTeams, onDelete, onAddMatch, onSaveMatch, onDeleteMatch, busy, error }: {
  tie: TieView
  season: SeasonView
  pool: number[]
  legs: number
  onTeams: (home: number | null, away: number | null) => void
  onDelete: () => void
  onAddMatch: (p: MatchPayload) => void
  onSaveMatch: (id: number, p: MatchPayload) => void
  onDeleteMatch: (id: number) => void
  busy: boolean
  error: unknown
}) {
  const [home, setHome] = useState<number | null>(tie.homeTeamId)
  const [away, setAway] = useState<number | null>(tie.awayTeamId)
  const dirty = home !== tie.homeTeamId || away !== tie.awayTeamId
  const nextLeg = tie.matches.length + 1
  const canAddLeg = home != null && away != null && (nextLeg <= legs || legs === 1)
  const winner = tie.winnerTeamId != null ? season.teams[String(tie.winnerTeamId)]?.name : null
  const opt = (id: number) => <option key={id} value={id}>{season.teams[String(id)]?.name ?? `#${id}`}</option>

  return (
    <section className="sheet">
      <header className="flex flex-wrap items-center gap-2 border-b border-rule-soft px-3 py-2">
        <span className="tnum w-6 text-xs text-ink-3">#{tie.position}</span>
        <Select value={home ?? ''} onChange={(e) => setHome(e.target.value ? Number(e.target.value) : null)} className="h-8 w-48 text-[13px]"><option value="">home (TBD)</option>{pool.map(opt)}</Select>
        <span className="text-ink-3">v</span>
        <Select value={away ?? ''} onChange={(e) => setAway(e.target.value ? Number(e.target.value) : null)} className="h-8 w-48 text-[13px]"><option value="">away (TBD)</option>{pool.map(opt)}</Select>
        {dirty && <Button size="sm" variant="primary" onClick={() => onTeams(home, away)} disabled={busy}>Save teams</Button>}
        <span className={cx('ml-auto text-xs', winner ? 'text-ink' : 'text-ink-3')}>
          {winner ? <>Winner <b>{winner}</b>{tie.resolution ? ` (${tie.resolution.toLowerCase().replace('_', ' ')})` : ''}</> : 'undecided'}
        </span>
        <Button size="sm" variant="ghost" onClick={onDelete} aria-label="Delete tie">✕</Button>
      </header>
      <div className="divide-y divide-rule-soft">
        {tie.matches.map((m) => (
          <div key={m.id} className="flex items-start">
            <span className="tnum w-12 shrink-0 px-3 py-3 text-xs text-ink-3">leg {m.leg}</span>
            <div className="flex-1"><MatchEditor match={m} teams={season.teams} teamIds={pool} busy={busy} onSave={(p) => onSaveMatch(m.id, p)} onDelete={() => onDeleteMatch(m.id)} /></div>
          </div>
        ))}
        {canAddLeg && (
          <div className="flex items-start">
            <span className="tnum w-12 shrink-0 px-3 py-3 text-xs text-ink-3">{legs === 1 && nextLeg > 1 ? 'replay' : `leg ${nextLeg}`}</span>
            <div className="flex-1">
              <MatchEditor key={`new-${tie.id}-${nextLeg}`} teams={season.teams} teamIds={pool} leg={nextLeg} busy={busy}
                defaultHome={nextLeg === 2 ? away : home} defaultAway={nextLeg === 2 ? home : away} onSave={onAddMatch} />
            </div>
          </div>
        )}
        {!canAddLeg && tie.matches.length === 0 && <p className="px-3 py-2 text-xs text-ink-3">Set both teams to add a match.</p>}
      </div>
      <ErrorBox error={error} className="m-3" />
    </section>
  )
}
