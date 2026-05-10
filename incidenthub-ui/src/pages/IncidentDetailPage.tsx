import { useEffect, useState } from 'react'
import {
  getIncidentAlerts,
  getIncidentEvidence,
  getIncidentTimeline,
} from '../api/incidenthubClient'
import { StatusBadge } from '../components/StatusBadge'
import type {
  Incident,
  IncidentAlert,
  IncidentEvidence,
  IncidentTimelineEvent,
} from '../types/incidenthub'

type IncidentDetailPageProps = {
  incident: Incident
  onBack: () => void
}

function formatDate(value: string | null | undefined): string {
  if (!value) {
    return '—'
  }

  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'medium',
  }).format(new Date(value))
}

function renderAttributes(attributes: Record<string, unknown>) {
  const entries = Object.entries(attributes)

  if (entries.length === 0) {
    return <span className="muted-text">No attributes</span>
  }

  return (
    <div className="attribute-grid">
      {entries.map(([key, value]) => (
        <div key={key}>
          <span>{key}</span>
          <strong>{String(value)}</strong>
        </div>
      ))}
    </div>
  )
}

export function IncidentDetailPage({ incident, onBack }: IncidentDetailPageProps) {
  const [timeline, setTimeline] = useState<IncidentTimelineEvent[]>([])
  const [evidence, setEvidence] = useState<IncidentEvidence[]>([])
  const [alerts, setAlerts] = useState<IncidentAlert[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadIncidentDetails() {
      try {
        setLoading(true)
        setError(null)

        const [timelineResponse, evidenceResponse, alertsResponse] = await Promise.all([
          getIncidentTimeline(incident.incidentId),
          getIncidentEvidence(incident.incidentId),
          getIncidentAlerts(incident.incidentId),
        ])

        setTimeline(timelineResponse)
        setEvidence(evidenceResponse)
        setAlerts(alertsResponse)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unexpected incident detail loading error')
      } finally {
        setLoading(false)
      }
    }

    void loadIncidentDetails()
  }, [incident.incidentId])

  return (
    <div className="incident-detail-page">
      <button className="back-button" onClick={onBack}>
        ← Back to dashboard
      </button>

      <header className="incident-hero">
        <div>
          <p className="eyebrow">Incident detail</p>
          <h1>{incident.summary}</h1>
          <p>{incident.description}</p>
        </div>

        <div className="incident-hero__badges">
          <StatusBadge value={incident.severity} variant="severity" />
          <StatusBadge value={incident.status} variant="status" />
        </div>
      </header>

      <section className="detail-grid detail-grid--summary">
        <article className="detail-card">
          <span>Service</span>
          <strong>{incident.serviceName}</strong>
        </article>

        <article className="detail-card">
          <span>Environment</span>
          <strong>{incident.environment}</strong>
        </article>

        <article className="detail-card">
          <span>Type</span>
          <strong>{incident.incidentType}</strong>
        </article>

        <article className="detail-card">
          <span>Occurrences</span>
          <strong>{incident.occurrenceCount}</strong>
        </article>
      </section>

      <section className="detail-grid detail-grid--dates">
        <article className="detail-card">
          <span>First seen</span>
          <strong>{formatDate(incident.firstSeenAt)}</strong>
        </article>

        <article className="detail-card">
          <span>Last seen</span>
          <strong>{formatDate(incident.lastSeenAt)}</strong>
        </article>

        <article className="detail-card">
          <span>Opened at</span>
          <strong>{formatDate(incident.openedAt)}</strong>
        </article>
      </section>

      {loading && (
        <div className="empty-state">
          <strong>Loading incident details...</strong>
          <span>Fetching timeline, evidence and alerts.</span>
        </div>
      )}

      {error && (
        <div className="empty-state empty-state--error">
          <strong>Unable to load incident details</strong>
          <span>{error}</span>
        </div>
      )}

      {!loading && !error && (
        <section className="incident-detail-layout">
          <div className="timeline-card">
            <div className="section-heading">
              <h2>Timeline</h2>
              <span>{timeline.length} events</span>
            </div>

            <div className="timeline-list">
              {timeline.map((event) => (
                <article className="timeline-event" key={event.eventId}>
                  <div className="timeline-event__marker" />
                  <div>
                    <div className="timeline-event__header">
                      <strong>{event.eventType}</strong>
                      <span>{formatDate(event.occurredAt)}</span>
                    </div>
                    <p>{event.summary}</p>
                    <small>Actor: {event.actor}</small>
                  </div>
                </article>
              ))}
            </div>
          </div>

          <div className="detail-side">
            <div className="panel">
              <div className="section-heading">
                <h2>Evidence</h2>
                <span>{evidence.length} items</span>
              </div>

              <div className="stack-list">
                {evidence.map((item) => (
                  <article className="stack-item" key={item.evidenceId}>
                    <strong>{item.summary}</strong>
                    <span>Signal: {item.signalId}</span>
                    <span>Captured: {formatDate(item.capturedAt)}</span>
                    {renderAttributes(item.attributes)}
                  </article>
                ))}
              </div>
            </div>

            <div className="panel">
              <div className="section-heading">
                <h2>Alerts</h2>
                <span>{alerts.length} items</span>
              </div>

              <div className="stack-list">
                {alerts.map((alert) => (
                  <article className="stack-item" key={alert.alertId}>
                    <div className="stack-item__header">
                      <strong>{alert.title}</strong>
                      <StatusBadge value={alert.status} variant="slo" />
                    </div>
                    <span>Channel: {alert.channel}</span>
                    <span>Created: {formatDate(alert.createdAt)}</span>
                    <span>Sent: {formatDate(alert.sentAt)}</span>
                    {alert.failureReason && <span>Failure: {alert.failureReason}</span>}
                  </article>
                ))}
              </div>
            </div>
          </div>
        </section>
      )}
    </div>
  )
}