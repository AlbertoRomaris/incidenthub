# V7 – IncidentHub Web Dashboard

## Purpose

V7 adds a real web dashboard on top of IncidentHub.

The goal of this version is to provide a visual operational interface for the platform, so incidents, timelines, evidence, alerts, SLOs and metrics can be inspected without using only REST calls or database queries.

This version turns IncidentHub from a backend/cloud platform into a complete incident response product with a user-facing operational console.

---

## Summary

V7 introduces a React + TypeScript + Vite frontend located in:

```text
incidenthub-ui/
```

The dashboard is connected to the real IncidentHub API through the Vite development proxy.

```text
React UI
  ↓
/api/*
  ↓
Vite proxy
  ↓
IncidentHub API
  ↓
PostgreSQL
```

The UI does not use mock data in its final state. It reads incidents, metrics, SLOs, timelines, evidence and alerts from the backend.

---

## Scope

Implemented in V7:

- React + TypeScript + Vite frontend
- Operational dashboard page
- Incidents page with filters
- Incident detail page
- Timeline visualization
- Evidence visualization
- Alerts visualization
- SLOs and metrics page
- Alerts page with filters
- Sidebar navigation
- Global alerts endpoint
- API client layer for frontend/backend communication

---

## New Module

### `incidenthub-ui`

The new frontend module contains the web dashboard.

```text
incidenthub-ui/
├── public/
├── src/
│   ├── api/
│   │   └── incidenthubClient.ts
│   │
│   ├── components/
│   │   ├── AppLayout.tsx
│   │   ├── IncidentTable.tsx
│   │   ├── MetricCard.tsx
│   │   └── StatusBadge.tsx
│   │
│   ├── pages/
│   │   ├── AlertsPage.tsx
│   │   ├── DashboardPage.tsx
│   │   ├── IncidentDetailPage.tsx
│   │   ├── IncidentsPage.tsx
│   │   └── SlosPage.tsx
│   │
│   ├── types/
│   │   └── incidenthub.ts
│   │
│   ├── App.tsx
│   ├── index.css
│   └── main.tsx
│
├── index.html
├── package.json
├── package-lock.json
├── tsconfig.json
└── vite.config.ts
```

---

## Frontend Architecture

The UI is intentionally simple and focused.

It is structured around:

- `api/`: HTTP client functions
- `types/`: shared frontend TypeScript models
- `components/`: reusable visual components
- `pages/`: top-level dashboard views

The frontend does not duplicate backend business logic. It only fetches data from the API, derives UI-level summaries and renders operational views.

---

## Vite Proxy

The frontend uses a Vite proxy so local development can call the backend without CORS configuration.

```ts
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, ''),
    },
  },
}
```

This means the UI calls:

```text
/api/incidents?limit=20
```

and Vite forwards it to:

```text
http://localhost:8080/incidents?limit=20
```

---

## Pages

## 1. Dashboard Page

File:

```text
incidenthub-ui/src/pages/DashboardPage.tsx
```

The dashboard is the main operational overview.

It shows:

- Open incidents
- High severity incidents
- Signal processing success rate
- Alerts sent
- Alert delivery success rate
- Runtime status
- Recent incidents
- SLO summary
- System flow

The dashboard consumes:

```http
GET /incidents?limit=20
GET /metrics/operational
GET /slo/summary
```

The purpose of this screen is to quickly answer:

- Is the system healthy?
- Are there active incidents?
- Are alerts being delivered?
- Are SLOs currently healthy?
- What incidents happened recently?

---

## 2. Incidents Page

File:

```text
incidenthub-ui/src/pages/IncidentsPage.tsx
```

The Incidents page provides a dedicated incident management view.

It includes:

- Total incident count
- Open incident count
- High impact incident count
- Number of affected services
- Search input
- Status filter
- Severity filter
- Service filter
- Clickable incident rows

The page consumes:

```http
GET /incidents?limit=100
```

Supported filters:

```text
Status:
- ALL
- OPEN
- ACKNOWLEDGED
- RESOLVED

Severity:
- ALL
- LOW
- MEDIUM
- HIGH
- CRITICAL

Service:
- ALL
- Dynamically loaded from incident data
```

