Awesome—here’s your final, comprehensive technical/architectural question bank for SP (the Builder).
Each item has two parts:
	1.	Ramp-up (learn fast): a concise primer so you can level up quickly.
	2.	Director-level answer: what you’d actually say in the interview (tight, confident, outcome-oriented).

Use this as your single prep doc. No need to hunt elsewhere.

---

A. Cloud-Native .NET on AWS (Foundations)

1) Designing cloud-native .NET microservices on AWS

Ramp-up (learn fast)
	•	Package services in Docker; run on EKS (Kubernetes) or ECS/Fargate.
	•	12-factor: config via env vars/Parameter Store, stateless services, externalized state.
	•	Resilience: Polly for retries/timeouts/circuit breakers; health probes; readiness/liveness.
	•	Observability: OpenTelemetry → traces/metrics/logs; central log agg (CloudWatch/Elastic).
	•	Secrets: AWS Secrets Manager/SSM Parameter Store; IAM roles (no creds in code).
	•	Data: RDS/Aurora for relational; DynamoDB for key-value/ultra-scale; S3 for blobs.

Director-level answer
“I design .NET services as containerized, stateless microservices on EKS/ECS with 12-factor discipline. Polly enforces timeouts/retries/circuit breakers; OpenTelemetry standardizes traces/metrics/logs. Config and secrets come from SSM/Secrets Manager with least-privilege IAM. Data tier is workload-aligned (Aurora for ACID, DynamoDB for scale). The outcome is horizontal scalability, predictable resilience, and uniform observability across teams.”

---

2) When to choose EKS vs ECS vs Lambda for .NET

Ramp-up
	•	Lambda: event-driven, spiky, sub-ms to seconds work, no servers to manage.
	•	ECS/Fargate: simplest managed containers; good for most web APIs/batch.
	•	EKS: full Kubernetes control/flexibility, service mesh, multi-tenant platform, higher ops overhead.

Director-level answer
“I default to ECS/Fargate for most .NET APIs due to lower ops burden; EKS for platformized multi-tenant clusters needing mesh/policied multi-namespace isolation; Lambda for event-driven, bursty functions or glue code. We standardize guardrails so teams pick the lightest runtime that meets latency, cost, and ops needs.”

---

3) Multi-account architecture & landing zones

Ramp-up
	•	Use Organizations: separate prod/stage/dev accounts, centralized logging/security.
	•	Baselines: SCPs, IAM boundaries, VPC standards, auditing, KMS key policies.

Director-level answer
“We operate a multi-account landing zone: prod/stage/dev isolation, centralized logs, shared VPC patterns, and SCP-enforced guardrails. This reduces blast radius, simplifies auditing, and lets platform teams evolve baselines without blocking product teams.”

---

B. Infrastructure as Code (IaC)

4) Terraform vs CDK; policy enforcement

Ramp-up
	•	Terraform: declarative, ecosystem rich.
	•	CDK: imperative (TypeScript/Python/C#) synthesizes CloudFormation.
	•	Policy-as-code: OPA/Conftest, Checkov, Terraform Cloud policies.

Director-level answer
“We standardize on Terraform modules for infra primitives and allow CDK for teams that need language-level composition. Compliance is automated via OPA/Checkov in CI; non-compliant plans never merge. This yields repeatable, compliant infra at scale.”

---

5) Golden templates/boilerplates for new services

Ramp-up
	•	Repo templates: Dockerfile, Helm chart/Task def, OTEL agent, Serilog config, health endpoints, GitHub Actions.

Director-level answer
“New .NET services start from golden templates: Dockerfile, IaC, OTEL, logging, health probes, and security scans wired by default. This cuts service bootstrap to minutes and ensures ‘secure-by-default’.”

---

C. Networking, Mesh, APIs

6) API Gateway, versioning, rate limiting

Ramp-up
	•	Gateway responsibilities: routing, auth, rate-limit, WAF, consistent headers/correlation IDs.
	•	Versioning: semantic (v1/v2), deprecation windows, backward compatibility.

Director-level answer
“An enterprise API Gateway enforces auth, rate-limits, contract versioning, and consistent telemetry headers. We mandate backward-compatible changes and sunset policies to avoid consumer breakage. It’s the contract front door for Travel.”

---

7) Service mesh (Istio/App Mesh) value

Ramp-up
	•	Sidecar proxies implement mTLS, retries, traffic shaping, canaries, policy.
	•	Pros: uniform resiliency/security; Cons: complexity, cost.

Director-level answer
“We employ a service mesh where multi-tenant clusters and cross-team policies are needed: mTLS, retry budgets, canary/traffic shifting, and zero-trust networking out of the box. We don’t mesh by default—use it where policy/uniformity matter.”

---

D. Reliability & Performance Engineering

