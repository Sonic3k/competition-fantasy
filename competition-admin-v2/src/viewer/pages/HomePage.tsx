import { Link } from 'react-router'
import { useUniverses } from '@/api/hooks'
import { Badge, Empty, ErrorBox, Spinner } from '@/components/ui'

export function HomePage() {
  const q = useUniverses()
  return (
    <div>
      <div className="mb-10 max-w-2xl">
        <h1 className="font-condensed text-5xl font-semibold leading-none text-ink">Every table, every bracket, from the notebooks to the web.</h1>
        <p className="mt-4 text-ink-2">Fantasy football universes rebuilt season by season, alongside real tournaments they borrowed their formats from.</p>
      </div>
      {q.isLoading && <Spinner />}
      <ErrorBox error={q.error} />
      {q.data && q.data.length === 0 && <Empty title="No universes yet" hint="Load one through the Script Manager in the admin area." />}
      {q.data && q.data.length > 0 && (
        <ul className="grid gap-4 sm:grid-cols-2">
          {q.data.map((u) => (
            <li key={u.id} className="sheet">
              <Link to={`/u/${u.key}`} className="block p-5 hover:bg-paper/60">
                <div className="flex items-start justify-between gap-3">
                  <h2 className="font-condensed text-2xl font-semibold leading-tight">{u.name}</h2>
                  <Badge tone={u.type === 'REAL' ? 'blue' : 'marker'}>{u.type === 'REAL' ? 'Real world' : 'Fictional'}</Badge>
                </div>
                {u.description && <p className="mt-2 line-clamp-3 text-sm text-ink-2">{u.description}</p>}
                <p className="tnum mt-4 text-sm text-ink-3">
                  {u.competitionCount} competitions · {u.teamCount} teams{u.usesNations ? ` · ${u.nationCount} nations` : ''}
                </p>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
