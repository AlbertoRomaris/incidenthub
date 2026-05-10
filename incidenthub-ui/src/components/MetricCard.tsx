type MetricCardProps = {
  title: string
  value: string
  subtitle: string
  accent?: 'blue' | 'green' | 'orange' | 'purple' | 'pink'
}

export function MetricCard({ title, value, subtitle, accent = 'blue' }: MetricCardProps) {
  return (
    <article className={`metric-card metric-card--${accent}`}>
      <div className="metric-card__label">{title}</div>
      <div className="metric-card__value">{value}</div>
      <div className="metric-card__subtitle">{subtitle}</div>
    </article>
  )
}