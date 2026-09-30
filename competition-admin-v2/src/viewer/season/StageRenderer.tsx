import type { StageView, TeamRef } from '@/api/types'
import { Bracket } from './Bracket'
import { RoundsPanel } from './MatchList'
import { StandingsPanel } from './StandingsPanel'

/** Chooses a layout from the stage type; nothing here knows which competition it is drawing. */
export function StageRenderer({ stage, teams }: { stage: StageView; teams: Record<string, TeamRef> }) {
  switch (stage.type) {
    case 'ROUND_ROBIN':
      return stage.groups.length <= 1 ? <SingleTable stage={stage} teams={teams} /> : <GroupGrid stage={stage} teams={teams} />
    case 'LEAGUE_PHASE':
      return <SingleTable stage={stage} teams={teams} />
    case 'KNOCKOUT':
    case 'SINGLE_MATCH':
      return <Bracket rounds={stage.koRounds} teams={teams} />
  }
}

function SingleTable({ stage, teams }: { stage: StageView; teams: Record<string, TeamRef> }) {
  const group = stage.groups[0]
  if (!group) return <p className="sheet px-4 py-6 text-center text-sm text-ink-3">No table in this stage yet.</p>
  return (
    <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,420px)]">
      <StandingsPanel group={group} teams={teams} stageConfig={stage.config} />
      <RoundsPanel rounds={group.rounds} teams={teams} />
    </div>
  )
}

function GroupGrid({ stage, teams }: { stage: StageView; teams: Record<string, TeamRef> }) {
  return (
    <div className="grid gap-6 md:grid-cols-2">
      {stage.groups.map((g) => (
        <section key={g.id}>
          <h3 className="mb-2 font-condensed text-lg font-semibold">{g.name}</h3>
          <StandingsPanel group={g} teams={teams} stageConfig={stage.config} compact />
          <details className="mt-2">
            <summary className="cursor-pointer text-sm text-ink-2 hover:text-ink">Matches</summary>
            <div className="mt-2">
              <RoundsPanel rounds={g.rounds} teams={teams} compact />
            </div>
          </details>
        </section>
      ))}
    </div>
  )
}
