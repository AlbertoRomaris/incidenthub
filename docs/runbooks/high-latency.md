# High Latency Runbook

## Purpose

This runbook helps investigate and mitigate incidents where a service emits repeated `LATENCY` signals within a short time window.

In IncidentHub, this runbook is attached to incidents of type:

```text
HIGH_LATENCY
```

Typical example:

```text
payment-service emits 3 LATENCY signals within 60 seconds
```

---

## What This Incident Means

A `HIGH_LATENCY` incident means that a service is still responding, but it is responding slower than expected.

This is different from a full outage. The service may still appear healthy from a basic health check, while users experience slow checkout, delayed responses or timeouts in upstream systems.

---

## Initial Severity

Default severity:

```text
MEDIUM
```

Escalate to `HIGH` or `CRITICAL` if:

- latency affects payment authorization
- users cannot complete checkout
- downstream services start timing out
- error rate increases at the same time
- the issue affects production traffic

---

## First Response Checklist

1. Confirm the incident details.
2. Check affected service and environment.
3. Review attached evidence.
4. Check recent latency signals.
5. Check whether errors or timeouts are also increasing.
6. Identify whether the issue is internal or caused by a dependency.
7. Apply mitigation if user impact is confirmed.
8. Resolve only after latency has returned to normal.

---

## IncidentHub Checks

### Get the incident

```http
GET /incidents/{incidentId}
```

Check:

- `serviceName`
- `environment`
- `severity`
- `status`
- `occurrenceCount`
- `firstSeenAt`
- `lastSeenAt`
- `summary`
- `description`

### Get incident evidence

```http
GET /incidents/{incidentId}/evidence
```

Look for:

- `correlationId`
- `matchingSignalsCount`
- `reason`
- `signalType`
- `severity`

Example evidence:

```text
reason: Matched 3 signals within 60 seconds
signalType: LATENCY
matchingSignalsCount: 3
```

### Get incident timeline

```http
GET /incidents/{incidentId}/timeline
```

Expected events:

```text
INCIDENT_OPENED
RUNBOOK_ATTACHED
ALERT_REQUESTED
EVIDENCE_ATTACHED
ALERT_SENT
```

### Get alerts

```http
GET /incidents/{incidentId}/alerts
```

Confirm whether the alert was sent:

```text
status: SENT
channel: LOG
```

---

## Local SQL Checks

### Recent latency signals

```sql
select id,
       service_name,
       environment,
       signal_type,
       severity,
       latency_ms,
       correlation_id,
       observed_at,
       received_at
from signals
where service_name = 'payment-service'
  and signal_type = 'LATENCY'
order by received_at desc
limit 20;
```

### Latency signal volume by minute

```sql
select date_trunc('minute', received_at) as minute,
       count(*) as latency_signals
from signals
where service_name = 'payment-service'
  and signal_type = 'LATENCY'
group by minute
order by minute desc
limit 15;
```

### Current active latency incidents

```sql
select id,
       service_name,
       environment,
       incident_type,
       severity,
       status,
       occurrence_count,
       first_seen_at,
       last_seen_at
from incidents
where incident_type = 'HIGH_LATENCY'
  and status in ('OPEN', 'ACKNOWLEDGED')
order by opened_at desc;
```

---

## Service-Level Checks

For `payment-service`, check:

- Is the service still accepting requests?
- Are payment requests completing slowly or timing out?
- Are only payments affected, or all endpoints?
- Did latency start after a deployment?
- Is a downstream provider responding slowly?
- Is the database slow?
- Is CPU, memory or thread usage high?
- Are there connection pool exhaustion symptoms?

---

## Common Causes

### Downstream dependency latency

A payment provider, fraud provider, database or external API may be slow.

Signs:

- high latency
- normal CPU/memory
- errors from external client logs
- request duration concentrated around dependency calls

### Resource saturation

The service may be overloaded.

Signs:

- high CPU
- high memory
- full thread pools
- connection pool exhaustion
- increased garbage collection
- slow responses across all endpoints

### Database slowness

Database queries may be slow or blocked.

Signs:

- increased query duration
- lock waits
- connection pool pressure
- slow inserts or updates

### Recent deployment regression

A new version may have introduced slower code paths.

Signs:

- latency starts immediately after deploy
- only new instances affected
- rollback improves latency

---

## Mitigation Options

Choose the least risky mitigation first.

### Reduce traffic pressure

- temporarily reduce request rate
- disable non-critical features
- pause expensive background jobs
- increase worker capacity if applicable

### Isolate dependency issue

- enable fallback if available
- reduce timeout values if currently too high
- temporarily bypass non-critical downstream calls
- contact provider if external dependency is degraded

### Roll back deployment

If latency started after a deployment:

1. Identify last known good version.
2. Roll back affected service.
3. Monitor latency and error signals.
4. Keep incident open until stability is confirmed.

### Scale service

If resource saturation is confirmed:

- increase replicas
- increase CPU/memory
- tune connection pool
- reduce concurrency if downstream is overloaded

---

## Escalation Criteria

Escalate immediately if:

- checkout completion rate drops
- payment authorization fails
- latency causes upstream timeouts
- incident lasts more than 15 minutes
- multiple services show latency
- severity increases from `MEDIUM` to `HIGH` or `CRITICAL`

Escalate to:

- service owner
- platform/on-call engineer
- database owner if DB latency is suspected
- vendor/provider contact if external provider is slow

---

## Resolution Criteria

Resolve the incident only when:

- no new `LATENCY` signals are observed for the affected service
- response times are back to expected baseline
- no related timeout or error incident is active
- user-facing impact is gone
- mitigation or fix has been applied and verified

---

## Post-Incident Follow-Up

Capture:

- start and end time
- affected endpoints
- affected users or traffic percentage
- root cause
- mitigation performed
- whether alert threshold was appropriate
- whether additional metrics or rules are needed

Potential improvements:

- add percentile latency tracking
- add dependency-specific latency signals
- add timeout-specific rule
- add dashboard for latency by service
- tune threshold or time window
