# Competition Fantasy

A platform to manage and showcase fantasy sports competition universes — leagues, cups, world cups, and more.

## Structure

```
competition-api/      Spring Boot 3 + PostgreSQL (REST API)
competition-admin/    React + Vite (Admin panel)
competition-web/      React + Vite (Public site - Phase 2+)
```

## Local Development

### Prerequisites
- Java 21
- Node.js 20+
- Docker (for PostgreSQL)

### Start database
```bash
docker compose up -d
```

### Start API
```bash
cd competition-api
./mvnw spring-boot:run
```
API runs at http://localhost:8080

### Start Admin
```bash
cd competition-admin
cp .env.example .env
npm install
npm run dev
```
Admin runs at http://localhost:5173

## Deploy (Railway)
- API service: root directory = `competition-api/`
- Admin service: root directory = `competition-admin/`, Dockerfile deploy
- Add PostgreSQL plugin, Railway auto-injects `DATABASE_URL`
- Set `VITE_API_URL` build arg on admin service to point to API URL
