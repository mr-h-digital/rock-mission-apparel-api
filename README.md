# Kingdom Drip — API

Backend for Kingdom Drip, Rock Mission Ministries' Christian streetwear store: product catalog, order creation,
PayFast checkout, and print-on-demand fulfillment handoff.

Serves [`rock-mission-apparel-web`](https://github.com/mr-h-digital/rock-mission-apparel-web).

## Architecture

A **modular monolith** — one Spring Boot deployable, with clean package boundaries by domain
(`product`, `order`, `payment.payfast`, `fulfillment`). At this store's scale (a single-team, non-profit
storefront), a microservices/event-driven split would add real operational cost (running Kafka, distributed
transaction handling, service discovery) without a corresponding benefit — a single Postgres transaction already
gives atomic "order created" guarantees that an async, multi-service design would have to rebuild. The module
boundaries here are deliberate so that if a piece (e.g. fulfillment) ever needs to become an independent service,
it can be extracted cleanly later — without paying that complexity tax now.

## Stack

- Spring Boot 3.5 / Java 21
- PostgreSQL + Flyway migrations
- JWT-based optional customer auth for account features

## Getting started

```bash
cp .env.example .env      # fill in DB credentials; PayFast defaults to their public sandbox test merchant
export $(cat .env | xargs)
mvn spring-boot:run
```

Requires a local Postgres database matching `DB_URL`/`DB_USER`/`DB_PASSWORD`. Flyway creates the schema and
seeds the product catalog on startup.

## API

- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me` — optional customer account auth
- `POST /api/auth/forgot-password`, `POST /api/auth/reset-password` — password recovery flow
- `POST /api/auth/forgot-username` — returns a generic account-recovery response (sign-in ID is email)

- `GET /api/products` / `GET /api/products/{id}` — catalog (seeded from `V2__seed_products.sql`, kept in sync
  with `src/data/products.js` in the storefront)
- `POST /api/orders` — creates an order from cart contents + customer details, **re-prices every line item from
  the server-side catalog** (never trusts client-submitted prices), and returns the PayFast hidden-form fields
  the frontend auto-submits to start checkout
- `POST /api/payfast/notify` — PayFast's ITN (Instant Transaction Notification) webhook. Verifies the request's
  signature, performs PayFast's required server-to-server validation round-trip, checks the paid amount against
  the order total, then marks the order `PAID` and hands it to `PrintfulService`

## Auth Recovery Config

- `AUTH_RESET_TOKEN_MINUTES` — reset token TTL in minutes (default `30`)
- `AUTH_EXPOSE_RESET_TOKEN` — include raw reset token/reset URL in forgot-password response for local testing (default `false`)
- `AUTH_RESET_BASE_URL` — base URL used when generating reset URL in exposed response mode (default `${FRONTEND_URL}/reset-password`)

## What's still needed before this can go live

1. **Real PayFast merchant credentials** for Rock Mission Ministries (`PAYFAST_MERCHANT_ID`,
   `PAYFAST_MERCHANT_KEY`, `PAYFAST_PASSPHRASE`), and `PAYFAST_SANDBOX=false` once ready. The current defaults
   are PayFast's published sandbox test-merchant credentials, fine for development only.
2. **A public `PAYFAST_NOTIFY_URL`** — PayFast must be able to reach this server's `/api/payfast/notify` over
   the internet, so this needs a real deployment (not `localhost`) before checkout can work end-to-end.
3. **A Printful account** (or other print-on-demand supplier), with the actual garment designs uploaded and each
   catalog product mapped to a Printful sync variant ID. `PrintfulService.submitOrder` is intentionally left as
   a logging stub (`printful.enabled=false` by default) because that mapping doesn't exist yet — guessing at
   variant IDs would silently create wrong orders once enabled.
4. **A production Postgres database** and hosting for this API (e.g. Railway/Render — something that runs a
   long-lived JVM process, unlike the static-hosted frontend).
