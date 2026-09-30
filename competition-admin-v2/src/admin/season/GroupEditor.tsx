import { useMemo, useState, type FormEvent } from 'react'
import { del, post, put } from '@/api/client'
import { keys, useAction } from '@/api/hooks'
import type { GroupView, SeasonView, StageView } from '@/api/types'
import { Button, Confirm, ErrorBox, Field, Input, Select, Spinner, Tabs } from '@/components/ui'
import { numOrNull, readForm } from '@/lib/forms'
import { MatchEditor, type MatchPayload } from './MatchEditor'

type Tab = 'matches' | 'teams' | 'recorded'

export function GroupEditor({ season, stage, group }: { season: SeasonView; stage: StageView; group: GroupView }) {
  const [tab, setTab] = useState<Tab>('matches')
  return (
    <div>
      <div className="mb-3 flex items-baseline justify-between">
        <h2 className="font-condensed text-xl font-semibold">{stage.name} <span className="text-ink-3">/</span> {group.name}</h2>
        <span className="tnum text-xs text-ink-3">{group.teams.length} teams · {group.rounds.length} rounds</span>
      </div>
      <Tabs value={tab} onChange={setTab} className="mb-4" items={[{ value: 'matches', label: 'Rounds & matches' }, { value: 'teams', label: 'Teams', count: group.teams.length }, { value: 'recorded', label: 'Notebook tables', count: group.recorded.length }]} />
      {tab === 'teams' && <GroupTeams season={season} group={group} />}
      {tab === 'matches' && <GroupRounds season={season} group={group} />}
      {tab === 'recorded' && <RecordedEditor season={season} group={group} />}
    </div>
  )
}

function GroupTeams({ season, group }: { season: SeasonView; group: GroupView }) {
  const seasonTeamIds = season.seasonTeams.map((t) => t.teamId)
  const [selected, setSelected] = useState<number[]>(group.teams.map((t) => t.teamId).filter((x): x is number => x != null))
  const save = useAction((ids: number[]) => put(`/api/admin/stage-groups/${group.id}/teams`, ids), [keys.season(season.season.id)])
  const toggle = (id: number) => setSelected((s) => (s.includes(id) ? s.filter((x) => x !== id) : [...s, id]))
  if (seasonTeamIds.length === 0) return <p className="text-sm text-ink-3">Add teams to the season first (Season teams, top of the page).</p>
  return (
    <div>
      <p className="mb-2 text-sm text-ink-2">Tick the teams in this group. Order is the seeding order shown before any match is played.</p>
      <ul className="grid grid-cols-2 gap-1 text-sm sm:grid-cols-3">
        {seasonTeamIds.map((id) => (
          <li key={id}>
            <label className="flex items-center gap-2 rounded-sm px-2 py-1 hover:bg-paper">
              <input type="checkbox" checked={selected.includes(id)} onChange={() => toggle(id)} />
              {season.teams[String(id)]?.name ?? `#${id}`}
            </label>
          </li>
        ))}
      </ul>
      <div className="mt-3 flex items-center gap-3">
        <Button variant="primary" onClick={() => save.mutate(selected)} disabled={save.isPending}>Save teams</Button>
        <span className="tnum text-sm text-ink-3">{selected.length} selected</span>
      </div>
      <ErrorBox error={save.error} className="mt-2" />
    </div>
  )
}

