import { useEffect, useMemo, useState } from 'react'
import { getIncidents } from '../api/incidenthubClient'
import { IncidentTable } from '../components/IncidentTable'
import { StatusBadge } from '../components/StatusBadge'
import type { Incident, IncidentSeverity, IncidentStatus } from '../types/incidenthub'

type IncidentsPageProps = {
  onSelectIncident: (incident: Incident) => void
}

type StatusFilter = 'ALL' | IncidentStatus
type SeverityFilter = 'ALL' | IncidentSeverity

function getUniqueServices(incidents: Incident[]): string[] {
  return Array.from(new Set(incidents.map((incident) => incident.serviceName))).sort()
}

export function IncidentsPage({ onSelectIncident }: IncidentsPageProps) {
  const [incidents, setIncidents] = useState<Incident[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [severityFilter, setSeverityFilter] = useState<SeverityFilter>('ALL')
  const [serviceFilter, setServiceFilter] = useState('ALL')
  const [search, setSearch] = useState('')

  useEffect(() => {
    async function loadIncidents() {
      try {
        setLoading(true)
        setError(null)

        const response = await getIncidents(100)
        setIncidents(response)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unexpected incidents loading error')
      } finally {
        setLoading(false)
      }
    }

    void loadIncidents()
  }, [])

  const services = useMemo(() => getUniqueServices(incidents), [incidents])

  const filteredIncidents = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase()

    return incidents.filter((incident) => {
      const matchesStatus = statusFilter === 'ALL' || incident.status === statusFilter
      const matchesSeverity = severityFilter === 'ALL' || incident.severity === severityFilter
      const matchesService = serviceFilter === 'ALL' || incident.serviceName === serviceFilter

      const matchesSearch =
        normalizedSearch.length === 0 ||
        incident.summary.toLowerCase().includes(normalizedSearch) ||
        incident.incidentType.toLowerCase().includes(normalizedSearch) ||
        incident.environment.toLowerCase().includes(normalizedSearch) ||
        incident.serviceName.toLowerCase().includes(normalizedSearch)

      return matchesStatus && matchesSeverity && matchesService && matchesSearch
    })
  }, [incidents, search, serviceFilter, severityFilter, statusFilter])

  const openCount = incidents.filter((incident) => incident.status === 'OPEN').length
  const highCount = incidents.filter(
    (incident) => incident.severity === 'HIGH' || incident.severity === 'CRITICAL',
  ).length

  if (loading) {
    return (
      <div className="incidents-page">
        <div className="empty-state">
          <strong>Loading incidents...</strong>
          <span>Fetching incident records from the IncidentHub API.</span>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="incidents-page">
        <div className="empty-state empty-state--error">
          <strong>Unable to load incidents</strong>
          <span>{error}</span>
        </div>
      </div>
    )
  }

  return (
    <div className="incidents-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Incident management</p>
          <h1>Incidents</h1>
          <p>
            Explore detected incidents, filter by operational state and drill down into evidence,
            timeline events and alert delivery.
          </p>
        </div>

        <div className="page-header__status incidents-summary-card">
          <span>Total incidents</span>
          <strong>{incidents.length}</strong>
        </div>
      </header>

      <section className="incidents-kpi-grid">
        <article className="compact-kpi">
          <span>Open</span>
          <strong>{openCount}</strong>
          <StatusBadge value="OPEN" variant="status" />
        </article>

        <article className="compact-kpi">
          <span>High impact</span>
          <strong>{highCount}</strong>
          <StatusBadge value="HIGH" variant="severity" />
        </article>

        <article className="compact-kpi">
          <span>Services</span>
          <strong>{services.length}</strong>
          <small>with detected incidents</small>
        </article>

        <article className="compact-kpi">
          <span>Filtered results</span>
          <strong>{filteredIncidents.length}</strong>
          <small>matching current filters</small>
        </article>
      </section>

      <section className="filters-card">
        <div className="filter-field filter-field--search">
          <label htmlFor="incident-search">Search</label>
          <input
            id="incident-search"
            placeholder="Search by summary, type, service or environment"
            type="search"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <div className="filter-field">
          <label htmlFor="status-filter">Status</label>
          <select
            id="status-filter"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as StatusFilter)}
          >
            <option value="ALL">All statuses</option>
            <option value="OPEN">Open</option>
            <option value="ACKNOWLEDGED">Acknowledged</option>
            <option value="RESOLVED">Resolved</option>
          </select>
        </div>

        <div className="filter-field">
          <label htmlFor="severity-filter">Severity</label>
          <select
            id="severity-filter"
            value={severityFilter}
            onChange={(event) => setSeverityFilter(event.target.value as SeverityFilter)}
          >
            <option value="ALL">All severities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
        </div>

        <div className="filter-field">
          <label htmlFor="service-filter">Service</label>
          <select
            id="service-filter"
            value={serviceFilter}
            onChange={(event) => setServiceFilter(event.target.value)}
          >
            <option value="ALL">All services</option>
            {services.map((service) => (
              <option key={service} value={service}>
                {service}
              </option>
            ))}
          </select>
        </div>
      </section>

      {filteredIncidents.length > 0 ? (
        <IncidentTable incidents={filteredIncidents} onSelectIncident={onSelectIncident} />
      ) : (
        <div className="table-card">
          <div className="empty-state">
            <strong>No incidents match the current filters</strong>
            <span>Adjust the filters or send new signals to the API.</span>
          </div>
        </div>
      )}
    </div>
  )
}
