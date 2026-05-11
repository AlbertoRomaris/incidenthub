import { useEffect, useMemo, useState } from 'react'
import { getOperationalMetrics, getSloSummary } from '../api/incidenthubClient'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'
import type { OperationalMetric, SloSummary } from '../types/incidenthub'

function formatNumber(value: number | undefined): string {
  if (value === undefined || Number.isNaN(value)) {
    return '—'
  }

  return new Intl.NumberFormat('en-US', {
    maximumFractionDigits: 2,
  }).format(value)
}

function formatValue(value: number | undefined, unit: string | undefined): string {
  if (value === undefined || Number.isNaN(value)) {
    return '—'
  }

  if (unit === 'PERCENT') {
    return `${formatNumber(value)}%`
  }

  if (unit === 'MILLISECONDS') {
    return `${formatNumber(value)} ms`
  }

  if (unit === 'SECONDS') {
    return `${formatNumber(value)} s`
  }

  return formatNumber(value)
}

function getMetric(metrics: OperationalMetric[], name: string): OperationalMetric | undefined {
  return metrics.find((metric) => metric.name === name)
}

function prettifyKey(value: string): string {
  return value
    .replaceAll('_', ' ')
    .replaceAll('-', ' ')
    .toLowerCase()
    .replace(/(^|\s)\S/g, (letter) => letter.toUpperCase())
}

function getMetricGroup(metricName: string): string {
  if (metricName.includes('SIGNAL_PROCESSING')) {
    return 'Signal processing'
  }

  if (metricName.includes('ALERT')) {
    return 'Alerting'
  }

  if (metricName.includes('INCIDENT')) {
    return 'Incidents'
  }

  if (metricName.includes('TASK')) {
    return 'Processing tasks'
  }

  return 'Other'
}