function GroupRounds({ season, group }: { season: SeasonView; group: GroupView }) {
  const seasonId = season.season.id
  const inval = [keys.season(seasonId)]
  const teamIds = group.teams.map((t) => t.teamId).filter((x): x is number => x != null)
  const pool = teamIds.length ? teamIds : season.seasonTeams.map((t) => t.teamId)
  const addRound = useAction((body: { number?: number; name?: string }) => post(`/api/admin/stage-groups/${group.id}/rounds`, body), inval)
  const deleteRound = useAction((id: number) => del(`/api/admin/rounds/${id}`), inval)
  const addMatch = useAction((p: MatchPayload) => post(`/api/admin/seasons/${seasonId}/matches`, p), inval)
  const saveMatch = useAction((a: { id: number; p: MatchPayload }) => put(`/api/admin/matches/${a.id}`, a.p), inval)
  const deleteMatch = useAction((id: number) => del(`/api/admin/matches/${id}`), inval)
  const [confirmRound, setConfirmRound] = useState<number | null>(null)
  // open the round that needs entering next: the first with a missing score, else the last
  const [openRound, setOpenRound] = useState<number | null>((group.rounds.find((r) => r.matches.length === 0 || r.matches.some((m) => m.homeScore == null)) ?? group.rounds[group.rounds.length - 1])?.id ?? null)

  return (
    <div className="space-y-3">
      {group.rounds.map((r) => {
        const open = openRound === r.id
        const played = r.matches.filter((m) => m.homeScore != null).length
        return (
          <section key={r.id} className="sheet">
            <header className="flex items-center gap-3 px-3 py-1.5">
              <button type="button" onClick={() => setOpenRound(open ? null : r.id)} className="flex flex-1 items-center gap-3 text-left">
                <span className="font-condensed text-[15px] font-semibold">{r.name}</span>
                <span className="tnum text-xs text-ink-3">{played}/{r.matches.length} played</span>
              </button>
              <Button size="sm" variant="ghost" onClick={() => setConfirmRound(r.id)}>Delete round</Button>
            </header>
            {open && (
              <div className="divide-y divide-rule-soft border-t border-rule-soft">
                {r.matches.map((m) => (
                  <MatchEditor key={m.id} match={m} teams={season.teams} teamIds={pool} busy={saveMatch.isPending} error={saveMatch.variables?.id === m.id ? saveMatch.error : null}
                    onSave={(p) => saveMatch.mutate({ id: m.id, p: { ...p, roundId: r.id } })} onDelete={() => deleteMatch.mutate(m.id)} />
                ))}
                <MatchEditor key={`new-${r.id}-${r.matches.length}`} teams={season.teams} teamIds={pool} busy={addMatch.isPending} error={addMatch.variables?.roundId === r.id ? addMatch.error : null}
                  onSave={(p) => addMatch.mutate({ ...p, roundId: r.id })} />
              </div>
            )}
          </section>
        )
      })}
      <div className="flex items-center gap-2">
        <Button onClick={() => addRound.mutate({})} disabled={addRound.isPending}>Add round {group.rounds.length + 1}</Button>
        {addRound.isPending && <Spinner label="" />}
      </div>
      <ErrorBox error={addRound.error ?? deleteRound.error ?? deleteMatch.error} />
      <Confirm open={confirmRound != null} onClose={() => setConfirmRound(null)} title="Delete round" message="Delete this round and all its matches?" busy={deleteRound.isPending} onConfirm={() => confirmRound != null && deleteRound.mutate(confirmRound, { onSuccess: () => setConfirmRound(null) })} />
    </div>
  )
}

