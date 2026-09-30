import { forwardRef, useEffect, useRef, type ButtonHTMLAttributes, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes, type TextareaHTMLAttributes } from 'react'
import { ApiError } from '@/api/client'

export function cx(...parts: Array<string | false | null | undefined>) {
  return parts.filter(Boolean).join(' ')
}

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger'
export function Button({ variant = 'secondary', size = 'md', className, ...rest }: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: ButtonVariant; size?: 'sm' | 'md' }) {
  const base = 'inline-flex items-center justify-center gap-1.5 font-medium rounded-sm border transition-colors disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap'
  const sizes = size === 'sm' ? 'text-[13px] px-2.5 h-7' : 'text-sm px-3.5 h-9'
  const variants: Record<ButtonVariant, string> = {
    primary: 'bg-biro text-white border-biro hover:bg-[#1d47b5]',
    secondary: 'bg-sheet text-ink border-rule hover:border-ink-3',
    ghost: 'bg-transparent text-ink-2 border-transparent hover:bg-rule-soft',
    danger: 'bg-sheet text-relegation border-relegation/50 hover:bg-relegation-soft',
  }
  return <button type="button" className={cx(base, sizes, variants[variant], className)} {...rest} />
}

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(function Input({ className, ...rest }, ref) {
  return <input ref={ref} className={cx('h-9 w-full rounded-sm border border-rule bg-sheet px-2.5 text-sm text-ink placeholder:text-ink-3 focus:border-biro', className)} {...rest} />
})

export function Select({ className, children, ...rest }: SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <select className={cx('h-9 w-full rounded-sm border border-rule bg-sheet px-2 text-sm text-ink focus:border-biro', className)} {...rest}>
      {children}
    </select>
  )
}

export function Textarea({ className, ...rest }: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea className={cx('w-full rounded-sm border border-rule bg-sheet px-2.5 py-2 text-sm text-ink placeholder:text-ink-3 focus:border-biro', className)} {...rest} />
}

export function Field({ label, hint, children, className }: { label: string; hint?: string; children: ReactNode; className?: string }) {
  return (
    <label className={cx('block text-sm', className)}>
      <span className="mb-1 block text-ink-2">{label}</span>
      {children}
      {hint && <span className="mt-1 block text-xs text-ink-3">{hint}</span>}
    </label>
  )
}

export function Badge({ children, tone = 'neutral', className }: { children: ReactNode; tone?: 'neutral' | 'blue' | 'green' | 'amber' | 'red' | 'marker'; className?: string }) {
  const tones = {
    neutral: 'bg-rule-soft text-ink-2',
    blue: 'bg-biro-soft text-biro',
    green: 'bg-qualify-soft text-qualify',
    amber: 'bg-playoff-soft text-playoff',
    red: 'bg-relegation-soft text-relegation',
    marker: 'bg-marker-soft text-ink',
  }
  return <span className={cx('inline-flex items-center rounded-sm px-1.5 py-0.5 text-xs font-medium leading-4', tones[tone], className)}>{children}</span>
}

export function Spinner({ label = 'Loading' }: { label?: string }) {
  return (
    <div className="flex items-center gap-2 py-6 text-sm text-ink-3" role="status">
      <span className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-rule border-t-biro" />
      {label}
    </div>
  )
}

export function Empty({ title, hint, action }: { title: string; hint?: string; action?: ReactNode }) {
  return (
    <div className="sheet px-6 py-10 text-center">
      <p className="text-ink-2">{title}</p>
      {hint && <p className="mt-1 text-sm text-ink-3">{hint}</p>}
      {action && <div className="mt-4 flex justify-center">{action}</div>}
    </div>
  )
}

export function ErrorBox({ error, className }: { error: unknown; className?: string }) {
  if (!error) return null
  const e = error as Partial<ApiError> & { message?: string }
  const list = e instanceof ApiError ? e.errors : []
  return (
    <div className={cx('rounded-sm border border-relegation/40 bg-relegation-soft px-3 py-2 text-sm text-relegation', className)} role="alert">
      <p>{e.message ?? 'Something went wrong'}</p>
      {list.length > 0 && (
        <ul className="mt-1 list-disc pl-5 text-[13px]">
          {list.slice(0, 20).map((x, i) => <li key={i}>{x}</li>)}
          {list.length > 20 && <li>…and {list.length - 20} more</li>}
        </ul>
      )}
    </div>
  )
}

/** Native <dialog> modal. */
export function Dialog({ open, onClose, title, children, width = 'md' }: { open: boolean; onClose: () => void; title: string; children: ReactNode; width?: 'md' | 'lg' | 'xl' }) {
  const ref = useRef<HTMLDialogElement>(null)
  useEffect(() => {
    const d = ref.current
    if (!d) return
    if (open && !d.open) d.showModal()
    if (!open && d.open) d.close()
  }, [open])
  const widths = { md: 'w-[min(560px,95vw)]', lg: 'w-[min(800px,95vw)]', xl: 'w-[min(1100px,96vw)]' }
  return (
    <dialog ref={ref} onClose={onClose} onCancel={onClose} className={cx('m-auto rounded-sm border border-rule bg-sheet p-0 text-ink shadow-xl', widths[width])}>
      {open && (
        <div className="max-h-[88vh] overflow-y-auto">
          <div className="flex items-center justify-between border-b border-rule px-5 py-3">
            <h2 className="font-condensed text-lg font-semibold">{title}</h2>
            <button type="button" onClick={onClose} className="text-ink-3 hover:text-ink" aria-label="Close">✕</button>
          </div>
          <div className="px-5 py-4">{children}</div>
        </div>
      )}
    </dialog>
  )
}

export function Tabs<T extends string>({ value, onChange, items, className }: { value: T; onChange: (v: T) => void; items: Array<{ value: T; label: ReactNode; count?: number }>; className?: string }) {
  return (
    <div role="tablist" className={cx('flex flex-wrap gap-1 border-b border-rule', className)}>
      {items.map((it) => {
        const active = it.value === value
        return (
          <button
            key={it.value}
            role="tab"
            type="button"
            aria-selected={active}
            onClick={() => onChange(it.value)}
            className={cx('-mb-px flex items-center gap-1.5 border-b-2 px-3 py-2 font-condensed text-[15px]', active ? 'border-ink text-ink font-semibold' : 'border-transparent text-ink-2 hover:text-ink')}
          >
            {it.label}
            {it.count != null && <span className="tnum text-xs text-ink-3">{it.count}</span>}
          </button>
        )
      })}
    </div>
  )
}

export function PageTitle({ eyebrow, title, right, children }: { eyebrow?: ReactNode; title: ReactNode; right?: ReactNode; children?: ReactNode }) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        {eyebrow && <div className="mb-1 text-sm text-ink-2">{eyebrow}</div>}
        <h1 className="font-condensed text-3xl font-semibold leading-tight text-ink">{title}</h1>
        {children && <div className="mt-2 text-sm text-ink-2">{children}</div>}
      </div>
      {right && <div className="flex flex-wrap items-center gap-2">{right}</div>}
    </div>
  )
}

export function Confirm({ open, onClose, onConfirm, title, message, busy }: { open: boolean; onClose: () => void; onConfirm: () => void; title: string; message: string; busy?: boolean }) {
  return (
    <Dialog open={open} onClose={onClose} title={title}>
      <p className="text-sm text-ink-2">{message}</p>
      <div className="mt-5 flex justify-end gap-2">
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="danger" onClick={onConfirm} disabled={busy}>Delete</Button>
      </div>
    </Dialog>
  )
}
