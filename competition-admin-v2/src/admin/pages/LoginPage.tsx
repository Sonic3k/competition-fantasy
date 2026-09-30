import { useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router'
import { auth, post } from '@/api/client'
import type { LoginResponse } from '@/api/types'
import { Button, ErrorBox, Field, Input } from '@/components/ui'

export function LoginPage() {
  const nav = useNavigate()
  const from = (useLocation().state as { from?: string } | null)?.from ?? '/admin'
  const [error, setError] = useState<unknown>(null)
  const [busy, setBusy] = useState(false)

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const f = new FormData(e.currentTarget)
    setBusy(true)
    setError(null)
    try {
      const res = await post<LoginResponse>('/api/auth/login', { username: f.get('username'), password: f.get('password') })
      auth.set(res.token, res.username)
      nav(from, { replace: true })
    } catch (err) {
      setError(err)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="mx-auto mt-24 w-[360px]">
      <h1 className="mb-6 font-condensed text-3xl font-semibold">Sign in</h1>
      <form onSubmit={submit} className="sheet space-y-4 p-5">
        <Field label="Username"><Input name="username" autoComplete="username" required autoFocus /></Field>
        <Field label="Password"><Input name="password" type="password" autoComplete="current-password" required /></Field>
        <ErrorBox error={error} />
        <Button type="submit" variant="primary" className="w-full" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</Button>
      </form>
    </div>
  )
}
