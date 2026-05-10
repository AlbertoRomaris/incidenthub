import { useEffect, useMemo, useState } from 'react'
import {
  getIncidents,
  getOperationalMetrics,
  getSloSummary,
} from '../api/incidenthubClient'
import { IncidentTable } from '../components/IncidentTable'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'
import type { Incident, OperationalMetric, SloSummary } from '../types/incidenthub'

function findMetric(metrics: OperationalMetric[], name: string): OperationalMetric | undefined {
  return metrics.find((metric) => metric.name === name)
}

function formatNumber(value: number | undefined): string {
  if (value === undefined || Number.isNaN(value)) {
    return '—'
  }

  return new Intl.NumberFormat('en-US', {
    maximumFractionDigits: 1,
  }).format(value)
}

function formatPercent(value: number | undefined): string {
  if (value === undefined || Number.isNaN(value)) {
    return '—'
  }

  return `${formatNumber(value)}%`
}

export function DashboardPage() {
  const [incidents, setIncidents] = useState<Incident[]>([])
  const [metrics, setMetrics] = useState<OperationalMetric[]>([])
  const [slos, setSlos] = useState<SloSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadDashboard() {
      try {
        setLoading(true)
        setError(null)

        const [incidentsResponse, metricsResponse, slosResponse] = await Promise.all([
          getIncidents(20),
          getOperationalMetrics(),
          getSloSummary(),
        ])

        setIncidents(incidentsResponse)
        setMetrics(metricsResponse)
        setSlos(slosResponse)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unexpected dashboard loading error')
      } finally {
        setLoading(false)
      }
    }

    void loadDashboard()
  }, [])

  const dashboardStats = useMemo(() => {
    const openIncidents = incidents.filter((incident) => incident.status === 'OPEN').length

    const highSeverity = incidents.filter(
      (incident) =>
        incident.status === 'OPEN' &&
        (incident.severity === 'HIGH' || incident.severity === 'CRITICAL'),
    ).length

    const processingSuccess = findMetric(metrics, 'SIGNAL_PROCESSING_SUCCESS_RATE')?.value
    const alertsSent = findMetric(metrics, 'ALERTS_SENT')?.value
    const alertDelivery = findMetric(metrics, 'ALERT_DELIVERY_SUCCESS_RATE')?.value

    const runtimeStatus = slos.some((slo) => slo.status === 'BREACHED') ? 'BREACHED' : 'HEALTHY'

    return {
      openIncidents,
      highSeverity,
      processingSuccess,
      alertsSent,
      alertDelivery,
      runtimeStatus,
    }
  }, [incidents, metrics, slos])

  if (loading) {
    return (
      <div className="dashboard-page">
        <div className="empty-state">
          <strong>Loading IncidentHub dashboard...</strong>
          <span>Fetching incidents, metrics and SLOs from the API.</span>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="dashboard-page">
        <div className="empty-state empty-state--error">
          <strong>Unable to load dashboard</strong>
          <span>{error}</span>
        </div>
      </div>
    )
  }

  return (
    <div className="dashboard-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Cloud-native incident response platform</p>
          <h1>Operational dashboard</h1>
          <p>
            Monitor incidents, alert delivery, signal processing and SLO health from a single
            IncidentHub view.
          </p>
        </div>

        <div className="page-header__status">
          <span>Runtime status</span>
          <StatusBadge value={dashboardStats.runtimeStatus} variant="slo" />
        </div>
      </header>

      <section className="metrics-grid">
        <MetricCard
          title="Open incidents"
          value={String(dashboardStats.openIncidents)}
          subtitle="Currently active operational incidents"
          accent="orange"
        />

        <MetricCard
          title="High severity"
          value={String(dashboardStats.highSeverity)}
          subtitle="Open incidents with HIGH or CRITICAL severity"
          accent="pink"
        />

        <MetricCard
          title="Processing success"
          value={formatPercent(dashboardStats.processingSuccess)}
          subtitle="Signal processing success rate"
          accent="green"
        />

        <MetricCard
          title="Alerts sent"
          value={formatNumber(dashboardStats.alertsSent)}
          subtitle="Delivered through LOG or SNS"
          accent="blue"
        />

        <MetricCard
          title="Alert delivery"
          value={formatPercent(dashboardStats.alertDelivery)}
          subtitle="Alert delivery success rate"
          accent="purple"
        />
      </section>

      <section className="content-grid">
        {incidents.length > 0 ? (
          <IncidentTable incidents={incidents} />
        ) : (
          <div className="table-card">
            <div className="empty-state">
              <strong>No incidents yet</strong>
              <span>Send signals to the API and let the Worker process them.</span>
            </div>
          </div>
        )}

        <aside className="side-panel">
          <div className="panel">
            <h2>SLO summary</h2>

            <div className="slo-list">
              {slos.map((slo) => (
                <div key={slo.key}>
                  <span>{slo.key.replaceAll('-', ' ')}</span>
                  <StatusBadge value={slo.status} variant="slo" />
                </div>
              ))}
            </div>
          </div>

          <div className="panel">
            <h2>System flow</h2>

            <div className="flow-list">
              <span>Signal</span>
              <strong>→</strong>
              <span>API</span>
              <strong>→</strong>
              <span>Worker</span>
              <strong>→</strong>
              <span>Incident</span>
              <strong>→</strong>
              <span>Alert</span>
            </div>
          </div>
        </aside>
      </section>
    </div>
  )
}