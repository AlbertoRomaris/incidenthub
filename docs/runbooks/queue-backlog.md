# Queue Backlog Runbook

## Purpose

This runbook helps investigate incidents where queues, task tables or asynchronous processors accumulate backlog.

In IncidentHub, this runbook is attached to incidents of type:

```text
QUEUE_BACKLOG
```

Although current V3 uses PostgreSQL-backed processing tasks and alerts, the same operational concepts apply to future queue-based versions.

---

## What This Incident Means

A `QUEUE_BACKLOG` incident means work is arriving faster than it is being processed.

Possible affected systems:

- signal processing tasks
- alert delivery
- async workers
- external queues
- message brokers
- scheduled processors

Backlog can cause delayed detection, delayed alerts and stale operational state.

---

## Initial Severity

Default severity:

```text
HIGH
```

Escalate to `CRITICAL` if:

- incident detection is delayed
- alert delivery is delayed
- backlog keeps growing
- worker is completely stopped
- production recovery depends on queued work

---

## First Response Checklist

1. Identify which queue or task table is backlogged.
2. Check if workers are running.
3. Check if workers are failing.
4. Check processing rate.
5. Check incoming rate.
6. Check oldest pending task age.
7. Check database locks or slow queries.
8. Scale or restart workers if safe.
9. Resolve only after backlog drains.

---

## IncidentHub Checks

### Dashboard summary

```http
GET /dashboard/summary
```

Check:

- pending alerts
- active incidents
- failed alerts

### Incident timeline

```http
GET /incidents/{incidentId}/timeline
```

Look for delayed or missing events:

```text
ALERT_REQUESTED
ALERT_SENT
ALERT_FAILED
```

### Incident alerts

```http
GET /incidents/{incidentId}/alerts
```

Check alert status:

```text
PENDING
SENT
FAILED
```

---

## Local SQL Checks

### Signal processing task status

```sql
select status,
       count(*) as tasks
from signal_processing_tasks
group by status
order by status;
```

### Oldest pending signal processing tasks

```sql
select signal_id,
       status,
       attempts,
       locked_by,
       locked_at,
       created_at,
       updated_at
from signal_processing_tasks
where status = 'PENDING'
order by created_at asc
limit 20;
```

### Processing failures

```sql
select signal_id,
       status,
       attempts,
       last_error,
       updated_at
from signal_processing_tasks
where status = 'FAILED'
order by updated_at desc
limit 20;
```

### Pending alerts

```sql
select id,
       incident_id,
       channel,
       status,
       title,
       created_at
from alerts
where status = 'PENDING'
order by created_at asc;
```

### Failed alerts

```sql
select id,
       incident_id,
       channel,
       status,
       failure_reason,
       failed_at
from alerts
where status = 'FAILED'
order by failed_at desc;
```

---

## Worker Checks

### Is the worker running?

Check worker logs.

Expected signal task logs:

```text
Claimed X signal processing task(s)
Processed signal task: signalId=...
```

Expected alert logs:

```text
INCIDENT ALERT | alertId=...
Processed pending alerts: processed=1, sent=1, failed=0
```

If no logs appear:

- worker may not be running
- scheduling may not be enabled
- profile may be wrong
- database connection may be failing

### Check worker database connection

Look for startup logs:

```text
HikariPool-1 - Start completed
Successfully validated migrations
Found 7 JPA repository interfaces
```

If connection fails:

```text
Connection to localhost:5434 refused
```

then PostgreSQL is not running or port mapping is wrong.

---

## Common Causes

### Worker stopped

Signs:

- pending tasks increase
- no worker logs
- API still accepts signals

Actions:

- restart worker
- check startup errors
- verify database connectivity

### Worker failing tasks

Signs:

- tasks move to `FAILED`
- `last_error` populated
- exceptions in logs

Actions:

- inspect `last_error`
- fix code/config
- consider manual reset if safe

### Processing too slow

Signs:

- worker processes tasks but backlog grows
- incoming rate exceeds processing rate
- CPU/database pressure

Actions:

- increase batch size
- reduce poll delay
- scale workers
- optimize queries
- reduce incoming traffic if needed

### Database locking or contention

Signs:

- queries slow
- processing delayed
- locks held too long

Actions:

- inspect DB locks
- reduce transaction duration
- avoid long-running queries
- check indexes

### Alert sender failure

Signs:

- alerts stuck in `PENDING` or `FAILED`
- timeline has `ALERT_REQUESTED` but no `ALERT_SENT`

Actions:

- check alert processor logs
- check channel support
- verify sender configuration
- retry failed alerts if supported

---

## Mitigation Options

### Restart worker

First option if worker is stopped or stale.

### Scale worker

Use if workload is valid and processing capacity is insufficient.

### Increase batch size

Use when worker is healthy but processing small batches too slowly.

### Reduce poll delay

Use when latency matters and database load is acceptable.

### Pause incoming workload

Use if backlog threatens system stability.

### Disable noisy producer

Use if one service is flooding the system with low-value signals.

---

## Escalation Criteria

Escalate if:

- backlog grows for more than 10 minutes
- incident detection is delayed
- alerts are not being sent
- workers cannot connect to DB
- failed tasks accumulate
- production incidents may be missed

Escalate to:

- platform team
- service owner
- database owner
- incident commander if operational visibility is impaired

---

## Resolution Criteria

Resolve when:

- pending task count returns to normal
- failed task count is understood
- oldest pending task age is acceptable
- alert delivery resumes
- worker logs show successful processing
- dashboard summary is healthy

---

## Post-Incident Follow-Up

Document:

- backlog start and end
- max pending count
- oldest task age
- processing rate
- root cause
- mitigation
- whether scaling or config changes are needed

Potential improvements:

- backlog dashboard
- stale lock recovery
- retry policy
- dead-letter table for failed processing tasks
- worker autoscaling
- queue depth alert
