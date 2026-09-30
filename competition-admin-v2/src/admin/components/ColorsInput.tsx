import type { Colors } from '@/api/types'
import { Input } from '@/components/ui'

/** Nation colours: primary / secondary / text. Team profile colours: home bg/text, away bg/text. */
export function ColorsInput({ kind, value }: { kind: 'nation' | 'team'; value: Colors | null | undefined }) {
  const fields = kind === 'nation'
    ? [['primary', 'Primary', value?.primary], ['secondary', 'Secondary', value?.secondary], ['text', 'Text', value?.text]]
    : [['home.bg', 'Home shirt', value?.home?.bg], ['home.text', 'Home text', value?.home?.text], ['away.bg', 'Away shirt', value?.away?.bg], ['away.text', 'Away text', value?.away?.text]]
  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
      {fields.map(([name, label, v]) => (
        <label key={name} className="block text-sm">
          <span className="mb-1 block text-ink-2">{label}</span>
          <div className="flex items-center gap-1.5">
            <input type="color" defaultValue={v && /^#[0-9a-f]{6}$/i.test(v) ? v : '#888888'} className="h-9 w-9 shrink-0 cursor-pointer rounded-sm border border-rule bg-sheet p-0.5" onChange={(e) => { const t = e.currentTarget.nextElementSibling as HTMLInputElement | null; if (t) t.value = e.currentTarget.value }} />
            <Input name={`colors.${name}`} defaultValue={v ?? ''} placeholder="#rrggbb" className="font-mono text-xs" />
          </div>
        </label>
      ))}
    </div>
  )
}

/** Turn dotted `colors.home.bg` form fields back into a Colors object (null when empty). */
export function colorsFromForm(values: Record<string, string>): Colors | null {
  const out: Record<string, unknown> = {}
  let any = false
  for (const [k, v] of Object.entries(values)) {
    if (!k.startsWith('colors.') || !v.trim()) continue
    any = true
    const path = k.slice(7).split('.')
    let cur = out
    for (let i = 0; i < path.length - 1; i++) cur = (cur[path[i]] as Record<string, unknown>) ?? (cur[path[i]] = {})
    cur[path[path.length - 1]] = v.trim()
  }
  return any ? (out as Colors) : null
}
