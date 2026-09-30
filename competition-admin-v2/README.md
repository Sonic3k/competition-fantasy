# competition-admin-v2

React 19 + TypeScript + Vite + Tailwind v4 front end for `competition-api-v2`.
One app: the public viewer at `/`, the admin area at `/admin`.

## Run

```bash
npm install
VITE_PROXY_TARGET=http://localhost:8080 npm run dev   # dev server on :5174, /api proxied to the API
npm run build                                         # tsc -b && vite build → dist/
```

## Deploy (Railway)

Service root: `competition-admin-v2`. `railway.toml` builds with nixpacks and serves `dist/` with `serve`.
Set at build time:

- `VITE_API_URL` — public URL of the API service, no trailing slash (e.g. `https://competition-api-v2.up.railway.app`).

The API must list this app's origin in `CORS_ORIGINS`.

## Layout

- `src/api` — DTO types, fetch client (token in `localStorage` as `cf.admin.token`), TanStack Query hooks
- `src/viewer` — public pages; `viewer/season` renders a stage from its type only (round robin → table + rounds or group grid, knockout → bracket, league phase → one table)
- `src/admin` — login, universes/nations/stadiums/teams/competitions CRUD, team profiles & kits, season workspace (teams, structure, matches, notebook tables, honours), presets, script manager, assets
