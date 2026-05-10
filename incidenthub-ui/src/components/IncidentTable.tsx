import type { Incident } from '../types/incidenthub'
import { StatusBadge } from './StatusBadge'

type IncidentTableProps = {
  incidents: Incident[]
}

export function IncidentTable({ incidents }: IncidentTableProps) {
  return (
    <div className="table-card">
      <div className="table-card__header">
        <div>
          <h2>Recent incidents</h2>
          <p>Latest incidents detected by IncidentHub rules.</p>
        </div>
        <span className="table-card__count">{incidents.length} items</span>
      </div>

      <div className="incident-table">
        <div className="incident-table__row incident-table__row--head">
          <span>Incident</span>
          <span>Service</span>
          <span>Severity</span>
          <span>Status</span>
          <span>Occurrences</span>
        </div>

        {incidents.map((incident) => (
          <div className="incident-table__row" key={incident.incidentId}>
            <span>
              <strong>{incident.summary}</strong>
              <small>{incident.environment}</small>
            </span>
            <span>{incident.serviceName}</span>
            <span>
              <StatusBadge value={incident.severity} variant="severity" />
            </span>
            <span>
              <StatusBadge value={incident.status} variant="status" />
            </span>
            <span>{incident.occurrenceCount}</span>
          </div>
        ))}
      </div>
    </div>
  )
}