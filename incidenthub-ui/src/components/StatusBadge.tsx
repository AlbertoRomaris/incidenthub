type StatusBadgeProps = {
  value: string
  variant?: 'status' | 'severity' | 'slo'
}

export function StatusBadge({ value, variant = 'status' }: StatusBadgeProps) {
  const normalized = value.toLowerCase().replaceAll('_', '-')

  return (
    <span className={`status-badge status-badge--${variant} status-badge--${normalized}`}>
      {value}
    </span>
  )
}