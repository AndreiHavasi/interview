# Java & Spring Boot Interview Prep Platform — Build Plan

A personal learning tool covering Java core, Spring Boot, architecture, databases, threads, security, and the rest of the topics in your `All_Java_Core.md` checklist — with theory, runnable coding challenges, an AI mock-interview simulator, and a Romania-market overlay.

---

## 1. Guiding principles

- **You're the only user.** No signup, no roles, no email flows. Single local profile.
- **The app is also study material.** Build it in Spring Boot so the codebase itself doubles as a reference — every topic on your list (Beans, `@Transactional`, JPA relations, Spring Security, Kafka, etc.) should appear *in the implementation*, not just in the content.
- **Three subsystems, one app.** Theory/content, coding challenges (sandboxed execution), AI mock interview. They share a topic taxonomy but are otherwise independent.
- **Romania overlay, not a separate app.** RO-specific content (common questions at Romanian companies, salary bands, typical interview structure at EPAM/Endava/Luxoft/Bitdefender/Cognizant/local banks) lives as tags and supplementary notes on existing topics.

---

## 2. Recommended stack

| Layer | Choice | Why |
|---|---|---|
| Backend | **Spring Boot 3.x (Java 21)** | Virtual threads + records + the topic you're studying |
| Persistence | **PostgreSQL** + Flyway + Spring Data JPA | Lets you practice JPA relations, indexes, Flyway migrations — all on your list |
| Search | **PostgreSQL full-text** to start, **Elasticsearch** later | Defer ES until you have content worth searching; then swap in to study it |
| Frontend | **React + Vite + TypeScript + Tailwind** | Fast to build, clean separation, looks like a real product |
| Code execution | **Docker-in-Docker sandbox** (one short-lived container per submission) | Forces you to learn Docker properly — also on your list |
| AI mock interview | **Anthropic API** (Claude) via Spring `WebClient` | Practices `WebClient` (also on your list) and gives you a real reason to use streaming |
| Auth | **None initially.** Add Spring Security + JWT in Phase 4 as a learning exercise | You study Spring Security by *building* it, not bolting it on early |
| Async / messaging | **Spring Events** first, then **Kafka** in Phase 4 | Kafka is on your list — wire it in for mock-interview transcripts/event log |
| Containerization | **Docker Compose** for local (Postgres + app + sandbox runner) | Mirrors real dev setup |
| Observability | **Actuator + Micrometer + simple `/metrics`** | Cheap, real, mentionable in interviews |

The stack is deliberately self-referential: every layer maps to a topic in your checklist.

---

## 3. Topic taxonomy (mapped from your file)

Every piece of content slots into this hierarchy. Use the same taxonomy for theory entries, challenges, and mock-interview question banks.

```
java-core/
  language-basics/        static, final, access-modifiers, method-hiding
  oop/                    4 pillars, SOLID, ACID
  memory/                 jvm-jre-jdk, stack-heap, gc, string-pool
  exceptions/             checked-vs-unchecked, try-with-resources, finally
  collections/            list, map (hashmap internals), set, iterator
  streams-functional/     lambdas, terminal/non-terminal, functional interface
  immutability-caching/   immutable classes, lru-cache
  threads/                basics, thread-vs-runnable, executor, completable-future,
                          virtual-threads, cyclic-barrier, countdown-latch,
                          optimistic-vs-pessimistic-lock, notify

spring-boot/
  core/                   ioc, di, bean-scopes, lifecycle, application-context,
                          bean-factory, bean-post-processor
  annotations/            @SpringBootApplication, @Service/@Repository/@Component,
                          @Controller vs @RestController, @Transactional (proxy)
  data/                   jpa-vs-crud-repo, entity-relations, hibernate-fetch-types,
                          embeddable-vs-embedded, flyway
  events-async/           ApplicationEvent, @EventListener, @TransactionalEventListener,
                          @Scheduled (cron)
  web/                    rest-vs-soap, http-methods, webclient-vs-resttemplate,
                          servlet-context, tomcat
  security/               oauth2, jwt-structure, spring-security-architecture, m2m-token

architectures/            event-driven, mvc, monolith-vs-microservices,
                          load-balancer, reverse-proxy, api-gateway,
                          mvc-vs-mvp-vs-mvvm

design-patterns/          singleton, factory, strategy, template-method,
                          adapter, circuit-breaker

databases/                relational-vs-nosql, postgres-vs-mongo, indexes,
                          joins, select-internals, two-phase-commit, why-elastic

messaging/                kafka-basics (partitions, consumer groups), rabbitmq,
                          kafka-vs-rabbitmq, rest-vs-messaging

devops-infra/             docker-vs-vm, kubernetes-high-level, ci-cd,
                          tls-ssl, cors, rest-vs-grpc, http-protocol

testing/                  unit, integration, e2e, mock-vs-spy

non-functional/           scalability, availability, consistency, latency, etc.

ro-market/                ro-companies, ro-interview-format, ro-salary-bands,
                          ro-common-questions  (cross-tag, not a silo)
```

