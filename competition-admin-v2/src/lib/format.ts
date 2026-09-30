import type { Colors, MatchView, SeasonStatus, StageType, TeamRef, ZoneKind } from '@/api/types'

export const stageTypeLabel: Record<StageType, string> = {
  ROUND_ROBIN: 'Round robin',
  KNOCKOUT: 'Knockout',
  LEAGUE_PHASE: 'League phase',
  SINGLE_MATCH: 'Single match',
}

export const statusLabel: Record<SeasonStatus, string> = { PLANNED: 'Planned', ONGOING: 'In progress', COMPLETED: 'Completed' }

export const zoneLabel: Record<ZoneKind, string> = {
  CHAMPION: 'Champion',
  QUALIFY: 'Qualifies',
  PLAYOFF: 'Play-off',
  PROMOTION: 'Promoted',
  RELEGATION: 'Relegated',
  RELEGATION_PLAYOFF: 'Relegation play-off',
  OTHER: '',
}

export const zoneClass: Record<ZoneKind, string> = {
  CHAMPION: 'bg-marker',
  QUALIFY: 'bg-qualify',
  PLAYOFF: 'bg-playoff',
  PROMOTION: 'bg-qualify',
  RELEGATION: 'bg-relegation',
  RELEGATION_PLAYOFF: 'bg-playoff',
  OTHER: 'bg-rule',
}

export function teamName(t: TeamRef | undefined, id: number | null | undefined, fallback = 'TBD') {
  if (id == null) return fallback
  if (!t) return `#${id}`
  return t.displayName || t.name
}

export function teamShort(t: TeamRef | undefined, id: number | null | undefined, fallback = 'TBD') {
  if (id == null) return fallback
  if (!t) return `#${id}`
  return t.shortName || t.code || t.name
}

/** Primary colour of a team: this year's home shirt, then nation primary. */
export function teamColor(t: TeamRef | undefined): string | null {
  if (!t) return null
  return pickColor(t.colors) ?? pickColor(t.nationColors)
}

export function pickColor(c: Colors | null | undefined): string | null {
  if (!c) return null
  return c.home?.bg ?? c.primary ?? null
}

export function scoreText(m: MatchView): string {
  if (m.walkover) return m.walkover === 'HOME' ? 'w/o' : 'w/o'
  if (m.homeScore == null || m.awayScore == null) return m.status === 'UNKNOWN' ? '?' : 'v'
  return `${m.homeScore}–${m.awayScore}`
}

/** Suffix such as "aet 2–2, pens 4–3" */
export function scoreDetail(m: MatchView): string | null {
  const parts: string[] = []
  if (m.homeEt != null && m.awayEt != null) parts.push(`aet ${m.homeEt}–${m.awayEt}`)
  if (m.homePens != null && m.awayPens != null) parts.push(`pens ${m.homePens}–${m.awayPens}`)
  if (m.walkover) parts.push(m.walkover === 'HOME' ? 'home walkover' : 'away walkover')
  return parts.length ? parts.join(', ') : null
}

export function fmtDate(d: string | null | undefined) {
  if (!d) return ''
  const [y, m, day] = d.split('-')
  return `${day}/${m}/${y}`
}

export function fmtInstant(s: string | null | undefined) {
  if (!s) return ''
  const d = new Date(s)
  return d.toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

export function num(n: number | null | undefined) {
  return n == null ? 0 : n
}

export function slugify(s: string) {
  return s
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
}

export function isDark(hex: string | null | undefined) {
  if (!hex || !/^#([0-9a-f]{6})$/i.test(hex)) return false
  const r = parseInt(hex.slice(1, 3), 16), g = parseInt(hex.slice(3, 5), 16), b = parseInt(hex.slice(5, 7), 16)
  return (r * 299 + g * 587 + b * 114) / 1000 < 140
}
