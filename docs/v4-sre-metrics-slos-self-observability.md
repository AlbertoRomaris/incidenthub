# V4 – SRE Metrics, SLOs & Self-Observability

## Overview

IncidentHub V4 evolves the platform from incident response workflows into self-observability.

V3 introduced incident timelines, runbook association, alert creation, alert delivery state, alert query endpoints and an operational dashboard.

V4 adds the observability layer around IncidentHub itself:

- operational metrics domain model
- metric types and units
- internal SLO definitions
- SLO evaluation model
- operational metrics summary use case
- operational metrics API endpoint
- SLO summary API endpoint
- Prometheus-compatible metrics endpoint
- signal processing latency SLO
- low-cardinality metric naming

The goal of V4 is to make IncidentHub not only capable of detecting incidents in other services, but also capable of reporting whether IncidentHub itself is healthy.

---

## Why V4 Exists

V3 could answer operational response questions:

```text
What happened during the incident?
Which runbook was attached?
Was an alert requested?
Was the alert sent?
What is the current incident state?
```

V4 starts answering self-observability questions:

```text
Is IncidentHub processing signals successfully?
Is there any signal processing backlog?
How old is the oldest pending task?
Are alerts being delivered successfully?
How many incidents are currently active?
Are internal SLOs healthy or breached?
Can external monitoring systems scrape IncidentHub metrics?
```

This moves IncidentHub from:

```text
incident response workflow
```

to:

```text
observable incident response platform
```

---

## High-Level Flow

```text
IncidentHub API / Worker
        ↓
Operational repositories
        ↓
Metrics summary use case
        ↓
Operational metrics
        ↓
SLO evaluations
        ↓
JSON metrics endpoints
        ↓
Prometheus-compatible endpoint
        ↓
Future dashboards and alarms
```

---

## Main V4 Capabilities

### Operational Metrics Model

V4 introduces a domain model for internal operational metrics.

Each metric has:

- name
- type
- unit
- value
- measured timestamp
- optional attributes

Metric types:

```text
COUNTER
GAUGE
RATIO
```

Metric units:

```text
COUNT
PERCENT
SECONDS
MILLISECONDS
```

This gives IncidentHub a consistent vocabulary for expressing its own health.

---

## Operational Metric Names

Current operational metric names include:

```text
SIGNAL_PROCESSING_TASKS_TOTAL
SIGNAL_PROCESSING_TASKS_PENDING
SIGNAL_PROCESSING_TASKS_PROCESSED
SIGNAL_PROCESSING_TASKS_FAILED
SIGNAL_PROCESSING_SUCCESS_RATE
SIGNAL_PROCESSING_AVERAGE_LATENCY_MS
OLDEST_PENDING_TASK_AGE_SECONDS

INCIDENTS_OPEN
INCIDENTS_ACTIVE
INCIDENTS_RESOLVED
INCIDENTS_HIGH_OPEN
INCIDENTS_CRITICAL_OPEN

ALERTS_TOTAL
ALERTS_PENDING
ALERTS_SENT
ALERTS_FAILED
ALERT_DELIVERY_SUCCESS_RATE
```

These metrics are intentionally low-cardinality and system-oriented.

They avoid labels such as dynamic incident ids, signal ids, correlation ids or request ids.

---

## Signal Processing Metrics

V4 exposes signal processing health.

Example metrics:

```text
SIGNAL_PROCESSING_TASKS_TOTAL
SIGNAL_PROCESSING_TASKS_PENDING
SIGNAL_PROCESSING_TASKS_PROCESSED
SIGNAL_PROCESSING_TASKS_FAILED
SIGNAL_PROCESSING_SUCCESS_RATE
SIGNAL_PROCESSING_AVERAGE_LATENCY_MS
OLDEST_PENDING_TASK_AGE_SECONDS
```

These metrics help answer:

```text
Is the worker keeping up?
Are tasks failing?
Is processing delayed?
Is the backlog growing?
```

