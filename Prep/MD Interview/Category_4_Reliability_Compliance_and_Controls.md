# Category 4 – Reliability, Compliance & Controls

### Q1 – How do you embed reliability engineering into culture?
**Director-Level Answer**  
> Reliability is a discipline, not a role. I institutionalize SLOs, blameless post-mortems, and error budgets. Every team owns its uptime and observability.

**JPMC Alternative**  
> Reliability is part of our control environment. We define SLOs, track error budgets, and treat breaches as control exceptions requiring root-cause and preventive actions.

**Drill-Down Details**  
* Introduced SLOs (99.9 % critical, 99.5 % non-critical).  
* MTTR ↓ 55 % after on-call training and runbooks.  
* Added error-budget dashboard to Exec reports.

---

### Q2 – How do you make compliance continuous?
**Director-Level Answer**  
> Automate checks: IaC linting, vulnerability scans, secrets detection. Compliance becomes a pipeline stage rather than a monthly exercise.

**JPMC Alternative**  
> Implement *Compliance-as-Code*—OPA, Checkov, and Terraform Sentinel policies embedded in CI. Every build is self-auditing, reducing manual evidence requests.

**Drill-Down Details**  
* Created policy repo with 150+ rules.  
* Audit finding closure time ↓ from 6 weeks to 48 hrs.  
* Auditors consume pipeline logs as evidence.

---

### Q3 – How do you balance speed with control?
**Director-Level Answer**  
> Design controls that scale with automation. Manual gates kill velocity; automated ones ensure compliance without delay. Regularly review control ROI.

**JPMC Alternative**  
> We embed controls into tooling so developers move fast safely. Change Management Board focuses on exceptions, not routine releases approved via automated evidence.

**Drill-Down Details**  
* Standard change model for low-risk deploys.  
* CAB load ↓ 70 %, release frequency ↑ 3×.  
* Continuous audit trail via Git and ServiceNow integration.

---
