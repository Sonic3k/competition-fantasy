import { Link } from 'react-router'
import type { TeamRef } from '@/api/types'
import { cx } from './ui'
import { teamColor, teamName } from '@/lib/format'

/** A team's name with its colour swatch; the recurring identity device of the viewer. */
export function TeamChip({ team, id, link = true, size = 'md', className, muted, bold }: { team?: TeamRef; id: number | null | undefined; link?: boolean; size?: 'sm' | 'md' | 'lg'; className?: string; muted?: boolean; bold?: boolean }) {
  const color = teamColor(team)
  const name = teamName(team, id)
  const sizes = { sm: 'text-[13px] gap-1.5', md: 'text-sm gap-2', lg: 'text-base gap-2.5' }
  const swatch = { sm: 'h-3 w-1', md: 'h-3.5 w-1.5', lg: 'h-5 w-2' }
  const content = (
    <span className={cx('inline-flex min-w-0 items-center', sizes[size], muted && 'text-ink-3', bold && 'font-semibold', className)}>
      {team?.logoUrl ? (
        <img src={team.logoUrl} alt="" className={cx('shrink-0 rounded-sm object-contain', size === 'lg' ? 'h-6 w-6' : 'h-4 w-4')} />
      ) : (
        <span className={cx('shrink-0 rounded-[1px]', swatch[size])} style={{ background: color ?? 'var(--color-rule)' }} aria-hidden />
      )}
      <span className="truncate">{name}</span>
    </span>
  )
  if (!link || id == null || !team) return content
  return <Link to={`/teams/${id}`} className="hover:underline decoration-ink-3 min-w-0">{content}</Link>
}
