import type {
  Incident,
  IncidentAlert,
  IncidentEvidence,
  IncidentTimelineEvent,
  OperationalMetric,
  SloSummary,
} from '../types/incidenthub'

const API_BASE_URL = '/api'

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`)

  if (!response.ok) {
    throw new Error(`IncidentHub API request failed: ${response.status} ${response.statusText}`)
  }

  return response.json() as Promise<T>
}

export async function getIncidents(limit = 20): Promise<Incident[]> {
  return getJson<Incident[]>(`/incidents?limit=${limit}`)
}

export async function getOperationalMetrics(): Promise<OperationalMetric[]> {
  const response = await getJson<{ metrics: OperationalMetric[] }>('/metrics/operational')
  return response.metrics
}

export async function getSloSummary(): Promise<SloSummary[]> {
  const response = await getJson<{ slos: SloSummary[] }>('/slo/summary')
  return response.slos
}

export async function getIncidentTimeline(
  incidentId: string,
): Promise<IncidentTimelineEvent[]> {
  return getJson<IncidentTimelineEvent[]>(`/incidents/${incidentId}/timeline`)
}

export async function getIncidentEvidence(
  incidentId: string,
): Promise<IncidentEvidence[]> {
  return getJson<IncidentEvidence[]>(`/incidents/${incidentId}/evidence`)
}

export async function getIncidentAlerts(incidentId: string): Promise<IncidentAlert[]> {
  return getJson<IncidentAlert[]>(`/incidents/${incidentId}/alerts`)
}