# competition-api-v2

Format-first rebuild of the Competition Fantasy API. Spring Boot 4.1 / Java 21 / PostgreSQL 16 / Flyway.
Requirements: `Competition Fantasy v2 — Requirements` (Claude Doc).

## Run locally

```bash
export PGHOST=localhost PGPORT=5432 PGDATABASE=competition_v2 PGUSER=postgres PGPASSWORD=postgres
export ADMIN_USERNAME=admin ADMIN_PASSWORD=change-me JWT_SECRET=$(openssl rand -hex 32)
./mvnw spring-boot:run        # or: mvn spring-boot:run
```

`GET /api/health` → `{"status":"ok"}`. Built-in format presets are loaded from `src/main/resources/presets/*.json` on startup.

## Environment (Railway service, root dir `competition-api-v2`)

| Variable | Purpose |
|---|---|
| `SPRING_DATASOURCE_URL` **or** `PGHOST/PGPORT/PGDATABASE/PGUSER/PGPASSWORD` | Postgres |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | single admin; password plain or BCrypt (`$2…`) |
| `JWT_SECRET` (≥32 chars), `JWT_TTL_HOURS` (default 72) | admin tokens |
| `CORS_ORIGINS` | comma-separated origins of the frontend |
| `B2_ENDPOINT`, `B2_REGION`, `B2_BUCKET`, `B2_KEY_ID`, `B2_APP_KEY` | Backblaze B2 (S3 API) |
| `STORAGE_PREFIX` (default `competition-fantasy/v2`), `CDN_BASE` | object key prefix and public CDN base |
| `IMPORT_DIR` (default `imports`) | folder the Script Manager reads JSON files from |

## Security

Public: every `GET /api/**` except `/api/admin/**`. Everything else needs `Authorization: Bearer <jwt>` from `POST /api/auth/login {username,password}`.

## Main endpoints

Public reads: `/api/universes`, `/api/universes/{key}/{nations|stadiums|teams|competitions}`, `/api/nations/{id}`, `/api/teams/{id}`,
`/api/competitions/{id}`, `/api/seasons/{id}` (full season view: stages → groups/rounds/matches, knockout rounds → ties, calculated + recorded tables, honours),
`/api/stage-groups/{id}/table?upToRound=N`, `/api/matches/{id}`, `/api/teams/{a}/head-to-head/{b}`, `/api/presets`, `/api/assets`.

Admin (`/api/admin/...`): CRUD for universes, nations, stadiums, teams (+profiles, kits), competitions, seasons (create from `presetKey` or explicit `format`; `scaffold` builds stages/groups/rounds from the format), stages, groups, group teams, rounds, knockout rounds, ties, matches (`PUT /matches/{id}/result` recalculates the table and resolves the tie), recorded standings, honours, presets, assets (upload + attach to slots), and the Script Manager.

## Script Manager (data loading)

1. Write a JSON file that follows `imports/schema/v1.json` (see `imports/examples/example-universe.json`) and commit it under `imports/`.
2. `POST /api/admin/imports/run {"file":"othelose.json","dryRun":true}` — executes everything, then rolls back, and returns counts, warnings (team plays twice in a round, group size ≠ format, unknown stadium…) and errors (unknown team names, missing keys).
3. Run again with `dryRun:false`. Seasons are replaced as a whole; universes, nations, teams, competitions are upserted by key. A run can be reverted (`POST /api/admin/imports/runs/{id}/revert`) which deletes what it created.

## Format definition

A season stores a resolved copy of its `FormatDefinition` (`format/model`). Stages are `ROUND_ROBIN`, `KNOCKOUT`, `LEAGUE_PHASE` or `SINGLE_MATCH`; each carries tie-breakers, zones, entry rules, legs, deciders, carry-over. The viewer renders by stage type, so a new real-world format is a new preset JSON, not new code.

## Tests

`mvn test` — standings calculator (FIFA vs UEFA order, recursive head-to-head, adjustments), tie resolver (aggregate, away goals, penalties), preset catalog validity.
