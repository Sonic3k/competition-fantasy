import { Link, useParams } from 'react-router'
import { useTeam } from '@/api/hooks'
import { Badge, ErrorBox, Spinner } from '@/components/ui'
import { isDark, pickColor, statusLabel } from '@/lib/format'

export function TeamPage() {
  const id = Number(useParams().id)
  const q = useTeam(id)
  if (q.isLoading) return <Spinner />
  if (q.error) return <ErrorBox error={q.error} />
  const { team, profiles, kits, seasons } = q.data!
  const latest = profiles[0]
  const color = pickColor(latest?.colors) ?? '#1b2a41'
  const text = latest?.colors?.home?.text ?? (isDark(color) ? '#ffffff' : '#1b2a41')
  const titles = seasons.filter((s) => s.honours.includes('CHAMPION')).length

  return (
    <div>
      <div className="mb-6 flex items-center gap-5 rounded-sm px-6 py-5" style={{ background: color, color: text }}>
        {team.logoUrl ? <img src={team.logoUrl} alt="" className="h-16 w-16 object-contain" /> : <span className="h-16 w-3 rounded-[2px] bg-white/30" />}
        <div>
          <div className="text-sm opacity-80">
            {team.nationId ? <Link to={`/nations/${team.nationId}`} className="hover:underline">{team.nationName}</Link> : team.type === 'NATIONAL' ? 'National team' : 'Club'}
            {team.foundedYear && <span className="tnum ml-2">est. {team.foundedYear}</span>}
          </div>
          <h1 className="font-condensed text-4xl font-semibold leading-none">{latest?.displayName ?? team.name}</h1>
          {latest?.sponsor && <div className="mt-1 text-sm opacity-80">Sponsor {latest.sponsor}</div>}
        </div>
        <div className="tnum ml-auto text-right">
          <div className="font-condensed text-4xl font-semibold leading-none">{titles}</div>
          <div className="text-xs opacity-80">{titles === 1 ? 'title' : 'titles'}</div>
        </div>
      </div>

      <div className="grid gap-8 lg:grid-cols-[1fr_300px]">
        <section>
          <h2 className="mb-2 font-condensed text-lg font-semibold">Seasons</h2>
          {seasons.length === 0 ? (
            <p className="text-sm text-ink-3">This team has not entered a season yet.</p>
          ) : (
            <table className="ruled sheet w-full text-sm">
              <thead className="font-condensed text-ink-2">
                <tr>
                  <th className="px-4 py-2 text-left">Season</th>
                  <th className="px-2 py-2 text-left">Competition</th>
                  <th className="px-2 py-2 text-left">Honours</th>
                  <th className="px-4 py-2 text-right">Status</th>
                </tr>
              </thead>
              <tbody>
                {seasons.map((s) => (
                  <tr key={s.seasonId} className="hover:bg-paper/60">
                    <td className="px-4 py-2">
                      <Link to={`/seasons/${s.seasonId}`} className="font-medium hover:underline">{s.seasonName}</Link>
                      {s.year && <span className="tnum ml-2 text-ink-3">{s.year}</span>}
                    </td>
                    <td className="px-2 py-2"><Link to={`/competitions/${s.competitionId}`} className="hover:underline">{s.competitionName}</Link></td>
                    <td className="px-2 py-2">
                      <span className="flex flex-wrap gap-1">
                        {s.honours.map((h) => <Badge key={h} tone={h === 'CHAMPION' ? 'marker' : h === 'RELEGATED' ? 'red' : 'neutral'}>{h.replace('_', ' ').toLowerCase()}</Badge>)}
                      </span>
                    </td>
                    <td className="px-4 py-2 text-right text-ink-2">{statusLabel[s.status]}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
        <aside className="space-y-6">
          <div>
            <h2 className="mb-2 font-condensed text-lg font-semibold">Details</h2>
            <dl className="sheet grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 px-4 py-3 text-sm">
              <dt className="text-ink-3">Code</dt><dd>{team.code ?? '—'}</dd>
              <dt className="text-ink-3">Short name</dt><dd>{team.shortName ?? '—'}</dd>
              {team.aliases.length > 0 && <><dt className="text-ink-3">Also known as</dt><dd>{team.aliases.join(', ')}</dd></>}
              {team.dissolvedYear && <><dt className="text-ink-3">Dissolved</dt><dd className="tnum">{team.dissolvedYear}</dd></>}
            </dl>
            {team.description && <p className="mt-2 text-sm text-ink-2">{team.description}</p>}
          </div>
          {kits.length > 0 && (
            <div>
              <h2 className="mb-2 font-condensed text-lg font-semibold">Kits</h2>
              <ul className="grid grid-cols-3 gap-2">
                {kits.map((k) => (
                  <li key={k.id} className="sheet p-2 text-center text-xs">
                    {k.imageUrl ? (
                      <img src={k.imageUrl} alt="" className="mx-auto h-16 object-contain" />
                    ) : (
                      <div className="mx-auto flex h-16 w-12 flex-col overflow-hidden rounded-sm border border-rule">
                        <span className="flex-[3]" style={{ background: k.shirtColor ?? '#ddd' }} />
                        <span className="flex-[2]" style={{ background: k.shortsColor ?? '#eee' }} />
                        <span className="flex-1" style={{ background: k.socksColor ?? '#ccc' }} />
                      </div>
                    )}
                    <div className="tnum mt-1 text-ink-2">{k.year} {k.kind.toLowerCase()}</div>
                  </li>
                ))}
              </ul>
            </div>
          )}
          {profiles.length > 1 && (
            <div>
              <h2 className="mb-2 font-condensed text-lg font-semibold">Identity by year</h2>
              <ul className="sheet divide-y divide-rule-soft text-sm">
                {profiles.map((p) => (
                  <li key={p.id} className="flex items-center gap-3 px-3 py-2">
                    <span className="h-4 w-4 rounded-[2px] border border-rule" style={{ background: pickColor(p.colors) ?? 'transparent' }} />
                    <span className="tnum text-ink-2">{p.year}</span>
                    <span>{p.displayName ?? team.name}</span>
                    {p.sponsor && <span className="ml-auto text-xs text-ink-3">{p.sponsor}</span>}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </aside>
      </div>
    </div>
  )
}