8) SLOs, error budgets, and incident practice

Ramp-up
	•	Define SLOs per critical journey (e.g., Search, Book).
	•	Error budgets drive release pace vs reliability work.
	•	Blameless postmortems, action items, hygiene metrics (MTTR).

Director-level answer
“Each user journey has SLOs (latency/availability). We manage release pace with error budgets, enforce blameless postmortems, and track MTTR. This aligns engineering with business impact and keeps us honest on reliability.”

---

9) Performance under peak traffic; capacity planning

Ramp-up
	•	RED/USE metrics, autoscaling (HPA), load testing (k6, Locust), synthetic checks.
	•	Caching/CDN, pre-computation, bulkheads, backpressure.

Director-level answer
“We benchmark with k6, monitor RED metrics, and enforce HPA with Pod/Task limits. We cache aggressively (edge + service), apply bulkheads/backpressure, and test surge scenarios before holidays. We publish capacity runbooks with autoscaling policies.”

---

10) Resilience patterns in .NET

Ramp-up
	•	Polly: retry w/ jitter, timeout, circuit breaker, fallback.
	•	Idempotency keys; dead-letter queues; outbox pattern.

Director-level answer
“We use Polly for timeout/retry/circuit breaker with jitter to avoid thundering herds. Critical writes are idempotent; async flows use DLQs and the outbox pattern for exactly-once semantics across boundaries.”

---

E. Data & Eventing

11) Event-driven architecture: Kafka/SNS/SQS/Solace

Ramp-up
	•	Event types: domain events vs commands.
	•	Exactly-once is hard; aim for at-least-once + idempotency.
	•	Schema evolution: Avro/JSON Schema + registry.

Director-level answer
“We separate domain events from commands, design for at-least-once with idempotency, and manage schemas via a registry. For Travel, this decouples search, pricing, booking, and downstream analytics while preserving autonomy.”

---

12) Eventual consistency & sagas

Ramp-up
	•	Distributed transactions handled via sagas: orchestrated or choreographed.
	•	Compensating actions for failed steps.

Director-level answer
“Cross-domain flows (e.g., Book flight + hotel) run as sagas with compensations. We choose orchestration where auditability is key, choreography where coupling must be minimal. Observability tracks end-to-end outcomes.”

---

F. Security & Compliance (DevSecOps)

13) Embedding security into CI/CD (SAST/DAST/deps)

Ramp-up
	•	SAST (code), SCA (dependencies), DAST (runtime), container scans (Trivy), IaC scans (Checkov).
	•	Secrets scanning; SBOM; signed artifacts (Sigstore).

Director-level answer
“Security is pipeline-native: SAST/SCA, container + IaC scanning, secrets detection, SBOM + artifact signing. Failing checks block merges. We publish security scorecards per service and drive issues into backlogs with SLAs.”

---

14) Secrets & identity for .NET services

Ramp-up
	•	Use IAM roles, Secrets Manager/SSM; rotate; deny plaintext secrets in repos.
	•	OIDC/OAuth2/JWT between services; mTLS via mesh/gateway.

Director-level answer
“Services use role-based IAM with Secrets Manager; no static creds. OIDC/JWT for service auth; mTLS for transport. Rotation and least privilege are automated and audited. This is table stakes for a regulated platform.”

---

15) Compliance as Code (HIPAA/CIS/PCI analogs)

Ramp-up
	•	Encode policies (encryption, logging, tagging, network) in OPA/Sentinel.
	•	Auto-enforce via PR checks; central dashboards for evidence.

Director-level answer
“Compliance is codified: policies enforced via OPA/Checkov/Sentinel in CI. Non-compliant changes cannot merge. Evidence is generated continuously—audits are a query, not a project.”

---

G. Observability & Ops

16) Standardizing logs/metrics/traces for .NET

Ramp-up
	•	Serilog structured logs; correlation IDs; OpenTelemetry SDK for traces/metrics.
	•	RED/Golden signals dashboards; exemplars to traces.

Director-level answer
“All .NET templates ship with Serilog and OpenTelemetry. We require correlation IDs across services and RED dashboards by default. This unified telemetry cuts MTTR and enables SLO-based operations.”

---

17) On-call, runbooks, and incident automation

Ramp-up
	•	Auto-remediation playbooks; ChatOps; post-incident reviews; error budgets.

Director-level answer
“We maintain runbooks with auto-remediation for known issues and ChatOps for response. Blameless PIRs feed back into automation or design fixes. Error budgets govern release pace. Ops is a team sport with platform support.”

---

H. CI/CD & Release Engineering

18) Trunk-based development, feature flags, progressive delivery

Ramp-up
	•	Trunk-based + small PRs; feature flags for risk; blue/green and canary via mesh/gateway.