Example local output:

```text
SIGNAL_PROCESSING_TASKS_TOTAL        24
SIGNAL_PROCESSING_TASKS_PENDING      0
SIGNAL_PROCESSING_TASKS_PROCESSED    24
SIGNAL_PROCESSING_TASKS_FAILED       0
SIGNAL_PROCESSING_SUCCESS_RATE       100
SIGNAL_PROCESSING_AVERAGE_LATENCY_MS 0
OLDEST_PENDING_TASK_AGE_SECONDS      0
```

---

## Signal Processing Latency

V4 adds a processing latency metric:

```text
SIGNAL_PROCESSING_AVERAGE_LATENCY_MS
```

This metric is calculated over a recent processing window rather than the entire historical dataset.

That makes the metric useful for current operational health.

The related SLO is:

```text
signal-processing-average-latency <= 10000 milliseconds
```

This means recent signal processing should average below 10 seconds.

---

## Incident Metrics

V4 also exposes incident state metrics.

Example metrics:

```text
INCIDENTS_OPEN
INCIDENTS_ACTIVE
INCIDENTS_RESOLVED
INCIDENTS_HIGH_OPEN
INCIDENTS_CRITICAL_OPEN
```

These metrics help answer:

```text
How many incidents are active?
How many high severity incidents are open?
Are there any critical open incidents?
How many incidents have been resolved?
```

Example local output:

```text
INCIDENTS_OPEN          2
INCIDENTS_ACTIVE        2
INCIDENTS_RESOLVED      4
INCIDENTS_HIGH_OPEN     1
INCIDENTS_CRITICAL_OPEN 0
```

---

## Alert Metrics

V4 exposes alert delivery health.

Example metrics:

```text
ALERTS_TOTAL
ALERTS_PENDING
ALERTS_SENT
ALERTS_FAILED
ALERT_DELIVERY_SUCCESS_RATE
```

These metrics help answer:

```text
Are alerts being delivered?
Are alerts stuck pending?
Are any alerts failing?
What is the alert success rate?
```

Example local output:

```text
ALERTS_TOTAL                 3
ALERTS_PENDING               0
ALERTS_SENT                  3
ALERTS_FAILED                0
ALERT_DELIVERY_SUCCESS_RATE  100
```

---

## SLO Model

V4 introduces a basic SLO model.

An SLO has:

- key
- description
- metric name
- comparison
- target
- unit

Supported comparisons:

```text
GREATER_THAN_OR_EQUAL
LESS_THAN_OR_EQUAL
```

Supported statuses:

```text
HEALTHY
BREACHED
```

SLOs are evaluated from current operational metrics.

---

## Current SLOs

V4 defines five internal SLOs.

### Signal Processing Success Rate

```text
signal-processing-success-rate
```

Objective:

```text
SIGNAL_PROCESSING_SUCCESS_RATE >= 99%
```

This means at least 99% of completed signal processing tasks should succeed.

---

### Alert Delivery Success Rate

```text
alert-delivery-success-rate
```

Objective:

```text
ALERT_DELIVERY_SUCCESS_RATE >= 99%
```

This means at least 99% of completed alert deliveries should succeed.

---

### Pending Task Backlog

```text
pending-task-backlog
```

Objective:

```text
SIGNAL_PROCESSING_TASKS_PENDING <= 50
```

This means IncidentHub should not accumulate too many pending signal processing tasks.

---

### Oldest Pending Task Age

```text
oldest-pending-task-age
```

Objective:

```text
OLDEST_PENDING_TASK_AGE_SECONDS <= 60
```

This means no pending signal processing task should remain pending for more than 60 seconds.

---

### Signal Processing Average Latency

```text
signal-processing-average-latency
```

Objective:

```text
SIGNAL_PROCESSING_AVERAGE_LATENCY_MS <= 10000
```

This means recent signal processing latency should remain below 10 seconds on average.