Clicking an incident opens the Incident Detail page.

---

## 3. Incident Detail Page

File:

```text
incidenthub-ui/src/pages/IncidentDetailPage.tsx
```

The Incident Detail page shows the full operational context of a selected incident.

It displays:

- Incident summary
- Description
- Severity
- Status
- Service
- Environment
- Incident type
- Occurrence count
- First seen timestamp
- Last seen timestamp
- Opened timestamp
- Timeline events
- Evidence records
- Alert records

The page consumes:

```http
GET /incidents/{incidentId}/timeline
GET /incidents/{incidentId}/evidence
GET /incidents/{incidentId}/alerts
```

This page is important because it demonstrates that IncidentHub is not only opening incidents, but also explaining why they exist.

It provides traceability through:

```text
Incident
  ↓
Timeline
  ↓
Evidence
  ↓
Alerts
```

---

## 4. SLOs & Metrics Page

File:

```text
incidenthub-ui/src/pages/SlosPage.tsx
```

The SLOs page exposes IncidentHub self-observability.

It displays:

- Healthy SLO count
- Breached SLO count
- Signal processing success rate
- Average processing latency
- Alert delivery success rate
- SLO health table
- Processing health panel
- Metric groups

The page consumes:

```http
GET /metrics/operational
GET /slo/summary
```

The purpose of this screen is to show that IncidentHub can observe its own reliability and operational state.

Metric groups are derived in the UI from metric names, for example:

```text
SIGNAL_PROCESSING_* → Signal processing
ALERT_*             → Alerting
INCIDENT_*          → Incidents
TASK_*              → Processing tasks
```

---

## 5. Alerts Page

File:

```text
incidenthub-ui/src/pages/AlertsPage.tsx
```

The Alerts page shows alert delivery history across all incidents.

It includes:

- Total alerts
- Sent alerts
- Pending alerts
- Failed alerts
- Status filter
- Channel filter
- Search input
- Related incident link

The page consumes:

```http
GET /alerts?limit=100
GET /incidents?limit=100
```

The `/alerts` endpoint provides global alert history. The incidents endpoint is also loaded so each alert can be linked back to its related incident.

Supported filters:

```text
Status:
- ALL
- SENT
- PENDING
- FAILED

Channel:
- ALL
- LOG
- SNS
```

Clicking the related incident opens the Incident Detail page.

---

## New Backend Endpoint

V7 adds a global alert listing endpoint:

```http
GET /alerts?limit=100
```

This endpoint returns the most recent alert records ordered by creation time.

Example response:

```json
[
  {
    "alertId": "5f5515f4-70aa-46da-9886-45a5ed936d65",
    "incidentId": "0e6b7414-1c05-4056-8204-b5c4f00fb54b",
    "channel": "LOG",
    "status": "SENT",
    "title": "Incident opened: TIMEOUT_SPIKE detected for payment-service",
    "message": "Incident TIMEOUT_SPIKE detected for service payment-service in environment local.",
    "createdAt": "2026-05-03T22:44:40.417953Z",
    "sentAt": "2026-05-03T22:44:44.973259Z",
    "failedAt": null,
    "failureReason": null,
    "attributes": {
      "serviceName": "payment-service",
      "environment": "local",
      "severity": "HIGH",
      "incidentType": "TIMEOUT_SPIKE"
    }
  }
]
```

---

## Backend Changes

### Core

Updated port:

```text
incidenthub-core/src/main/java/com/incidenthub/core/application/port/AlertRepository.java
```

Added:

```java
List<Alert> findRecent(int limit);
```

Updated use case:

```text
incidenthub-core/src/main/java/com/incidenthub/core/application/usecase/ListIncidentAlertsUseCase.java
```

Added:

```java
public List<Alert> findRecent(int limit)
```

### Infrastructure

Updated Spring Data repository:

```text
incidenthub-infrastructure/src/main/java/com/incidenthub/infrastructure/persistence/alert/SpringDataAlertRepository.java
```