Director-level answer
“We default to trunk-based with small PRs, feature flags for safety, and blue/green or canary rollouts. Pipelines are reusable workflows with security stages embedded. Releases are frequent, reversible, observable.”

---

19) Building reusable pipelines for .NET

Ramp-up
	•	GitHub Actions reusable workflows; central actions repo; org-level policies.

Director-level answer
“We publish reusable CI/CD workflows (build/test/SAST/SCA/container/IaC scans/deploy), version them centrally, and auto-upgrade via bots. Teams get velocity; platform gets compliance.”

---

I. Data Management & Privacy

20) PII handling and data minimization

Ramp-up
	•	Classify data; encrypt in transit/at rest; minimize copies; tokenization/masking; access audits.

Director-level answer
“We classify data and enforce encryption, tokenization, masking, and least-privilege access. Pipelines block PII spills in logs. We audit access regularly and minimize data movement. Privacy controls are built-in, not bolted-on.”

---

J. Cost & FinOps

21) Cost controls without slowing teams

Ramp-up
	•	Budgets/alerts, rightsizing, autoscaling, savings plans, tag hygiene; showback/chargeback.

Director-level answer
“We tag everything, set budgets/alerts, enforce rightsizing/autoscaling, and negotiate savings plans. Showback gives teams visibility; we coach, not police. Cost is a first-class engineering metric.”

---

K. Migration & Modernization

22) .NET Framework → .NET (Core/8) modernization at scale

Ramp-up
	•	Strangler pattern; upgrade shared libs first; dual-run, parity tests; map unsupported APIs early.

Director-level answer
“We modernize via strangler: update common libs, pick a low-risk service, dual-run, build parity tests, and rinse-repeat. We publish a migration playbook and scorecards. This de-risks change while showing quick wins.”

---

23) Decommissioning bespoke middleware to standard tech

Ramp-up
	•	Build compat layers, run both systems, migrate consumers in waves, measure/rollback.

Director-level answer
“I’ve decommissioned bespoke buses to Solace/Kafka by running dual-publishers, migrating consumers in waves, and tracking metrics for cutover. We keep rollback paths and publish timelines so downstream teams aren’t surprised.”

---

L. Domain-Specific (Travel)

24) Search & caching strategy for travel content

Ramp-up
	•	Multi-source aggregation; normalize schemas; cache hot searches at edge and service; TTL/invalidations.

Director-level answer
“Travel search is read-heavy. We cache normalized results at edge + service, tune TTLs per supplier volatility, and use async refresh to keep results hot. This reduces partner load and improves perceived UX.”

---

25) Booking orchestration across multiple suppliers

Ramp-up
	•	Orchestrated saga with compensations; idempotent booking; provider SLAs; retries/timeouts.

Director-level answer
“We orchestrate booking via saga, use idempotency keys, and adopt provider-specific SLAs/timeouts. If a supplier fails, we compensate gracefully and present actionable fallback to the user. Reliability beats ‘perfect’ every time.”

---

M. Governance & Parity with Java

26) Bringing .NET governance to parity with Java

Ramp-up
	•	Mirror principles (not tools): API standards, logging, SLOs, security, CI/CD quality bars.
	•	Cross-stack council; shared vocabulary and maturity model.

Director-level answer
“We align on principles—observability, resilience, security, CI/CD—then map Java’s maturity to .NET analogs (Serilog/OTEL, Polly, GitHub Actions). A cross-stack Architecture Council keeps parity while letting stacks evolve independently.”

---

N. Advanced Topics

27) Blue/green vs canary strategies

Ramp-up
	•	Blue/green = instant switch, easy rollback; Canary = gradual % rollout + metrics guardrails.

Director-level answer
“We use blue/green for infra changes and canary for code changes with user impact, gating progression on SLOs and error budgets. It’s progressive delivery backed by data, not hope.”

---

28) Chaos engineering in a regulated org

Ramp-up
	•	Start in staging; inject latency/failure; runbooks/alerts verified; limited, well-scoped prod chaos later.

Director-level answer
“We do chaos in staging first to validate runbooks/alerts, then limited, well-scoped experiments in prod during low traffic with rollback ready. The goal is confidence, not fireworks.”

---

29) Testing strategy for microservices

Ramp-up
	•	Pyramid: unit → contract → integration → e2e; consumer-driven contracts; test data mgmt.

Director-level answer
“We prioritize unit + contract tests, keep e2e thin, and automate test data. Consumer-driven contracts prevent integration surprises and speed up CI.”

---

30) Idempotency & exactly-once semantics

Ramp-up
	•	Exactly-once is mythical across distrib systems; design idempotent ops + dedupe/outbox.

Director-level answer
“We don’t chase exactly-once across networks; we design idempotency and use outbox/inbox with dedupe. That’s how you get practical correctness at scale.”