export function SlosPage() {
  const [metrics, setMetrics] = useState<OperationalMetric[]>([])
  const [slos, setSlos] = useState<SloSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadSloPage() {
      try {
        setLoading(true)
        setError(null)

        const [metricsResponse, slosResponse] = await Promise.all([
          getOperationalMetrics(),
          getSloSummary(),
        ])

        setMetrics(metricsResponse)
        setSlos(slosResponse)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unexpected SLO loading error')
      } finally {
        setLoading(false)
      }
    }

    void loadSloPage()
  }, [])

  const pageStats = useMemo(() => {
    const breachedSlos = slos.filter((slo) => slo.status === 'BREACHED').length
    const healthySlos = slos.filter((slo) => slo.status === 'HEALTHY').length
    const runtimeStatus = breachedSlos > 0 ? 'BREACHED' : 'HEALTHY'

    const processingSuccess = getMetric(metrics, 'SIGNAL_PROCESSING_SUCCESS_RATE')
    const averageLatency = getMetric(metrics, 'SIGNAL_PROCESSING_AVERAGE_LATENCY_MS')
    const pendingTasks = getMetric(metrics, 'SIGNAL_PROCESSING_TASKS_PENDING')
    const alertDelivery = getMetric(metrics, 'ALERT_DELIVERY_SUCCESS_RATE')

    return {
      breachedSlos,
      healthySlos,
      runtimeStatus,
      processingSuccess,
      averageLatency,
      pendingTasks,
      alertDelivery,
    }
  }, [metrics, slos])

  const groupedMetrics = useMemo(() => {
    return metrics.reduce<Record<string, OperationalMetric[]>>((groups, metric) => {
      const group = getMetricGroup(metric.name)
      const existing = groups[group] ?? []

      return {
        ...groups,
        [group]: [...existing, metric],
      }
    }, {})
  }, [metrics])

  if (loading) {
    return (
      <div className="slos-page">
        <div className="empty-state">
          <strong>Loading SLOs...</strong>
          <span>Fetching operational metrics and SLO health from the IncidentHub API.</span>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="slos-page">
        <div className="empty-state empty-state--error">
          <strong>Unable to load SLOs</strong>
          <span>{error}</span>
        </div>
      </div>
    )
  }

  return (
    <div className="slos-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Self-observability</p>
          <h1>SLOs & metrics</h1>
          <p>
            Track IncidentHub operational health through service-level objectives, processing
            metrics, backlog indicators and alert delivery reliability.
          </p>
        </div>

        <div className="page-header__status">
          <span>Runtime status</span>
          <StatusBadge value={pageStats.runtimeStatus} variant="slo" />
        </div>
      </header>

      <section className="metrics-grid">
        <MetricCard
          title="Healthy SLOs"
          value={String(pageStats.healthySlos)}
          subtitle="Objectives currently within target"
          accent="green"
        />

        <MetricCard
          title="Breached SLOs"
          value={String(pageStats.breachedSlos)}
          subtitle="Objectives requiring attention"
          accent={pageStats.breachedSlos > 0 ? 'pink' : 'blue'}
        />

        <MetricCard
          title="Processing success"
          value={formatValue(pageStats.processingSuccess?.value, pageStats.processingSuccess?.unit)}
          subtitle="Signal processing success rate"
          accent="green"
        />

        <MetricCard
          title="Avg latency"
          value={formatValue(pageStats.averageLatency?.value, pageStats.averageLatency?.unit)}
          subtitle="Average signal processing latency"
          accent="purple"
        />

        <MetricCard
          title="Alert delivery"
          value={formatValue(pageStats.alertDelivery?.value, pageStats.alertDelivery?.unit)}
          subtitle="Alert delivery success rate"
          accent="blue"
        />
      </section>

      <section className="slo-layout">
        <div className="slo-table-card">
          <div className="table-card__header">
            <div>
              <h2>SLO health</h2>
              <p>Current objective status based on live operational metrics.</p>
            </div>
            <span className="table-card__count">{slos.length} objectives</span>
          </div>

          <div className="slo-table">
            <div className="slo-table__row slo-table__row--head">
              <span>Objective</span>
              <span>Actual</span>
              <span>Target</span>
              <span>Status</span>
            </div>

            {slos.map((slo) => (
              <div className="slo-table__row" key={slo.key}>
                <span>
                  <strong>{prettifyKey(slo.key)}</strong>
                  <small>{slo.unit.toLowerCase()}</small>
                </span>
                <span>{formatValue(slo.actualValue, slo.unit)}</span>
                <span>{formatValue(slo.targetValue, slo.unit)}</span>
                <span>
                  <StatusBadge value={slo.status} variant="slo" />
                </span>
              </div>
            ))}
          </div>
        </div>

        <aside className="slo-side">
          <div className="panel">
            <h2>Processing health</h2>

            <div className="slo-health-list">
              <div>
                <span>Pending tasks</span>
                <strong>
                  {formatValue(pageStats.pendingTasks?.value, pageStats.pendingTasks?.unit)}
                </strong>
              </div>

              <div>
                <span>Average latency</span>
                <strong>
                  {formatValue(pageStats.averageLatency?.value, pageStats.averageLatency?.unit)}
                </strong>
              </div>

              <div>
                <span>Processing success</span>
                <strong>
                  {formatValue(pageStats.processingSuccess?.value, pageStats.processingSuccess?.unit)}
                </strong>
              </div>
            </div>
          </div>

          <div className="panel">
            <h2>Metric groups</h2>

            <div className="metric-group-list">
              {Object.entries(groupedMetrics).map(([group, groupMetrics]) => (
                <div key={group}>
                  <span>{group}</span>
                  <strong>{groupMetrics.length}</strong>
                </div>
              ))}
            </div>
          </div>
        </aside>
      </section>

      <section className="metric-groups-grid">
        {Object.entries(groupedMetrics).map(([group, groupMetrics]) => (
          <article className="metric-group-card" key={group}>
            <div className="section-heading">
              <h2>{group}</h2>
              <span>{groupMetrics.length} metrics</span>
            </div>

            <div className="metric-list">
              {groupMetrics.map((metric) => (
                <div className="metric-list__item" key={metric.name}>
                  <span>{prettifyKey(metric.name)}</span>
                  <strong>{formatValue(metric.value, metric.unit)}</strong>
                </div>
              ))}
            </div>
          </article>
        ))}
      </section>
    </div>
  )
}
