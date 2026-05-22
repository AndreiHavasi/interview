# Interview Prep Platform — Phase 0 scaffold

Three projects in this repo:

```
backend/          Spring Boot 3.x app (Java 21, Maven)  → :8080
sandbox-runner/   Spring Boot service (Phase 2 stub)    → :8081
frontend/         React + Vite + TS + Tailwind          → :5173
content/          Markdown content loaded into DB on startup
docker-compose.yml
```

## Run with Docker (recommended)

```bash
docker compose up --build
```

Then open http://localhost:5173 and run the frontend in dev mode separately (the compose file does not yet build the frontend — see below).

## Run locally for development

In three terminals:

```bash
# 1. Postgres only
docker compose up postgres

# 2. Backend (loads ../content on startup)
cd backend && mvn spring-boot:run

# 3. Sandbox runner
cd sandbox-runner && mvn spring-boot:run

# 4. Frontend
cd frontend && npm install && npm run dev
```

Open http://localhost:5173.

## End-to-end check

1. Start the backend — logs should show "Content load done: added=N updated=0".
2. Visit http://localhost:5173 — sidebar shows the topic tree, click an entry, markdown renders.
3. Toggle EN/RO in the header. Entries without an `ro.md` show a fallback banner.
4. Edit `content/java-core/static-keyword/en.md`, then click "Reload content" in the header (or `curl -X POST http://localhost:8080/admin/reload-content`). Refresh — new content shown.

## Endpoints

- `GET /api/health`
- `GET /api/topics` — full topic tree with entries
- `GET /api/topics/by-path/{slug...}?locale=en|ro` — single entry (slug includes slashes)
- `POST /admin/reload-content` — re-scan `content/`
- `GET /actuator/health`

## Next phases

See `interview_platform_plan.md`. Phase 1 layers more content. Phase 2 replaces the
sandbox-runner stub with real Docker-in-Docker execution. Phase 3 wires up the AI
mock interview via `WebClient`. Phase 4 adds Security/Kafka/etc.
