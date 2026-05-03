# High Error Rate Runbook

## Purpose

This runbook helps investigate incidents where a service emits repeated `ERROR` signals within a short time window.

In IncidentHub, this runbook is attached to incidents of type:

```text
HIGH_ERROR_RATE
```

Typical example:

```text
payment-service emits 5 ERROR signals within 60 seconds
```

---

## What This Incident Means

A `HIGH_ERROR_RATE` incident means the service is returning or emitting too many errors.

Errors may be caused by:

- invalid internal state
- downstream dependency failures
- database failures
- deployment regressions
- unexpected input
- provider timeouts
- infrastructure issues

---

## Initial Severity

Default severity:

```text
HIGH
```

Escalate to `CRITICAL` if:

- users cannot complete a critical flow
- payments are failing
- error rate continues increasing
- multiple environments or services are affected
- the issue affects production traffic

---

## First Response Checklist

1. Confirm affected service and environment.
2. Review the incident evidence.
3. Identify dominant error codes.
4. Check whether errors started after a deployment.
5. Check if errors correlate with latency or timeout incidents.
6. Determine whether the error is internal or dependency-related.
7. Apply mitigation.
8. Resolve only after the error rate returns to baseline.

---

## IncidentHub Checks

### Get the incident

```http
GET /incidents/{incidentId}
```

Check:

- `incidentType`
- `severity`
- `occurrenceCount`
- `firstSeenAt`
- `lastSeenAt`
- `deduplicationKey`

### Get evidence

```http
GET /incidents/{incidentId}/evidence
```

Important fields:

- `correlationId`
- `signalType`
- `severity`
- `matchingSignalsCount`
- `reason`

### Get timeline

```http
GET /incidents/{incidentId}/timeline
```

Use timeline to verify:

- when the incident opened
- which runbook was attached
- whether alert was sent
- whether someone acknowledged or resolved it

---

## Local SQL Checks

### Recent error signals

```sql
select id,
       service_name,
       environment,
       severity,
       message,
       status_code,
       error_code,
       correlation_id,
       observed_at,
       received_at
from signals
where service_name = 'payment-service'
  and signal_type = 'ERROR'
order by received_at desc
limit 30;
```

### Error count by error code

```sql
select error_code,
       count(*) as errors
from signals
where service_name = 'payment-service'
  and signal_type = 'ERROR'
group by error_code
order by errors desc;
```

### Error count by status code

```sql
select status_code,
       count(*) as errors
from signals
where service_name = 'payment-service'
  and signal_type = 'ERROR'
group by status_code
order by errors desc;
```

### Active error incidents

```sql
select id,
       service_name,
       environment,
       severity,
       status,
       occurrence_count,
       first_seen_at,
       last_seen_at
from incidents
where incident_type = 'HIGH_ERROR_RATE'
  and status in ('OPEN', 'ACKNOWLEDGED')
order by opened_at desc;
```

---

## Common Causes

### Deployment regression

Signs:

- errors started after deployment
- new error code appears
- rollback reduces errors

Actions:

- compare deployment time with `firstSeenAt`
- inspect application logs
- rollback if impact is high

### Downstream provider failure

Signs:

- error codes mention provider failure
- timeouts or dependency failures occur too
- failures concentrated in external calls

Actions:

- check provider status
- enable fallback if available
- reduce dependency pressure
- contact provider support if needed

### Database issue

Signs:

- persistence errors
- connection pool errors
- timeout waiting for connection
- lock or migration-related errors

Actions:

- check DB health
- inspect slow queries
- check connection pool
- verify migrations

### Bad input or validation issue

Signs:

- errors are mostly 4xx
- specific request shape fails
- only one client or correlation group affected

Actions:

- identify bad request source
- block or reject invalid traffic earlier
- improve validation and error handling

---

## Mitigation Options

### Roll back

Use if issue started after deployment and user impact is high.

### Disable affected feature

Use if a specific non-critical path is failing.

### Apply fallback

Use if dependency failure is confirmed and fallback exists.

### Rate limit or block bad traffic

Use if a client or integration is producing invalid requests.

### Scale service

Use only if errors are caused by overload, not by code or dependency failures.

---

## Escalation Criteria

Escalate if:

- error rate continues after first mitigation
- payment or checkout flow is affected
- incident lasts more than 10 minutes
- multiple services are affected
- severity should be raised to `CRITICAL`

Escalate to:

- service owner
- on-call engineer
- platform team
- dependency owner
- vendor support if external provider is failing

---

## Resolution Criteria

Resolve only when:

- error signal volume returns to baseline
- no related timeout or dependency incident remains active
- mitigation or fix is confirmed
- recent requests succeed
- no new evidence is being attached to the incident

---

## Post-Incident Follow-Up

Document:

- root cause
- dominant error code
- affected endpoints
- affected customers or traffic percentage
- mitigation
- permanent fix
- alert threshold quality

Potential improvements:

- add error-code-specific rules
- add dependency-specific rules
- add deployment correlation
- add automatic rollback recommendation
- improve evidence attributes
