# REST + Cross‑System Metadata Headers — Director‑Level Prep Pack

This is a one‑stop, director‑level guide to design and govern RESTful systems and cross‑system metadata headers across Java (Spring) and .NET (ASP.NET Core). It’s written to help you **whiteboard architecture**, **standardize headers**, and **propose enforceable governance** without writing code in the interview.

---

## 0) What success looks like in your on‑site interview

- You can **whiteboard** a clean REST architecture (client → API gateway → microservices → data/async), call out **governance guardrails**, and explain **how headers propagate** across Java + .NET.
- You propose a **small, opinionated enterprise standard**: versioning, error envelope, pagination, security, and a **firm‑wide header contract** (W3C Trace Context + minimal business headers).
- You show **benefits vs risks** and **how to enforce via the platform** (service templates, middleware, API gateway policies, CI checks, OpenAPI linting).
- You anchor with **measurable outcomes** (MTTR ↓, adoption %, audit exceptions ↓, dev NPS ↑).

---

## 1) REST API — principles that scale in the enterprise

### 1.1 Core REST principles
- **Resource‑oriented modeling**: `/bookings/{id}`, `/customers/{id}/trips` (avoid verbed URLs).
- **Uniform interface**: HTTP methods (`GET/POST/PUT/PATCH/DELETE`), status codes, content negotiation.
- **Statelessness**: every request carries context → enables horizontal scaling and resilience.
- **Cacheability**: use `ETag`, `Cache‑Control` for safe GETs; prefer server‑side caches internally.
- **Contract‑first**: **OpenAPI** spec is source of truth → codegen, docs, mocks, tests derive from it.
- **Observability & security baked‑in**: tracing, metrics, auth, rate limits from day 0.

### 1.2 Versioning
- Prefer **URI versioning**: `/v1/bookings`.
- Alternative: **media‑type** or **header** versioning for internal APIs.
- Rule: never break old clients without a deprecation plan and dates.

### 1.3 Status code palette (tight and predictable)
- 2xx: `200 OK`, `201 Created`, `204 No Content`
- 4xx: `400` bad request, `401/403` auth failures, `404` not found, `409` conflict, `422` validation, `429` throttled
- 5xx: `500` internal, `502/503` upstream or maintenance

### 1.4 Error model (standardize it!)
Adopt **RFC 7807 Problem+JSON** or a uniform envelope:
```json
{
  "type": "https://errors.company.com/validation",
  "title": "Validation failed",
  "status": 422,
  "traceId": "a8b4e911-...",
  "errors": [
    {"code": "BOOKING_DATE_PAST", "field": "travelDate", "message": "Date must be in the future"}
  ]
}
```
**Always include** `traceId` to link support, logs, and APM.

### 1.5 Pagination, sorting, filtering
- Prefer **cursor‑based pagination** (`nextPageToken`) for scale; avoid deep offsets.
- Return `Link` headers or body metadata for next page; document limits and defaults.

### 1.6 Idempotency & retries
- For **unsafe** operations (e.g., `POST /payments`), require **`Idempotency-Key`** header and store request hash with TTL to dedupe.
- Client retries: **exponential backoff + jitter**; combine with server idempotency.

### 1.7 Caching & conditional requests
- Use `ETag`/`If‑None‑Match` for GETs to reduce bandwidth.
- For internal traffic, prefer edge caches + ETag at gateway/service mesh.

---

## 2) Cross‑System Metadata Headers — the enterprise contract

### 2.1 Why standardize headers?
- **End‑to‑end traceability** for SRE and incident response.
- **Auditability** for who/what/when across services.
- **Interoperability** so Java ↔ .NET speak the same language.
- **Security posture** via consistent auth/claims transport.
- **Business analytics & routing** via tenant/client/channel context.

### 2.2 Recommended golden set (minimal, enforceable)

**(A) Tracing / correlation (W3C standards)**  
- `traceparent` — global trace/span propagation (MUST)  
- `tracestate` — vendor‑specific trace info (SHOULD)  
- `baggage` — small key/values carried with the request (SHOULD; keep tiny)

**(B) Human‑friendly correlation (optional but useful)**  
- `X-Request-Id` or `X-Correlation-Id` — mirrors the trace id for tickets/chats (derive from `traceparent`).

**(C) Identity / client context**  
- `Authorization: Bearer <JWT>` (OAuth2/OIDC).  
- `X-Client-Id` (calling app id) — optional if not present in JWT claims.  
- `X-Tenant-Id` — only if not modeled as a JWT claim.  
> **Guideline:** Prefer **JWT claims** for identity/PII rather than custom headers.

**(D) Reliability & safety**  
- `Idempotency-Key` — dedupe unsafe POST/PUT/PATCH.  
- `X-Request-Timestamp` — ISO‑8601 for replay detection/time drift (optional).