/** Copy the notebook's table as written: one row per team, plus which round it was taken after (0 = final). */
function RecordedEditor({ season, group }: { season: SeasonView; group: GroupView }) {
  const inval = [keys.season(season.season.id)]
  const recorded = useMemo(() => [...group.recorded].sort((a, b) => (a.checkpointRound || 999) - (b.checkpointRound || 999)), [group.recorded])
  const [checkpoint, setCheckpoint] = useState<number | 'new'>(recorded.find((r) => r.checkpointRound === 0)?.checkpointRound ?? recorded[0]?.checkpointRound ?? 'new')
  const current = checkpoint === 'new' ? null : recorded.find((r) => r.checkpointRound === checkpoint) ?? null
  const teamIds = group.teams.map((t) => t.teamId).filter((x): x is number => x != null)
  const rows = current ? current.rows : teamIds.map((id, i) => ({ teamId: id, position: i + 1, played: 0, won: 0, drawn: 0, lost: 0, goalsFor: 0, goalsAgainst: 0, points: 0, zone: null as string | null }))
  const save = useAction((body: unknown) => put(`/api/admin/stage-groups/${group.id}/recorded`, body), inval)
  const remove = useAction((cp: number) => del(`/api/admin/stage-groups/${group.id}/recorded/${cp}`), inval)
  const calc = group.calculated ? new Map(group.calculated.rows.map((r) => [r.teamId, r])) : null

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const v = readForm(e.currentTarget)
    const cp = numOrNull(v.checkpointRound) ?? 0
    const out = rows.map((_, i) => ({
      teamId: numOrNull(v[`teamId.${i}`]), position: numOrNull(v[`position.${i}`]), played: numOrNull(v[`played.${i}`]), won: numOrNull(v[`won.${i}`]),
      drawn: numOrNull(v[`drawn.${i}`]), lost: numOrNull(v[`lost.${i}`]), goalsFor: numOrNull(v[`gf.${i}`]), goalsAgainst: numOrNull(v[`ga.${i}`]), points: numOrNull(v[`pts.${i}`]), zone: null,
    })).filter((r) => r.teamId != null).sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
    save.mutate({ checkpointRound: cp, rows: out }, { onSuccess: () => setCheckpoint(cp) })
  }

  if (teamIds.length === 0) return <p className="text-sm text-ink-3">Add teams to the group first.</p>
  const cols = ['position', 'played', 'won', 'drawn', 'lost', 'gf', 'ga', 'pts'] as const
  const labels: Record<(typeof cols)[number], string> = { position: '#', played: 'P', won: 'W', drawn: 'D', lost: 'L', gf: 'GF', ga: 'GA', pts: 'Pts' }
  const valueOf = (r: (typeof rows)[number], c: (typeof cols)[number]) => c === 'gf' ? r.goalsFor : c === 'ga' ? r.goalsAgainst : c === 'pts' ? r.points : r[c]

  return (
    <form onSubmit={submit} key={`${group.id}-${checkpoint}`}>
      <div className="mb-3 flex flex-wrap items-end gap-3">
        <Field label="Table">
          <Select value={checkpoint} onChange={(e) => setCheckpoint(e.target.value === 'new' ? 'new' : Number(e.target.value))} className="w-48">
            {recorded.map((r) => <option key={r.id} value={r.checkpointRound}>{r.checkpointRound === 0 ? 'Final table' : `After round ${r.checkpointRound}`}</option>)}
            <option value="new">New table…</option>
          </Select>
        </Field>
        <Field label="Taken after round" hint="0 = final table">
          <Input name="checkpointRound" type="number" min={0} defaultValue={current?.checkpointRound ?? (recorded.length ? group.rounds.length : 0)} className="w-28" />
        </Field>
        {current && <Button variant="danger" onClick={() => remove.mutate(current.checkpointRound, { onSuccess: () => setCheckpoint('new') })} disabled={remove.isPending}>Delete this table</Button>}
      </div>
      <table className="ruled sheet tnum w-full text-sm">
        <thead className="font-condensed text-ink-2"><tr><th className="px-2 py-1.5 text-left">Team</th>{cols.map((c) => <th key={c} className="px-1 py-1.5 text-right">{labels[c]}</th>)}<th className="px-2 py-1.5 text-right text-ink-3">calc</th></tr></thead>
        <tbody>
          {rows.map((r, i) => {
            const c = calc?.get(r.teamId)
            const differs = c && (c.points !== r.points || c.goalsFor !== r.goalsFor || c.goalsAgainst !== r.goalsAgainst)
            return (
              <tr key={r.teamId} className={differs ? 'bg-playoff-soft/60' : undefined}>
                <td className="px-2 py-1"><input type="hidden" name={`teamId.${i}`} value={r.teamId} />{season.teams[String(r.teamId)]?.name ?? `#${r.teamId}`}</td>
                {cols.map((col) => <td key={col} className="px-1 py-1"><Input name={`${col}.${i}`} type="number" defaultValue={valueOf(r, col) ?? 0} className="h-7 w-12 px-1 text-right" /></td>)}
                <td className="px-2 py-1 text-right text-xs text-ink-3" title="Calculated from the entered results">{c ? `${c.points} pts, ${c.goalsFor}–${c.goalsAgainst}` : ''}</td>
              </tr>
            )
          })}
        </tbody>
      </table>
      <p className="mt-1 text-xs text-ink-3">Highlighted rows differ from what the recorded results add up to — often a missing page or a notebook slip.</p>
      <ErrorBox error={save.error ?? remove.error} className="mt-2" />
      <div className="mt-3"><Button type="submit" variant="primary" disabled={save.isPending}>Save table</Button></div>
    </form>
  )
}
