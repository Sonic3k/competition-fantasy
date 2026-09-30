import { Link, Outlet, useLocation } from 'react-router'
import { auth } from '@/api/client'

export function ViewerLayout() {
  const { pathname } = useLocation()
  return (
    <div className="min-h-screen">
      <header className="border-b border-rule bg-sheet/90 backdrop-blur-sm">
        <div className="mx-auto flex h-12 max-w-6xl items-center justify-between px-4">
          <Link to="/" className="font-condensed text-lg font-semibold tracking-tight text-ink">
            Competition Fantasy
          </Link>
          <nav className="flex items-center gap-4 text-sm text-ink-2">
            <Link to="/" className={pathname === '/' ? 'text-ink' : 'hover:text-ink'}>Universes</Link>
            <Link to="/admin" className="rounded-sm border border-rule px-2 py-1 hover:border-ink-3">{auth.loggedIn() ? 'Admin' : 'Sign in'}</Link>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
