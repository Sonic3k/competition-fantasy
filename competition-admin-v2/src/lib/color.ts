import type { CSSProperties } from 'react'
import type { Colors } from '@/api/types'

const INK = '#1b2a41'
const SHEET = '#ffffff'
/** Luminance of a flag tile, dark enough for the cream-and-tan civ emblems (contrast >= 2.4 against the tan). */
const DEEP = 0.07

function rgb(hex: string): [number, number, number] | null {
  const m = /^#?([0-9a-f]{3}|[0-9a-f]{6})$/i.exec(hex.trim())
  if (!m) return null
  const h = m[1].length === 3 ? m[1].split('').map((c) => c + c).join('') : m[1]
  return [parseInt(h.slice(0, 2), 16), parseInt(h.slice(2, 4), 16), parseInt(h.slice(4, 6), 16)]
}

/** WCAG relative luminance, 0 (black) to 1 (white); null for anything that is not a hex colour. */
export function luminance(hex: string | null | undefined): number | null {
  const c = hex ? rgb(hex) : null
  if (!c) return null
  const [r, g, b] = c.map((v) => {
    const s = v / 255
    return s <= 0.03928 ? s / 12.92 : ((s + 0.055) / 1.055) ** 2.4
  })
  return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

export function contrast(a: string | null | undefined, b: string | null | undefined): number {
  const la = luminance(a), lb = luminance(b)
  if (la == null || lb == null) return 21
  return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05)
}

/** A colour that all but disappears on the white sheet: white and cream shirts, pale yellows. */
export function isLight(hex: string | null | undefined): boolean {
  return luminance(hex) != null && contrast(hex, SHEET) < 1.6
}

/**
 * Fill for a colour swatch. Light colours get a 1px inset ring so they still read as a shape:
 * in the team's accent colour (its shirt text colour) when that is dark enough, otherwise in ink.
 */
export function swatchStyle(bg: string | null | undefined, accent?: string | null): CSSProperties {
  if (!bg) return { background: 'var(--color-rule)' }
  if (!isLight(bg)) return { background: bg }
  const ring = accent && contrast(accent, SHEET) >= 3 ? accent : 'rgba(27, 42, 65, 0.45)'
  return { background: bg, boxShadow: `inset 0 0 0 1px ${ring}` }
}

function darken(hex: string, t: number): string {
  const c = rgb(hex)!
  return '#' + c.map((v) => Math.round(v * (1 - t)).toString(16).padStart(2, '0')).join('')
}

/**
 * Background for a nation's flag tile. Every AOE2 civ emblem is cream line art in a tan dust halo
 * (average about #a77f5b), so mid tones swallow it; a deep shade of the nation's colour carries it,
 * like a banner. Base: the primary colour, or for a pale primary (yellow, white) the text colour.
 */
export function flagBackground(colors: Colors | null | undefined): string {
  const usable = (c: string | null | undefined): c is string => !!c && luminance(c) != null && !isLight(c)
  const base = [colors?.primary ?? colors?.home?.bg, colors?.text, colors?.secondary].find(usable) ?? INK
  let t = 0
  while ((luminance(darken(base, t)) ?? 0) > DEEP && t < 0.9) t += 0.05
  return darken(base, t)
}