---

## Operational Metrics Endpoint

V4 exposes operational metrics through the API:

```http
GET /metrics/operational
```

Example PowerShell command:

```powershell
(Invoke-RestMethod -Uri "http://localhost:8080/metrics/operational").metrics | Format-Table name,type,unit,value
```

Example output:

```text
name                                 type    unit         value
----                                 ----    ----         -----
SIGNAL_PROCESSING_TASKS_TOTAL        COUNTER COUNT         24
SIGNAL_PROCESSING_TASKS_PENDING      GAUGE   COUNT          0
SIGNAL_PROCESSING_TASKS_PROCESSED    COUNTER COUNT         24
SIGNAL_PROCESSING_TASKS_FAILED       COUNTER COUNT          0
SIGNAL_PROCESSING_SUCCESS_RATE       RATIO   PERCENT      100
SIGNAL_PROCESSING_AVERAGE_LATENCY_MS GAUGE   MILLISECONDS   0
OLDEST_PENDING_TASK_AGE_SECONDS      GAUGE   SECONDS        0
INCIDENTS_OPEN                       GAUGE   COUNT          2
INCIDENTS_ACTIVE                     GAUGE   COUNT          2
INCIDENTS_RESOLVED                   COUNTER COUNT          4
INCIDENTS_HIGH_OPEN                  GAUGE   COUNT          1
INCIDENTS_CRITICAL_OPEN              GAUGE   COUNT          0
ALERTS_TOTAL                         COUNTER COUNT          3
ALERTS_PENDING                       GAUGE   COUNT          0
ALERTS_SENT                          COUNTER COUNT          3
ALERTS_FAILED                        COUNTER COUNT          0
ALERT_DELIVERY_SUCCESS_RATE          RATIO   PERCENT      100
```

---

## SLO Summary Endpoint

V4 exposes SLO evaluations through the API:

```http
GET /slo/summary
```

Example PowerShell command:

```powershell
(Invoke-RestMethod -Uri "http://localhost:8080/slo/summary").slos | Format-Table key,actualValue,targetValue,unit,status
```

Example output:

```text
key                               actualValue targetValue unit         status
---                               ----------- ----------- ----         ------
signal-processing-success-rate          100.0        99.0 PERCENT      HEALTHY
alert-delivery-success-rate             100.0        99.0 PERCENT      HEALTHY
pending-task-backlog                      0.0        50.0 COUNT        HEALTHY
oldest-pending-task-age                   0.0        60.0 SECONDS      HEALTHY
signal-processing-average-latency         0.0     10000.0 MILLISECONDS HEALTHY
```

This endpoint gives a simple SRE-style health view.

---

## Prometheus-Compatible Metrics Endpoint

V4 exposes a Prometheus-compatible endpoint:

```http
GET /metrics/prometheus
```

This endpoint returns text metrics with `HELP` and `TYPE` metadata.

Example output:

```text
# HELP incidenthub_signal_processing_tasks_total IncidentHub operational metric SIGNAL_PROCESSING_TASKS_TOTAL measured in COUNT.
# TYPE incidenthub_signal_processing_tasks_total counter
incidenthub_signal_processing_tasks_total 24.0

# HELP incidenthub_signal_processing_success_rate IncidentHub operational metric SIGNAL_PROCESSING_SUCCESS_RATE measured in PERCENT.
# TYPE incidenthub_signal_processing_success_rate gauge
incidenthub_signal_processing_success_rate 100.0

# HELP incidenthub_signal_processing_average_latency_ms IncidentHub operational metric SIGNAL_PROCESSING_AVERAGE_LATENCY_MS measured in MILLISECONDS.
# TYPE incidenthub_signal_processing_average_latency_ms gauge
incidenthub_signal_processing_average_latency_ms 0.0

# HELP incidenthub_slo_healthy Whether a configured IncidentHub SLO is healthy. 1 means healthy, 0 means breached.
# TYPE incidenthub_slo_healthy gauge
incidenthub_slo_healthy{key="signal-processing-success-rate"} 1.0
incidenthub_slo_healthy{key="alert-delivery-success-rate"} 1.0
incidenthub_slo_healthy{key="pending-task-backlog"} 1.0
incidenthub_slo_healthy{key="oldest-pending-task-age"} 1.0
incidenthub_slo_healthy{key="signal-processing-average-latency"} 1.0
```

