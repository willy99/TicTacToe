const STATUS_LABEL = {
  IN_PROGRESS: 'In progress...',
  WIN: 'Game over - winner:',
  DRAW: 'Game over - draw',
}

export default function StatusPanel({ status, winner }) {
  if (!status) {
    return <p className="status status--idle">Press "Start Simulation" to begin.</p>
  }

  return (
    <p className={`status status--${status.toLowerCase()}`}>
      {STATUS_LABEL[status] ?? status}
      {status === 'WIN' && winner ? ` ${winner}` : null}
    </p>
  )
}
