import { useState } from 'react'
import { AppLayout } from './components/AppLayout'
import { DashboardPage } from './pages/DashboardPage'
import { IncidentDetailPage } from './pages/IncidentDetailPage'
import type { Incident } from './types/incidenthub'

function App() {
  const [selectedIncident, setSelectedIncident] = useState<Incident | null>(null)

  return (
    <AppLayout>
      {selectedIncident ? (
        <IncidentDetailPage
          incident={selectedIncident}
          onBack={() => setSelectedIncident(null)}
        />
      ) : (
        <DashboardPage onSelectIncident={setSelectedIncident} />
      )}
    </AppLayout>
  )
}

export default App