# Dependency Failure / Timeout Spike Runbook

## Purpose

This runbook helps investigate incidents caused by repeated dependency failures or timeout signals.

In IncidentHub, this runbook is attached to incident types such as:

```text
DEPENDENCY_FAILURE
TIMEOUT_SPIKE
```

Current rule example:

```text
payment-service emits 3 TIMEOUT signals within 60 seconds
```

---

## What This Incident Means

A dependency failure or timeout spike means the affected service is having trouble communicating with another system.

The service itself may be healthy, but it cannot complete its operation because a required dependency is slow, unavailable or returning failures.

Examples:

- payment provider timeout
- fraud provider unavailable
- database timeout
- message broker unavailable
- third-party API degradation
- internal service dependency failure

---

## Initial Severity

Default severity for timeout spike:

```text
HIGH
```

Escalate to `CRITICAL` if:

- core user flows fail
- payments cannot be completed
- retries increase load and worsen the incident
- multiple services depend on the failing dependency
- there is no fallback path

---

## First Response Checklist

1. Confirm affected service.
2. Identify suspected dependency.
3. Review attached evidence.
4. Check whether errors are timeout-related.
5. Check if latency is also high.
6. Check retry behavior.
7. Determine if fallback or circuit breaker should be enabled.
8. Reduce pressure on the failing dependency.
9. Resolve only after dependency calls recover.

---

## IncidentHub Checks

### Get incident

```http
GET /incidents/{incidentId}
```

Check:

- `incidentType`
- `severity`
- `summary`
- `description`
- `occurrenceCount`

### Get evidence

```http
GET /incidents/{incidentId}/evidence
```

Look for:

- dependency/provider name in attributes
- endpoint
- error code
- correlation id
- timeout-related messages

### Get timeline

```http
GET /incidents/{incidentId}/timeline
```

Expected events:

```text
INCIDENT_OPENED
RUNBOOK_ATTACHED
ALERT_REQUESTED
ALERT_SENT
EVIDENCE_ATTACHED
```

---

## Local SQL Checks

### Recent timeout signals

```sql
select id,
       service_name,
       environment,
       message,
       status_code,
       error_code,
       latency_ms,
       correlation_id,
       attributes_json,
       observed_at,
       received_at
from signals
where signal_type = 'TIMEOUT'
order by received_at desc
limit 30;
```

### Dependency failures

```sql
select id,
       service_name,
       environment,
       message,
       error_code,
       attributes_json,
       received_at
from signals
where signal_type = 'DEPENDENCY_FAILURE'
order by received_at desc
limit 30;
```

### Timeout volume by minute

```sql
select date_trunc('minute', received_at) as minute,
       count(*) as timeout_signals
from signals
where signal_type = 'TIMEOUT'
group by minute
order by minute desc
limit 15;
```

---

## Common Causes

### External provider degradation

Signs:

- high latency before timeout
- provider-specific error code
- external status page shows degradation
- multiple clients affected

Actions:

- check provider status
- contact provider support
- enable fallback if possible
- reduce timeout/retry pressure

### Internal dependency outage

Signs:

- internal service unavailable
- connection refused
- DNS/service discovery issue
- recent deploy in dependency

Actions:

- check dependency health
- check deployment history
- rollback dependency if needed
- reroute traffic if possible

### Database timeout

Signs:

- SQL timeout errors
- connection pool exhaustion
- slow queries
- lock waits

Actions:

- inspect active queries
- check locks
- increase pool only if safe
- stop expensive jobs
- rollback problematic migration

### Retry storm

Signs:

- failures increase traffic
- downstream dependency receives excessive retries
- timeout volume grows rapidly

Actions:

- reduce retry count
- add backoff
- enable circuit breaker
- shed non-critical traffic

---

## Mitigation Options

### Enable fallback

Use if the dependency is non-critical or degraded data is acceptable.

### Apply circuit breaker

Use when repeated calls to a failing dependency are worsening the incident.

### Reduce retry pressure

Retries can amplify an outage. Prefer exponential backoff and low max attempts.

### Temporarily disable affected integration

If a non-critical provider is failing, disable the integration until recovery.

### Roll back dependency deployment

If an internal dependency changed recently and failure started after that change.

---

## Escalation Criteria

Escalate immediately if:

- dependency affects payments or checkout
- timeout spike lasts more than 10 minutes
- retry storm is suspected
- multiple services report the same dependency
- no fallback exists

Escalate to:

- dependency owner
- platform team
- vendor support
- incident commander for production-wide impact

---

## Resolution Criteria

Resolve when:

- timeout/dependency signals stop
- dependency health is confirmed
- retries return to normal
- affected user flow succeeds
- no new evidence is attached to the incident

---

## Post-Incident Follow-Up

Document:

- dependency involved
- root cause
- timeout values
- retry behavior
- fallback availability
- customer impact
- mitigation

Potential improvements:

- dependency-specific rules
- provider-specific dashboards
- circuit breaker
- retry budget
- fallback mode
- dependency health signal
