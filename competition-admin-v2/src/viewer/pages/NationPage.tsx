import { Link, useParams } from 'react-router'
import { useNation, useNationTeams } from '@/api/hooks'
import { Badge, ErrorBox, PageTitle, Spinner } from '@/components/ui'
import { pickColor } from '@/lib/format'

export function NationPage() {
  const id = Number(useParams().id)
  const n = useNation(id)
  const teams = useNationTeams(id)
  if (n.isLoading) return <Spinner />
  if (n.error) return <ErrorBox error={n.error} />
  const nation = n.data!
  const national = teams.data?.filter((t) => t.type === 'NATIONAL') ?? []
  const clubs = teams.data?.filter((t) => t.type === 'CLUB') ?? []
  return (
    <div>
      <PageTitle
        title={
          <span className="inline-flex items-center gap-3">
            {nation.flagUrl ? <img src={nation.flagUrl} alt="" className="h-8 w-12 rounded-[2px] object-cover" /> : <span className="h-8 w-12 rounded-[2px]" style={{ background: pickColor(nation.colors) ?? 'var(--color-rule)' }} />}
            {nation.name}
            <span className="text-lg font-normal text-ink-3">{nation.code}</span>
          </span>
        }
      >
        {nation.description}
      </PageTitle>
      {national.length > 0 && (
        <section className="mb-8">
          <h2 className="mb-2 font-condensed text-lg font-semibold">National team</h2>
          <ul className="sheet divide-y divide-rule-soft text-sm">
            {national.map((t) => (
              <li key={t.id}><Link to={`/teams/${t.id}`} className="flex items-center gap-3 px-4 py-2 hover:bg-paper/60">{t.name} <Badge>National</Badge></Link></li>
            ))}
          </ul>
        </section>
      )}
      <section>
        <h2 className="mb-2 font-condensed text-lg font-semibold">Clubs <span className="tnum text-sm font-normal text-ink-3">{clubs.length}</span></h2>
        {clubs.length === 0 ? <p className="text-sm text-ink-3">No clubs from this nation yet.</p> : (
          <ul className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
            {clubs.map((t) => (
              <li key={t.id} className="sheet"><Link to={`/teams/${t.id}`} className="flex items-center gap-3 px-4 py-2.5 text-sm hover:bg-paper/60">
                {t.logoUrl ? <img src={t.logoUrl} alt="" className="h-5 w-5 object-contain" /> : <span className="h-3.5 w-1.5 rounded-[1px] bg-rule" />}
                {t.name}<span className="ml-auto text-xs text-ink-3">{t.code}</span>
              </Link></li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
