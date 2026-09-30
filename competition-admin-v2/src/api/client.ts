const BASE = (import.meta.env.VITE_API_URL as string | undefined)?.replace(/\/$/, '') ?? ''
const TOKEN_KEY = 'cf.admin.token'
const USER_KEY = 'cf.admin.user'

export class ApiError extends Error {
  status: number
  detail: string | null
  errors: string[]
  constructor(status: number, message: string, detail: string | null, errors: string[] = []) {
    super(message)
    this.status = status
    this.detail = detail
    this.errors = errors
  }
}

export const auth = {
  token: () => localStorage.getItem(TOKEN_KEY),
  user: () => localStorage.getItem(USER_KEY),
  set(token: string, user: string) {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, user)
  },
  clear() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  },
  loggedIn: () => !!localStorage.getItem(TOKEN_KEY),
}

export function apiUrl(path: string) {
  return `${BASE}${path}`
}

async function parseError(res: Response): Promise<ApiError> {
  let detail: string | null = null
  let errors: string[] = []
  try {
    const body = await res.json()
    detail = body.detail ?? body.message ?? null
    if (Array.isArray(body.errors)) errors = body.errors.map(String)
  } catch {
    // no body
  }
  if (res.status === 401) auth.clear()
  return new ApiError(res.status, detail ?? `${res.status} ${res.statusText}`, detail, errors)
}

export async function api<T>(path: string, init: RequestInit & { json?: unknown } = {}): Promise<T> {
  const headers = new Headers(init.headers)
  const token = auth.token()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  let body = init.body
  if (init.json !== undefined) {
    headers.set('Content-Type', 'application/json')
    body = JSON.stringify(init.json)
  }
  const res = await fetch(apiUrl(path), { ...init, headers, body })
  if (!res.ok) throw await parseError(res)
  if (res.status === 204) return undefined as T
  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}

export const get = <T>(path: string) => api<T>(path)
export const post = <T>(path: string, json?: unknown) => api<T>(path, { method: 'POST', json })
export const put = <T>(path: string, json?: unknown) => api<T>(path, { method: 'PUT', json })
export const del = <T = void>(path: string) => api<T>(path, { method: 'DELETE' })

export async function upload<T>(path: string, form: FormData): Promise<T> {
  return api<T>(path, { method: 'POST', body: form })
}
