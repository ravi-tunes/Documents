# Category 2 – Engineering Governance & Standards

### Q1 – What does “engineering governance” mean to you?
**Director-Level Answer**  
> Governance is about consistency and accountability, not bureaucracy. I define it as a lightweight framework that ensures teams deliver secure, observable, high-quality software without slowing innovation. The focus is on shared principles—security, reliability, maintainability—and measurable standards applied through automation.

**JPMC-Tailored Alternative**  
> Engineering governance at Chase means codifying *controls* into our daily engineering workflow—security, compliance, observability, resiliency—so that every release meets enterprise expectations automatically. The aim is to make compliance frictionless through automation and visibility.

**Drill-Down Details**  
* Framework – Architectural Review Council + Playbook + Templates.  
* Automation – Policy-as-Code in CI/CD; static analysis, IaC scans, SCA.  
* Metrics – Governance adoption %, policy violations resolved within SLA.  
* Example – Reduced review turnaround from 10 days to 2 via automated linting and checklists.  
* Vocabulary – “shift-left controls,” “golden path,” “reference architecture,” “guardrails not gates.”

---

### Q2 – How do you balance standardization with autonomy?
**Director-Level Answer**  
> I follow the 80/20 rule: 80 % standardized guardrails for efficiency and safety, 20 % sandbox for innovation. Governance should provide a paved road that’s easy to adopt; innovation labs can trial new tech, and proven patterns graduate into standards.

**JPMC Alternative**  
> We maintain a *federated governance* model: enterprise controls set the minimum bar; domain architects can innovate above that bar as long as controls and risk appetite remain satisfied. Approved innovations feed back into the enterprise playbook.

**Drill-Down Details**  
* Introduced “Tech Radar” + “Sandbox approval process”.  
* Created Innovation→Standardization pipeline (document→pilot→pattern→template).  
* Result – Teams kept velocity while reducing duplicate tooling by 40 %.  
* Vocabulary – “paved road,” “innovation lanes,” “federated architecture.”

---

### Q3 – How do you ensure engineering standards are adopted and not ignored?
**Director-Level Answer**  
> Adoption happens when standards save engineers time. I embed them in tooling, communicate “why” through workshops, and measure adoption publicly. Visibility plus convenience drives compliance more than enforcement.

**JPMC Alternative**  
> We integrate standards into the pipeline so that compliance is automatic—e.g., approved Docker bases, linting, security gates. Non-compliant changes fail builds with clear guidance. Adoption metrics feed into our Engineering Maturity Scorecard.

**Drill-Down Details**  
* Developer portal with boilerplates → service creation time ↓ 80 %.  
* Adoption dashboards per team; monthly league table of maturity scores.  
* 92 % standard pipeline usage in 6 months.  
* “Make the right path the easy path.”

---

### Q4 – How do you measure engineering excellence?
**Director-Level Answer**  
> I rely on a composite of **DORA metrics** + quality indicators + developer experience scores. These give a balanced view of velocity, stability, and team health.

**JPMC Alternative**  
> We use the Engineering Excellence Dashboard: lead time, deployment frequency, change-fail rate, MTTR, security findings, control exceptions. KPIs tie directly to risk and business impact.

**Drill-Down Details**  
* Benchmarked DORA baseline → set targets (+50 % deploy freq).  
* Introduced quarterly “Excellence Review” with CIO metrics.  
* Added Developer NPS survey to balance quant + qual.

---

### Q5 – How would you align .NET governance with existing Java standards?
**Director-Level Answer**  
> Start by extracting shared principles—security, observability, resilience—then map each to .NET equivalents. Form a cross-stack Architecture Council so standards evolve together. Parity is about maturity, not identical tools.

**JPMC Alternative**  
> I’d establish a unified governance framework where .NET and Java teams share the same control objectives but language-specific implementations. Joint scorecards show equivalent compliance and resilience maturity.

**Drill-Down Details**  
* Example mappings: SpringBoot→ASP.NET Core; SLF4J→Serilog; Resilience4J→Polly.  
* Governance council meets monthly; shared playbook in Confluence.  
* Cross-stack heatmap of maturity to prioritize training.

---
