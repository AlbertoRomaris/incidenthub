import { useState } from 'react'
import { AppLayout, type AppView } from './components/AppLayout'
import { AlertsPage } from './pages/AlertsPage'
import { DashboardPage } from './pages/DashboardPage'
import { IncidentDetailPage } from './pages/IncidentDetailPage'
import { IncidentsPage } from './pages/IncidentsPage'
import { SlosPage } from './pages/SlosPage'
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
      return <SlosPage />
    }

    if (currentView === 'alerts') {
      return <AlertsPage onSelectIncident={openIncident} />
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