**(E) Governance & transport hints (optional)**  
- `X-Channel` (web/app/partner)  
- `X-Data-Classification` (public/internal/confidential) → drives log redaction

> **Director take:** Standardize on **W3C Trace Context** + a **minimal business header set**. Push identity and PII into **JWT claims**, not free‑form headers.

### 2.3 Trust boundary & propagation policy
- **API Gateway is the trust boundary**:  
  - **Validates** inbound headers and **rewrites** as needed.  
  - **Whitelist‑forwards** only approved headers to internal services.  
  - **Issues** or **normalizes** `traceparent`.  
  - **Validates** `Authorization` (JWT/mTLS) and injects verified identity downstream (claims).  
- **Services**: consume `traceparent|tracestate|baggage`, **never trust caller‑provided identity headers** outside the token.  
- **Async** (Kafka/Solace): propagate `traceparent` and business correlation in **message headers**; resume span on consumption.

### 2.4 Benefits you should articulate
- **Incident triage** is dramatically faster: search by `traceId` across logs/services.  
- **Unified audit** and easier compliance evidence.  
- **Safe retries** with idempotency.  
- **Platform governance**: the gateway enforces schema, size limits, and redaction.

### 2.5 Risks & mitigations
| Risk | What can go wrong | Mitigation |
|---|---|---|
| **Spoofing** | Clients forge `X-User-Id`/`traceparent` | Strip at edge; reissue `traceparent`; trust identity only via JWT/mTLS |
| **PII leakage** | PII in headers/logs | Keep PII in JWT; classify and **redact** logs; limit baggage |
| **Header bloat** | Oversized headers break proxies | Allow‑list + per‑header size caps; minimal baggage |
| **Propagation gaps** | Lost correlation on async hops | Standard message header mapping; libs for Java + .NET |
| **Stack drift** | Java/.NET behave differently | One firm spec; shared libraries; CI conformance tests |

---

## 3) Unifying Java & .NET — the platform approach

### 3.1 Enterprise API contract (one‑pager spec)
- **Headers**: require W3C `traceparent`; allow `tracestate`, `baggage`; whitelist `Authorization`, `Idempotency-Key`, `X-Request-Id`; strongly prefer JWT claims for identity.  
- **Error**: adopt **Problem+JSON** with `traceId`; no stack traces to clients.  
- **OpenAPI**: style guide (naming, status code map, error schema, pagination model, security schemes).  
- **SLOs**: latency budgets by endpoint class; 429 behavior; retry semantics.

### 3.2 Enforcement — make the right path the easy path
- **API Gateway policies**: header allow‑list, size limits, JWT validation, rate limits, error normalization.  
- **Language SDKs**:  
  - **Spring Boot starter** (existing): filters for tracing/auth/logging/error model.  
  - **ASP.NET Core NuGet** (to build): middleware for trace propagation, auth, idempotency, error envelope, structured logging.  
- **Service templates**: `dotnet new jpmc-api` & Spring archetype; both pre‑wired to the standard.  
- **CI Conformance**: OpenAPI linting, synthetic header tests, static checks for banned headers/PII.

### 3.3 Metrics & outcomes
- **Adoption**: ≥ 90% services on standard template/pipeline.  
- **Reliability**: MTTR ↓ 50%; zero repeat Sev‑1 root causes due to missing trace/correlation.  
- **Compliance**: zero repeat audit findings; evidence auto‑generated from pipeline/gateway logs.  
- **Developer Experience**: NPS > 60; service bootstrap time ↓ from weeks → hours.

---

## 4) Whiteboard reference designs (talk track)

### 4.1 Synchronous call with idempotency + tracing
```
Client
  -> [API Gateway]
      - Validates & normalizes headers (traceparent/JWT)
      - Enforces allow‑list, rate limits, size caps
    -> Service A (.NET, ASP.NET Core middleware)
      - Propagates trace, validates Idempotency-Key, structured logs
      -> Service B (Java, Spring filter)
         - Continues tracing, auth context via JWT claims
      <- Error? Return Problem+JSON + traceId
```
**Narrate:** where trace is created/validated; how idempotency is enforced; how errors map to the standard envelope.

### 4.2 Async event correlation (Kafka/Solace)
- Producer adds **`traceparent`** & **`business-correlation-id`** to message headers.  
- Consumer resumes span from `traceparent`; logs both `traceId` & business id; emits metrics.  
- **Benefit:** joins async + sync paths in APM; preserves audit chain.

### 4.3 Error standardization at the edge
- Upstream quirks (non‑Problem+JSON) are **normalized** by gateway policy before returning to clients.  
- Ensures consistent developer & operator experience.

---

## 5) Governance artifacts to propose (fast wins)

