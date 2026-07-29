const STATUS_LABEL = {
  IN_PROGRESS: 'In progress...',
  WIN: 'Game over - winner:',
  DRAW: 'Game over - draw',
  FAILED: 'Simulation failed:',
}

export default function StatusPanel({ status, winner, failureReason }) {
  if (!status) {
    return <p className="status status--idle">Press "Start Simulation" to begin.</p>
  }

  return (
    <p className={`status status--${status.toLowerCase()}`}>
      {STATUS_LABEL[status] ?? status}
      {status === 'WIN' && winner ? ` ${winner}` : null}
      {status === 'FAILED' && failureReason ? ` ${failureReason}` : null}
    </p>
  )
}
