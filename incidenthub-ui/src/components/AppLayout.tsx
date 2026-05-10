import type { ReactNode } from 'react'

export type AppView = 'dashboard' | 'incidents' | 'slos' | 'alerts'

type AppLayoutProps = {
  children: ReactNode
  currentView: AppView
  onNavigate: (view: AppView) => void
}

const navItems: Array<{ view: AppView; label: string }> = [
  { view: 'dashboard', label: 'Dashboard' },
  { view: 'incidents', label: 'Incidents' },
  { view: 'slos', label: 'SLOs' },
  { view: 'alerts', label: 'Alerts' },
]

export function AppLayout({ children, currentView, onNavigate }: AppLayoutProps) {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar__brand">
          <div className="sidebar__logo">🚨</div>
          <div>
            <strong>IncidentHub</strong>
            <span>Ops Dashboard</span>
          </div>
        </div>

        <nav className="sidebar__nav">
          {navItems.map((item) => (
            <button
              className={
                currentView === item.view
                  ? 'sidebar__link sidebar__link--active'
                  : 'sidebar__link'
              }
              key={item.view}
              type="button"
              onClick={() => onNavigate(item.view)}
            >
              {item.label}
            </button>
          ))}
        </nav>

        <div className="sidebar__footer">
          <span>V7 UI Preview</span>
          <strong>Local mode</strong>
        </div>
      </aside>

      <main className="main-content">{children}</main>
    </div>
  )
}