Romanian-market notes attach as **tags** to existing topics (e.g. "asked frequently at Endava", "common follow-up at Bitdefender") rather than living in their own section. That way one piece of content serves both general study and RO-specific prep.

---

## 4. Data model (first cut)

```
Topic              id, slug, parent_id, taxonomy_path, difficulty (1-5)
TheoryEntry        id, topic_id, slug, sources_json, updated_at
TheoryEntryBody    id, theory_entry_id, locale (EN|RO),
                   title, body_md, updated_at
                   UNIQUE(theory_entry_id, locale)

Challenge          id, topic_id, slug, language (JAVA), difficulty,
                   starter_code, reference_solution, updated_at
ChallengeBody      id, challenge_id, locale, title, prompt_md
                   UNIQUE(challenge_id, locale)
TestCase           id, challenge_id, input, expected_output, hidden (bool)
Submission         id, challenge_id, code, status, stdout, stderr,
                   runtime_ms, submitted_at

MockSession        id, topic_filter[], locale, started_at, ended_at,
                   score, tokens_used, provider (claude|ollama|scripted),
                   transcript_id
Transcript         id, mock_session_id, messages_jsonb

Tag                id, name        (e.g. "ro-endava", "frequently-asked")
TopicTag           topic_id, tag_id

StudyLog           id, topic_id, action (read|attempted|solved|mock-passed),
                   timestamp, notes
```

`StudyLog` is your progress tracker — even without spaced repetition (you said no), it lets you see what you've touched and when. The `*Body` split keeps the EN/RO content separate so adding RO later is a pure insert, no migration.

---

## 5. Feature breakdown

### 5.1 Theory module
- Markdown content stored in DB, rendered with syntax highlighting on the frontend.
- Each entry: explanation, code examples, "common interview traps", optional RO addendum.
- Cross-links between related entries (e.g. `@Transactional` → proxy pattern → bean lifecycle).
- Seed initial content from your existing markdown links — you can copy your own notes in over time; don't try to write all of it up front.

### 5.2 Coding challenges (the hard part)
- Browser editor (Monaco) → submit Java code → backend compiles + runs in a **disposable Docker container** with CPU/memory/time limits → returns stdout/stderr + test-case results.
- Start with `javac` + `java` in a minimal container; no build tool inside the sandbox.
- Test cases run by feeding stdin and diffing stdout (simplest model). Add JUnit-based grading later if you want.
- **Security**: container has no network, read-only filesystem except `/tmp`, killed after N seconds, ulimits on memory/processes. Treat the sandbox runner as untrusted even though it's just you — good practice.
- Seed challenges around concrete topics: "implement an LRU cache", "write a thread-safe singleton", "make this class immutable", "fix the deadlock", "implement custom Comparator", "build a simple `@Transactional`-style proxy with dynamic proxies".

### 5.3 AI mock interview
- Pick a topic filter (e.g. "Spring Boot core + JPA") and difficulty.
- Backend opens a streaming session with Claude using a system prompt that defines: persona (senior Java engineer interviewer at a Romanian company), question style, follow-up depth, scoring rubric.
- Frontend shows messages as they stream in. You answer in text (or paste code).
- At session end, the model produces a structured evaluation: score per topic touched, gaps, suggested theory entries to revisit (linked back to your topic taxonomy).
- Store full transcript so you can re-read past mocks.
- This is where `WebClient` + reactive streaming + a long-running async flow naturally fits.