- **API Header Spec (v1)** — required/optional headers, allowed char sets, max sizes, trust‑boundary rules.
- **Error & Status Code Guide (v1)** — scenario→status mapping + Problem+JSON template.
- **OpenAPI Style Guide** — tags, operationId, error schema, pagination, security.
- **.NET Platform SDK** — ASP.NET Core middleware package (trace, auth, idempotency, errors, logging).
- **Paritization Table** — Spring starter ↔ ASP.NET Core middleware feature parity.
- **CI Conformance Pack** — OpenAPI linter, header conformance tests, PII/banned‑header scans.
- **Dashboards** — adoption %, header coverage %, error envelope coverage, MTTR, 429 rates, retry success.

---

## 6) Talking points (executive tone)

- “We standardize on **W3C Trace Context** for cross‑stack interoperability and keep enterprise business headers **minimal**.”  
- “**API gateway is the trust boundary**; it rewrites and enforces header policy. Services must not trust external identity outside the token.”  
- “PII stays out of headers — **use JWT claims** and log redaction by **data classification**.”  
- “We ship parity SDKs: **Spring starter** and **ASP.NET Core middleware**, enforced by **gateway policy + CI conformance**.”  
- “Success is adoption and outcomes: **≥90% template usage, MTTR ↓ 50%, zero repeat audit findings, dev NPS > 60**.”

---

## 7) Rapid FAQ

**Q: Why both W3C `traceparent` and `X-Request-Id`?**  
A: `traceparent` is the standard for tooling; `X-Request-Id` is a user‑friendly correlation value for tickets/chats. Keep them aligned (derive the latter from the trace id).

**Q: Where is correlation generated?**  
A: At the **edge**. External values are mapped/sanitized; internal propagation is trusted and enforced by shared libs.

**Q: Can teams add custom headers?**  
A: Only via review + registry. Prefer JWT claims or body fields to avoid header sprawl and leakage.

**Q: How do we handle retries safely?**  
A: Client retries with backoff + **server idempotency** verified by `Idempotency-Key`; dedupe for a TTL.

**Q: How do we prove adoption?**  
A: CI conformance tests + runtime telemetry (percent of requests with valid trace headers; error envelope coverage; missing‑header alerts).

---

## 8) 15‑minute whiteboard script (practice)

1) Draw **Client → Gateway → Service A (.NET) → Service B (Java)**.  
2) Mark headers at each hop: `traceparent`, `Authorization`, `Idempotency-Key`, and the allow‑list.  
3) Explain **trust boundary** and **header normalization** at the gateway.  
4) Walk the **ASP.NET Core middleware** stack: tracing → auth → idempotency → error mapping → logging.  
5) Mention **OpenAPI + CI conformance** and the **platform SDKs** (Spring starter/.NET NuGet).  
6) Close with **KPIs** and **adoption plan** (pilot → document → scale).

---

## 9) Actionable checklist

- [ ] Choose **W3C Trace Context** + minimal business headers (Spec v1).  
- [ ] Define **Problem+JSON** error envelope with `traceId`.  
- [ ] Publish **OpenAPI style guide** + linter rules.  
- [ ] Build **ASP.NET Core middleware** (trace, auth, idempotency, error, logging).  
- [ ] Map **Spring starter parity** and document equivalences.  
- [ ] Enforce at **API gateway** (allow‑list, size limits, JWT, rate limits, normalization).  
- [ ] Add **CI conformance** + runtime dashboards.  
- [ ] Agree on **KPIs** (adoption %, MTTR, audit repeats, dev NPS).

---

### Appendix A — Example header policy (human‑readable)
```
Required:   traceparent, Authorization
Allowed:    tracestate, baggage, X-Request-Id, Idempotency-Key, X-Client-Id, X-Tenant-Id
Rejected:   Any PII headers; unknown prefixes
Size caps:  All headers combined <= 8 KB; baggage <= 512 B
Trust:      External traceparent accepted but normalized; identity only from JWT/mTLS
Logging:    Redact Authorization; hash Idempotency-Key; never log PII
```

### Appendix B — Error/status mapping (starter)
| Scenario | Status | Notes |
|---|---:|---|
| Created resource | 201 | Return `Location` |
| No content on success | 204 | For deletes/updates |
| Validation failure | 422 | Problem+JSON; include field errors |
| Auth failure | 401/403 | 401 unauthenticated; 403 unauthorized |
| Rate limited | 429 | Include `Retry-After` |
| Upstream down | 503 | With retry hint |
| Unknown error | 500 | Include `traceId`; no stack trace |

### Appendix C — OpenAPI style snippets
- **Security**: bearer auth scheme required for protected routes.  
- **Errors**: `$ref` to a single Problem+JSON schema.  
- **Pagination**: cursor model with `nextPageToken` property.  
- **operationId**: `{resource}_{verb}_{qualifier}` (unique, stable).

---

**Use this pack to rehearse a concise, confident whiteboard session.** Focus on:  
- the **header contract**,  
- the **trust boundary at the gateway**,  
- the **parity SDKs** (Spring + .NET), and  
- the **enforcement + metrics** that prove adoption and value.