---

## Low-Cardinality Metric Design

V4 intentionally keeps metric labels low-cardinality.

Good metric examples:

```text
incidenthub_signal_processing_tasks_pending
incidenthub_alert_delivery_success_rate
incidenthub_slo_healthy{key="alert-delivery-success-rate"}
```

Avoided label examples:

```text
incident_id
signal_id
correlation_id
trace_id
message
error_code with uncontrolled values
```

This keeps metrics safe for Prometheus-style systems and future dashboards.

---

## Future Visualization

The metrics added in V4 are designed to be visualized later.

Potential V5/AWS visualization options:

```text
Amazon Managed Service for Prometheus
Amazon Managed Grafana
CloudWatch Dashboards
CloudWatch Alarms
```

Example dashboard panels:

```text
Signal processing success rate
Signal processing average latency
Pending tasks
Oldest pending task age
Alert delivery success rate
Open incidents
High severity open incidents
SLO health
```

V4 prepares the platform for visual dashboards without requiring cloud infrastructure yet.

---

## API Endpoints Added in V4

### Operational Metrics

```http
GET /metrics/operational
```

Returns internal operational metrics as JSON.

### SLO Summary

```http
GET /slo/summary
```

Returns evaluated SLOs as JSON.

### Prometheus-Compatible Metrics

```http
GET /metrics/prometheus
```

Returns metrics in text format suitable for Prometheus-style scraping.

---

## Persistence Changes in V4

V4 does not add new tables.

The metrics are calculated from existing persisted state:

```text
signal_processing_tasks
incidents
alerts
```

This keeps V4 simple and avoids storing redundant metric snapshots.

Future versions may add historical metric snapshots if needed.

---

## Worker and API Responsibilities in V4

### API

The API now exposes:

- operational metrics
- SLO summary
- Prometheus-compatible metrics

### Worker

The worker continues to update operational state through:

- signal processing tasks
- incident creation
- evidence creation
- alert delivery

V4 reads that state and turns it into metrics and SLO evaluations.

---

## Current Status

### Version 4 complete

Implemented features:

- operational metrics domain model
- metric name, type and unit enums
- SLO definition model
- SLO evaluation model
- SLO status model
- operational metrics summary use case
- signal processing task count metrics
- signal processing success rate
- signal processing average latency metric
- oldest pending task age metric
- incident state metrics
- alert delivery metrics
- alert delivery success rate
- SLO summary endpoint
- operational metrics endpoint
- Prometheus-compatible metrics endpoint
- low-cardinality metric design

---

## Known Limitations

V4 intentionally does not include:

- real Prometheus server
- Grafana dashboard
- CloudWatch dashboard
- CloudWatch custom metrics
- historical metric snapshots
- alerting on SLO breach
- burn-rate alerts
- distributed tracing
- OpenTelemetry instrumentation
- service-level dashboards by customer or tenant

These belong to later versions.

---

## Next Version

### V5 – AWS Cloud Deployment

Potential V5 focus:

- Terraform-managed infrastructure
- ECS Fargate API and Worker
- SQS signal queue
- RDS PostgreSQL
- SNS alerting
- CloudWatch logs and alarms
- GitHub Actions CI/CD with OIDC
- optional Prometheus/Grafana visualization path

---

V4 is the version where IncidentHub starts observing itself.

It connects operational state, internal metrics, SLOs and Prometheus-compatible output so the platform can be monitored like a real SRE-facing system.