### 5.4 Romania-market overlay
- Curated list of ~20–30 Romanian employers hiring Java/Spring (EPAM, Endava, Luxoft, Bitdefender, Cognizant Softvision, Crystal System, Accenture RO, Stefanini, plus banks like ING Hubs, Raiffeisen, BRD, fintechs).
- For each: typical interview format (HR → tech → system design → final), languages used in tech round (EN/RO), things they emphasize.
- Tag content frequently asked at each. Filterable view: "show me only what's commonly asked at Endava".
- This data is curated by you over time — the platform just gives you the structure.

### 5.5 Progress (lightweight)
You opted out of spaced repetition, so keep it minimal: a per-topic "last touched", "ever attempted", "ever solved" view. A heatmap calendar of activity is enough.

---

## 6. Build order (phases)

**Phase 0 — scaffold (1 evening)**
Spring Boot project, Postgres in Docker Compose, Flyway baseline migration, React app talking to a `/health` endpoint. Done.

**Phase 1 — theory module (1 week)**
CRUD for topics + theory entries via JPA. Markdown editor on the frontend (or just edit in DB and view-only on frontend — your call). Seed 10 entries on topics you know best. Goal: you're already using the app to study within a week.

**Phase 2 — coding challenges (2–3 weeks, the meaty one)**
- 2a: Sandbox runner as a separate Spring Boot service that takes `{code, stdin}` and returns `{stdout, stderr, exitCode, timeMs}`. Talks to the main app over REST. This separation is *itself* a microservices study exercise.
- 2b: Challenge UI with Monaco, test-case display, submission history.
- 2c: Seed 15–20 challenges covering threads, collections, immutability, design patterns.

**Phase 3 — AI mock interview (1 week)**
`WebClient`-based streaming integration with Claude API, transcript storage, structured-evaluation parsing. Wire `@TransactionalEventListener` to fire a `MockSessionCompleted` event that updates `StudyLog` — practical use of two checklist topics in one feature.

**Phase 4 — "study by building" features (ongoing)**
Each of these adds value *and* exercises a checklist item:
- Add Spring Security + JWT (study OAuth2/JWT structure)
- Add Kafka for mock-session events → consumer that writes to a denormalized read model in Elasticsearch (study Kafka, ES, CQRS-lite)
- Add Actuator + custom health checks
- Add a `@Scheduled` job that recomputes "weakest topics" weekly
- Add a circuit breaker (Resilience4j) around the Claude call
- Migrate one heavy endpoint to virtual threads, benchmark vs platform threads
- Add Testcontainers-based integration tests

**Phase 5 — Romania overlay (1 weekend)**
Seed company data, add tag filtering, write the "interview format per company" notes. Mostly content work, low code.

---

## 7. What this gets you

By the time the platform is feature-complete, you've personally written code touching: JPA relations, `@Transactional` propagation, Spring Events, `@TransactionalEventListener`, `@Scheduled`, `WebClient` (streaming), `RestTemplate` (one legacy endpoint for comparison), Spring Security with JWT, OAuth2 client, Flyway, Postgres indexes, Elasticsearch, Kafka producers/consumers, Docker, Docker Compose, executor services, virtual threads, circuit breaker, and a handful of GoF patterns. Plus you have a tool that drills you on the theory.

That's roughly 80% of your checklist as *built artifacts* you can talk about in interviews — not just things you read articles about.

---

## 8. Resolved decisions & their consequences

### 8.1 Localhost only
- Skip nginx, HTTPS, domain setup, production Postgres tuning. `docker compose up` is the deployment story.
- CORS config can be permissive (`localhost:5173` → `localhost:8080`).
- No secrets manager — `.env` file in `.gitignore` is fine.
- One Postgres instance, one app, one sandbox runner, one optional Elasticsearch container — everything in one `docker-compose.yml`.
- **Don't** skip Docker Compose just because it's local; you want to study Docker anyway, and it makes the whole thing reproducible if you nuke your machine.

### 8.2 Content authoring: file-based markdown loaded into DB
Decision: **markdown files in a `content/` folder, loaded into the DB on app startup via a `CommandLineRunner`**. Reasons:

- Versionable in Git alongside the code — your study notes become part of the project history.
- No CRUD UI to build → cuts ~3 days off Phase 1.
- You can edit in your IDE with proper markdown preview, not a half-baked in-browser editor.
- Reload trigger: a `POST /admin/reload-content` endpoint that re-scans the folder. Cheap to build.

