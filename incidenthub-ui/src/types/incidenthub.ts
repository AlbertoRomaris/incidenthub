export type IncidentSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type IncidentStatus = 'OPEN' | 'ACKNOWLEDGED' | 'RESOLVED'

export interface Incident {
  incidentId: string
  serviceName: string
  environment: string
  incidentType: string
  severity: IncidentSeverity
  status: IncidentStatus
  summary: string
  description: string
  occurrenceCount: number
  firstSeenAt: string
  lastSeenAt: string
  openedAt: string
  acknowledgedAt?: string | null
  resolvedAt?: string | null
}

export interface OperationalMetric {
  name: string
  type: string
  unit: string
  value: number
}

export interface SloSummary {
  key: string
  actualValue: number
  targetValue: number
  unit: string
  status: 'HEALTHY' | 'BREACHED'
}