import type { ReactNode } from 'react'

type AppLayoutProps = {
  children: ReactNode
}

export function AppLayout({ children }: AppLayoutProps) {
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
          <a className="sidebar__link sidebar__link--active" href="#">
            Dashboard
          </a>
          <a className="sidebar__link" href="#">
            Incidents
          </a>
          <a className="sidebar__link" href="#">
            SLOs
          </a>
          <a className="sidebar__link" href="#">
            Alerts
          </a>
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