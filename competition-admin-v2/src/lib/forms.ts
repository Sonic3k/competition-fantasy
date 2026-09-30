/** Read every named field of a form into a string map (checkboxes become 'on' or ''). */
export function readForm(form: HTMLFormElement): Record<string, string> {
  const out: Record<string, string> = {}
  const data = new FormData(form)
  for (const [k, v] of data.entries()) out[k] = typeof v === 'string' ? v : ''
  form.querySelectorAll<HTMLInputElement>('input[type=checkbox]').forEach((el) => { if (el.name && !(el.name in out)) out[el.name] = '' })
  return out
}

export const strOrNull = (v: string | undefined) => (v == null || v.trim() === '' ? null : v.trim())
export const numOrNull = (v: string | undefined) => (v == null || v.trim() === '' ? null : Number(v))
export const listOrEmpty = (v: string | undefined) => (v == null || v.trim() === '' ? [] : v.split(',').map((s) => s.trim()).filter(Boolean))

export function parseJsonOrNull(v: string | undefined): unknown {
  if (v == null || v.trim() === '') return null
  return JSON.parse(v)
}
