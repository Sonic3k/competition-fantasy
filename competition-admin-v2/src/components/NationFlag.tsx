import type { Colors } from '@/api/types'
import { cx } from './ui'
import { pickColor } from '@/lib/format'
import { flagBackground, swatchStyle } from '@/lib/color'

const sizes = { sm: 'h-4 w-6', md: 'h-6 w-9', lg: 'h-8 w-12' }

/**
 * A nation's flag: its FLAG image (for AOE2, the civ emblem) on the nation's own colour, so cream or white
 * artwork stays visible; without an image, a plain tile in the nation colour.
 */
export function NationFlag({ flagUrl, colors, size = 'md', className }: { flagUrl: string | null; colors: Colors | null; size?: 'sm' | 'md' | 'lg'; className?: string }) {
  if (flagUrl) {
    return (
      <span className={cx('inline-flex shrink-0 overflow-hidden rounded-[2px] border border-ink/15', sizes[size], className)} style={{ background: flagBackground(colors) }}>
        <img src={flagUrl} alt="" className="h-full w-full scale-125 object-contain" loading="lazy" />
      </span>
    )
  }
  return <span className={cx('inline-block shrink-0 rounded-[2px]', sizes[size], className)} style={swatchStyle(pickColor(colors), colors?.text)} aria-hidden />
}