Folder layout:

```
content/
  java-core/
    static-keyword/
      en.md
      ro.md           (optional; falls back to en.md if missing)
      meta.yaml       (difficulty, tags, ro-companies, related-topics)
    final-keyword/
      en.md
      meta.yaml
  spring-boot/
    transactional/
      en.md
      ro.md
      meta.yaml
  ...
```

`meta.yaml` per entry:
```yaml
title: "@Transactional and the Proxy Pattern"
difficulty: 4
tags: [spring-core, aop, frequently-asked]
ro_companies: [endava, luxoft, ing-hubs]
related: [spring-boot/bean-lifecycle, design-patterns/proxy]
sources:
  - https://medium.com/...
```

Loader behavior: scan folder → upsert each entry by `slug` (= folder path) → log diff (added / updated / unchanged). Idempotent.

### 8.3 AI module is optional and pluggable
Decision: **define an `InterviewerClient` interface** in the domain layer; provide implementations:

```
InterviewerClient (interface)
 ├── ClaudeInterviewerClient    (uses Anthropic API + WebClient)
 ├── OllamaInterviewerClient    (local model — added later)
 └── ScriptedInterviewerClient  (deterministic, for tests + offline mode)
```

Wire via Spring `@ConditionalOnProperty`:
```
interview.provider=claude   # or "ollama" or "scripted"
```

If `interview.provider` is unset or the API key is missing, the mock-interview UI shows a "configure an AI provider" panel instead of crashing. The whole AI feature becomes a switchable module — and incidentally this is a clean real-world example of the **Strategy pattern + Spring conditional beans**, both on your checklist.

Cost control on the Claude side: cap mock sessions at N messages, log token usage per session into `MockSession.tokens_used`, surface a running total on the dashboard so you see what you're spending.

### 8.4 Bilingual EN/RO from day one
Decision: **store both languages, lazily authored**. Bake it into the schema now even if you only write English first — retrofitting i18n later is painful.

Schema implications:

```
TheoryEntry        id, topic_id, slug, difficulty, ...
TheoryEntryBody    id, theory_entry_id, locale (EN|RO), title, body_md, updated_at
                   UNIQUE(theory_entry_id, locale)
```

Same pattern for `Challenge.prompt_md` (bodies in both languages, code is language-agnostic).

Frontend: a single locale toggle in the header (`EN | RO`), persisted in `localStorage`. If the selected locale's body is missing for an entry, show the EN version with a small "Romanian translation not yet written" banner.

UI chrome (buttons, labels, nav) goes through a tiny i18n layer — `react-i18next` is overkill for a personal tool, a plain `messages.en.json` / `messages.ro.json` and a `useT()` hook is enough.

Mock-interview prompts: the system prompt to Claude includes the user's locale, so the interviewer asks questions in RO if you're studying in RO. Useful because some Romanian companies do tech rounds in Romanian — practicing Java vocabulary in RO is a real edge.

---

## 9. Updated Phase 0 checklist

Given the four decisions above, here's exactly what Phase 0 looks like:

- [ ] `docker-compose.yml` with: postgres:16, the app (built from local Dockerfile), sandbox-runner (placeholder service), optional elasticsearch (commented out for later)
- [ ] Spring Boot 3.x project, Java 21, Gradle (or Maven — your preference)
- [ ] Flyway baseline migration: `topic`, `theory_entry`, `theory_entry_body`, `tag`, `topic_tag` tables
- [ ] `content/` folder in repo root with one example entry (`java-core/static-keyword/{en.md, ro.md, meta.yaml}`)
- [ ] `ContentLoader` (`CommandLineRunner`) that scans `content/`, parses YAML, upserts into DB
- [ ] `GET /api/topics` and `GET /api/topics/{slug}?locale=en` endpoints
- [ ] React + Vite + TS + Tailwind frontend, sidebar with topic tree, main pane renders markdown, header has EN/RO toggle
- [ ] One end-to-end flow works: edit `content/java-core/static-keyword/en.md` → `POST /admin/reload-content` → refresh browser → see new content

That's the foundation. Everything else (challenges, mock interviews, security, Kafka) layers on top without schema upheaval.
