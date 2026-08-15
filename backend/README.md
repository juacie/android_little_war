# Backend (planned — not yet implemented)

Not started. Milestone 001 is a fully offline, client-only battle prototype by design (see
[Game_Project_GDD.md](../Game_Project_GDD.md) §4) — a backend isn't needed until formation/battle
results need to be server-validated.

## Planned stack

- **Kotlin + Ktor**, not the Node.js/TypeScript originally sketched — so the server can depend on
  `:battle-engine` directly instead of re-implementing the same combat formulas in a second
  language. This is the single biggest lever for keeping client display and server authority from
  ever disagreeing (see GDD §13).
- **PostgreSQL**, likely via Supabase or Neon's free tier to start (see GDD §15) — cheapest path
  with no infrastructure to babysit for a solo-maintained project.
- Local dev via `docker-compose` (Ktor app + Postgres), before anything touches real cloud infra.

## Why nothing is built yet

Building this now would be scope creep against the GDD's own sequencing: prove the battle is fun
first (Milestone 001), then build the account/economy/anti-cheat layer that only matters once
there's something worth protecting. Revisit after Milestone 001 playtesting.

## When this gets built, in order

1. `docker-compose.yml` with Ktor + Postgres for local dev
2. Auth + Player profile
3. `POST /api/v1/battles` — accepts a formation, runs `:battle-engine` server-side, returns the
   authoritative `BattleResult`
4. Everything else in GDD §16 (Roadmap)

Actual deployment to a live cloud environment requires account credentials (GCP/Supabase/etc.) that
only you can provide — this repo will have the Dockerfile and deploy config ready, but the actual
`gcloud`/`flyctl`/等 login and deploy step needs to happen with you present.