Added:

```java
List<AlertEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
```

Updated JPA adapter:

```text
incidenthub-infrastructure/src/main/java/com/incidenthub/infrastructure/persistence/alert/JpaAlertRepository.java
```

Added implementation for:

```java
findRecent(int limit)
```

### API

Added controller:

```text
incidenthub-api/src/main/java/com/incidenthub/api/controller/AlertController.java
```

Endpoint:

```http
GET /alerts?limit=100
```

Updated DTO:

```text
incidenthub-api/src/main/java/com/incidenthub/api/dto/incident/IncidentAlertResponse.java
```

Added static factory method:

```java
public static IncidentAlertResponse from(Alert alert)
```

---

## UI Navigation

The sidebar now supports real navigation between:

```text
Dashboard
Incidents
SLOs
Alerts
```

The selected view is stored in React state.

When an incident is selected from Dashboard, Incidents or Alerts, the UI opens the Incident Detail view.

```text
Dashboard → Incident Detail
Incidents → Incident Detail
Alerts    → Incident Detail
```

---

## API Client

File:

```text
incidenthub-ui/src/api/incidenthubClient.ts
```

The API client centralizes frontend HTTP calls:

```ts
getIncidents(limit)
getOperationalMetrics()
getSloSummary()
getIncidentTimeline(incidentId)
getIncidentEvidence(incidentId)
getIncidentAlerts(incidentId)
getAlerts(limit)
```

This keeps page components focused on UI logic instead of raw fetch calls.

---

## TypeScript Models

File:

```text
incidenthub-ui/src/types/incidenthub.ts
```

Main models:

```ts
Incident
OperationalMetric
SloSummary
IncidentTimelineEvent
IncidentEvidence
IncidentAlert
```

These models mirror the backend response DTOs used by the dashboard.

---

## Local Run

### 1. Start PostgreSQL

From the repository root:

```powershell
docker compose up -d postgres
```

### 2. Start API

Run:

```text
IncidentHubApiApplication
```

with the `local` profile.

API URL:

```text
http://localhost:8080
```

### 3. Start Worker

Run:

```text
IncidentHubWorkerApplication
```

with the `local` profile.

### 4. Start UI

```powershell
cd incidenthub-ui
npm install
npm run dev
```

UI URL:

```text
http://localhost:5173
```

---

## Validation

### Backend

```powershell
mvn clean package -DskipTests
```

### Frontend

```powershell
cd incidenthub-ui
npm run build
```

### API Checks

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
Invoke-RestMethod -Uri "http://localhost:8080/incidents?limit=10"
Invoke-RestMethod -Uri "http://localhost:8080/metrics/operational"
Invoke-RestMethod -Uri "http://localhost:8080/slo/summary"
Invoke-RestMethod -Uri "http://localhost:8080/alerts?limit=20"
```

---

## Demo Flow

A good V7 demo flow is:

1. Start PostgreSQL
2. Start API
3. Start Worker
4. Start UI
5. Send signals to the API
6. Let the Worker process the signals
7. Open the Dashboard
8. Inspect recent incidents
9. Open the Incidents page
10. Filter incidents by severity/status/service
11. Open an Incident Detail page
12. Review timeline events
13. Review evidence records
14. Review alert delivery records
15. Open the SLOs page
16. Validate operational health
17. Open the Alerts page
18. Filter alerts and jump back to related incidents

---

## What V7 Demonstrates

V7 demonstrates that IncidentHub is not only a backend service, but a complete operational platform.

It shows:

- Real frontend/backend integration
- API-driven UI design
- Incident management workflows
- Operational traceability
- Alert delivery visibility
- SLO and metrics visualization
- Full-stack product thinking
- Clean separation between UI, API, core and infrastructure

---

## Final Result

After V7, IncidentHub includes:

```text
Backend API
Worker
PostgreSQL persistence
Rule engine
Incident lifecycle
Evidence
Timeline
Runbooks
Alerts
Metrics
SLOs
AWS infrastructure
CI/CD
React web dashboard
```

V7 completes the project as a full portfolio-grade incident response platform.
