import { useState } from 'react'
import { AppLayout, type AppView } from './components/AppLayout'
import { DashboardPage } from './pages/DashboardPage'
import { IncidentDetailPage } from './pages/IncidentDetailPage'
import { IncidentsPage } from './pages/IncidentsPage'
import type { Incident } from './types/incidenthub'

function App() {
  const [currentView, setCurrentView] = useState<AppView>('dashboard')
  const [selectedIncident, setSelectedIncident] = useState<Incident | null>(null)

  function navigate(view: AppView) {
    setCurrentView(view)
    setSelectedIncident(null)
  }

  function openIncident(incident: Incident) {
    setSelectedIncident(incident)
  }

  function renderCurrentView() {
    if (selectedIncident) {
      return <IncidentDetailPage incident={selectedIncident} onBack={() => setSelectedIncident(null)} />
    }

    if (currentView === 'incidents') {
      return <IncidentsPage onSelectIncident={openIncident} />
    }

    if (currentView === 'slos') {
      return (
        <div className="dashboard-page">
          <div className="empty-state">
            <strong>SLOs page coming next</strong>
            <span>The next V7 step will expose SLO health and operational metrics in a dedicated page.</span>
          </div>
        </div>
      )
    }

    if (currentView === 'alerts') {
      return (
        <div className="dashboard-page">
          <div className="empty-state">
            <strong>Alerts page coming next</strong>
            <span>The next V7 step will list delivered and failed alert notifications.</span>
          </div>
        </div>
      )
    }

    return <DashboardPage onSelectIncident={openIncident} />
  }

  return (
    <AppLayout currentView={currentView} onNavigate={navigate}>
      {renderCurrentView()}
    </AppLayout>
  )
}

export default App
