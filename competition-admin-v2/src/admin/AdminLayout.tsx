import { Link, NavLink, Navigate, Outlet, useLocation } from 'react-router'
import { auth } from '@/api/client'
import { cx } from '@/components/ui'

export function RequireAuth() {
  const location = useLocation()
  if (!auth.loggedIn()) return <Navigate to="/admin/login" replace state={{ from: location.pathname }} />
  return <Outlet />
}

const nav = [
  { to: '/admin', label: 'Universes', end: true },
  { to: '/admin/presets', label: 'Format presets' },
  { to: '/admin/imports', label: 'Script manager' },
  { to: '/admin/assets', label: 'Assets' },
]

export function AdminLayout() {
  return (
    <div className="flex min-h-screen">
      <aside className="w-56 shrink-0 border-r border-rule bg-sheet">
        <div className="border-b border-rule px-4 py-3">
          <Link to="/" className="font-condensed text-lg font-semibold">Competition Fantasy</Link>
          <div className="text-xs text-ink-3">Admin</div>
        </div>
        <nav className="flex flex-col py-2 text-sm">
          {nav.map((n) => (
            <NavLink key={n.to} to={n.to} end={n.end} className={({ isActive }) => cx('px-4 py-2 hover:bg-paper', isActive ? 'bg-biro-soft font-medium text-biro' : 'text-ink-2')}>
              {n.label}
            </NavLink>
          ))}
        </nav>
        <div className="mt-auto border-t border-rule px-4 py-3 text-xs text-ink-3">
          <div>{auth.user()}</div>
          <button type="button" className="mt-1 text-biro hover:underline" onClick={() => { auth.clear(); location.href = '/' }}>Sign out</button>
        </div>
      </aside>
      <main className="min-w-0 flex-1 px-8 py-6">
        <Outlet />
      </main>
    </div>
  )
}
